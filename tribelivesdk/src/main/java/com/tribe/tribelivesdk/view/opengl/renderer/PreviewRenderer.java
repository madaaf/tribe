package com.tribe.tribelivesdk.view.opengl.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.facetracking.UlseeManager;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.FilterManager;
import com.tribe.tribelivesdk.view.opengl.filter.FilterMask;
import com.tribe.tribelivesdk.view.opengl.filter.ImageFilter;
import com.tribe.tribelivesdk.view.opengl.gles.GlSurfaceTexture;
import com.tribe.tribelivesdk.view.opengl.gles.PreviewTextureInterface;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.uls.renderer.GLRenderMask;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.microedition.khronos.egl.EGLConfig;
import org.webrtc.CameraEnumerationAndroid;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

public class PreviewRenderer extends GlFrameBufferObjectRenderer
    implements PreviewTextureInterface.OnFrameAvailableListener {

  private static final int FRAMES_SKIP = 10;
  private static final File FILES_DIR = Environment.getExternalStorageDirectory();

  @NonNull private final Handler mainHandler;
  private Context context;
  private PreviewTextureInterface previewTexture;
  private boolean updateSurface = false, shouldUpdateAllocations = true, shouldSkipFrames = false;
  private UlseeManager ulseeManager;
  private final float[] mvpMatrix = new float[16], projMatrix = new float[16], matrix =
      new float[16], vMatrix = new float[16], stMatrix = new float[16], stMatrixBis = new float[16];
  private CameraInfo cameraInfo;
  private float cameraRatio = 1, surfaceWidth = 1, surfaceHeight = 1;
  private int framesSkipped = 0;
  private final RendererCallback rendererCallback;
  private ImageFilter filter;
  private FaceMaskFilter maskFilter;
  private GLRenderMask maskRender;
  private UlsRenderer ulsRenderer;
  private FilterManager filterManager;
  private ByteBuffer byteBuffer;
  private Object frameListenerLock = new Object();
  private Frame frame;
  private LibYuvConverter libYuvConverter;
  private final Object layoutLock = new Object();
  private float layoutAspectRatio;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<SurfaceTexture> onSurfaceTextureReady = PublishSubject.create();
  private PublishSubject<Frame> onFrameAvailable = PublishSubject.create();

  public PreviewRenderer(@NonNull Context context, final RendererCallback callback) {
    super();
    resetMatrix();
    this.context = context;
    rendererCallback = callback;
    mainHandler = new Handler(context.getMainLooper());
    ulseeManager = UlseeManager.getInstance(context);
    filterManager = FilterManager.getInstance(context);
    libYuvConverter = LibYuvConverter.getInstance();
  }

  public void initSwitchFilterSubscription(Observable<FilterMask> obs) {
    subscriptions.add(obs.subscribe(filterMask -> {
      switchFilter(filterMask);
    }));
  }

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    subscriptions.add(obs.subscribe(integer -> {
      shouldUpdateAllocations = true;
    }));
  }

  public void updateCameraInfo(CameraInfo cameraInfo) {
    this.cameraInfo = cameraInfo;
    computeMatrices();

    if (ulsRenderer != null) ulsRenderer.updateCameraInfo(cameraInfo);
  }

  public void setLayoutAspectRatio(float layoutAspectRatio) {
    Timber.d("setLayoutAspectRatio: " + layoutAspectRatio);
    synchronized (this.layoutLock) {
      this.layoutAspectRatio = layoutAspectRatio;
    }
  }

  public void onStartPreview() {
    computeMatrices();

    if (previewTexture == null) {
      previewTexture = new GlSurfaceTexture();
      previewTexture.setOnFrameAvailableListener(this);
      previewTexture.onSurfaceTextureReady().subscribe(onSurfaceTextureReady);
    }

    previewTexture.setup();
  }

  @Override public void onSurfaceCreated(final EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    switchFilter(filterManager.getFilter());

    resetMatrix();

    synchronized (this) {
      updateSurface = false;
    }

    maskRender = new GLRenderMask(context);
    ulsRenderer = UlsRenderer.getInstance(context);
    ulsRenderer.ulsSurfaceCreated(null, null);
    if (cameraInfo != null) ulsRenderer.updateCameraInfo(cameraInfo);

    mainHandler.post(() -> rendererCallback.onRendererInitialized());
  }

  private float stageRatio = Float.MIN_VALUE;

  @Override public void onSurfaceChanged(final int width, final int height) {
    if (!shouldSkipFrames && !shouldUpdateAllocations) {
      shouldSkipFrames = true;
      framesSkipped = 0;
    }

    surfaceWidth = width;
    surfaceHeight = height;

    ulsRenderer.ulsSurfaceChanged(null, width, height);

    if (shouldUpdateAllocations) {
      shouldUpdateAllocations = false;
      libYuvConverter.initPBO((int) surfaceWidth, (int) surfaceHeight);

      byteBuffer = ByteBuffer.allocateDirect((int) surfaceWidth * (int) surfaceHeight * 4);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

      stageRatio = (stageRatio == Float.MIN_VALUE) ? width / (float) height : stageRatio;
      try {
        float zoom = 4f;
        Matrix.frustumM(projMatrix, 0, -stageRatio, stageRatio, -1, 1, zoom, 25 * zoom);
      } catch (Exception ignored) {
        Timber.e("onSurfaceChanged exception", ignored);
      }
    }

    if (rendererCallback != null) {
      rendererCallback.onSurfaceChanged(width, height);
    }
  }

  @Override public synchronized void onDrawFrame() {
    if (updateSurface && previewTexture != null) {
      previewTexture.updateTexImage();
      previewTexture.getTransformMatrix(stMatrix);
      updateSurface = false;
    }

    glViewport(0, 0, (int) surfaceWidth, (int) surfaceHeight);
    glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    Matrix.multiplyMM(mvpMatrix, 0, vMatrix, 0, matrix, 0);
    Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0);

    if (previewTexture != null && byteBuffer != null) {
      filter.draw(previewTexture, mvpMatrix, stMatrix, cameraRatio);

      if (maskFilter != null) {
        int rotation = ulseeManager.getCameraRotation();
        if (rotation != 90 && rotation != 270) rotation = 180 - ulseeManager.getCameraRotation();

        for (int i = 0; i < UlseeManager.MAX_TRACKER; i++) {
          draw(i, rotation);
        }
      }

      if (shouldSkipFrames && framesSkipped < FRAMES_SKIP ||
          ((int) surfaceWidth * (int) surfaceHeight * 4 != byteBuffer.capacity())) {
        framesSkipped++;
        onFrameAvailable.onNext(frame);
        return;
      } else {
        Timber.d("Surface width : " + surfaceWidth + " / surface height : " + surfaceHeight);
        //long timeStart = System.nanoTime();
        byteBuffer.rewind();
        libYuvConverter.readFromPBO(byteBuffer, (int) surfaceWidth, (int) surfaceHeight);
        byteBuffer.flip();

        //long timeEndReadPBO = System.nanoTime();
        //Timber.d("Total time of read PBO frame "
        //    + " / "
        //    + (timeEndReadPBO - timeStart) / 1000000.0f
        //    + " ms");
        int width = (int) surfaceWidth;
        int height = (int) surfaceHeight;
        frame = new Frame(byteBuffer.array(), width, height, 0, previewTexture.getTimestamp(),
            cameraInfo.isFrontFacing());
        onFrameAvailable.onNext(frame);

        //long timeEndFrame = System.nanoTime();
        //Timber.d("Total time of end frame "
        //    + " / "
        //    + (timeEndFrame - timeEndReadPBO) / 1000000.0f
        //    + " ms");
      }
    }
  }

  private void draw(int index, int rotation) {
    float ratioH = (surfaceWidth / surfaceHeight);
    int width = cameraInfo.getCaptureFormat().width, height = cameraInfo.getCaptureFormat().height;
    float[][] shape = ulseeManager.getShape();
    float[][] pose = ulseeManager.getPose();
    float[][] confidence = ulseeManager.getConfidence();
    float[] poseQuality = ulseeManager.getPoseQuality();
    float[][] gaze = ulseeManager.getGaze();
    float[][] pupils = ulseeManager.getPupils();
    int cameraRotation = ulseeManager.getCameraRotation();
    boolean isFrontFacing = cameraInfo.isFrontFacing();

    if (shape[index] == null) {
      ulsRenderer.setTrackParamNoFace(isFrontFacing);
      ulsRenderer.ulsDrawFrame(null, index, ratioH, false);
    } else {
      if (pose != null && poseQuality[index] > 0.0f) {
        maskRender.drawMask(shape[index], confidence[index], 5.0f, rotation, width, height,
            ulsRenderer.getMaskFile(), isFrontFacing);
        if (isFrontFacing) {
          ulsRenderer.setTrackParam(width, height, shape[index], confidence[index], pupils[index],
              gaze[index], pose[index], poseQuality[index], isFrontFacing, cameraRotation, ratioH);
        } else {
          float[] flippedFaceShape = new float[99 * 2];
          flipFaceShape(flippedFaceShape, shape[index]);

          ulsRenderer.setTrackParam(width, height, flippedFaceShape, confidence[index],
              pupils[index], gaze[index], pose[index], poseQuality[index], isFrontFacing,
              cameraRotation, ratioH);
        }

        ulsRenderer.ulsDrawFrame(null, index, ratioH, true);
      }

      if (isFrontFacing) {
        ulsRenderer.setTrackParam(width, height, shape[index], confidence[index], pupils[index],
            gaze[index], pose[index], poseQuality[index], isFrontFacing, cameraRotation, ratioH);
      } else {
        float[] flippedFaceShape = new float[99 * 2];
        flipFaceShape(flippedFaceShape, shape[index]);

        ulsRenderer.setTrackParam(width, height, flippedFaceShape, confidence[index], pupils[index],
            gaze[index], pose[index], poseQuality[index], isFrontFacing, cameraRotation, ratioH);
      }
    }

    GLES20.glFlush();
  }

  private void computeMatrices() {
    final int orientation = (cameraInfo == null) ? 90 : cameraInfo.getFrameOrientation();

    resetMatrix();

    Matrix.setIdentityM(matrix, 0);
    Matrix.rotateM(matrix, 0, orientation, 0.0f, 0.0f, 1f);

    final CameraEnumerationAndroid.CaptureFormat captureFormat = cameraInfo.getCaptureFormat();
    cameraRatio = 1;
    if (captureFormat != null) {
      cameraRatio = captureFormat.width / (float) captureFormat.height;
    }
  }

  public void resetMatrix() {
    Matrix.setIdentityM(stMatrix, 0);
    Matrix.setIdentityM(matrix, 0);
    Matrix.rotateM(matrix, 0, 180, 0.0f, 0.0f, 1.0f);
    Matrix.scaleM(matrix, 0, -1f, 1f, 1f);
    Matrix.setLookAtM(vMatrix, 0, 0.0f, 0.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
  }

  private void flipFaceShape(float[] flippedFaceShape, float[] oriFaceShape) {
    int width = (int) surfaceHeight, height = (int) surfaceWidth;

    if (oriFaceShape == null) return;
    for (int i = 0; i < 99; i++) {
      flippedFaceShape[i * 2] = width - oriFaceShape[i * 2];
      flippedFaceShape[i * 2 + 1] = height - oriFaceShape[i * 2 + 1];
    }
  }

  private void clearMask() {
    if (maskFilter != null) {
      maskFilter.release();
      maskFilter = null;
    }
  }

  private void clearImageFilter() {
    if (filter != null) {
      filter.release();
    }
  }

  private void switchFilter(FilterMask filterMask) {
    if (filterMask == null || filterMask.equals(filter) || filterMask.equals(maskFilter)) {
      clearImageFilter();
      clearMask();
      filter = new ImageFilter(context, ImageFilter.IMAGE_FILTER_NONE, "None", -1);
      return;
    }

    if (filterMask instanceof ImageFilter) {
      clearMask();

      ImageFilter shader = (ImageFilter) filterMask;
      if (shader == null || shader.equals(filter)) {
        shader = new ImageFilter(context, ImageFilter.IMAGE_FILTER_NONE, "None", -1);
      }

      clearImageFilter();

      this.filter = shader;
    } else {
      clearMask();
      clearImageFilter();
      filter = new ImageFilter(context, ImageFilter.IMAGE_FILTER_NONE, "None", -1);

      FaceMaskFilter faceMaskFilter = (FaceMaskFilter) filterMask;
      faceMaskFilter.computeMask(filterManager.getMaskAndGlassesPath(), cameraInfo.isFrontFacing());
      maskFilter = (FaceMaskFilter) filterMask;
    }

    rendererCallback.requestRender();
  }

  @Override
  public synchronized void onFrameAvailable(final PreviewTextureInterface previewTexture) {
    updateSurface = true;
    rendererCallback.requestRender();
  }

  public interface RendererCallback {
    void onRendererInitialized();

    void requestRender();

    void onSurfaceChanged(final int width, final int height);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<SurfaceTexture> onSurfaceTextureReady() {
    return onSurfaceTextureReady;
  }

  public Observable<Frame> onFrameAvailable() {
    return onFrameAvailable;
  }
}
