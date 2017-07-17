package com.tribe.tribelivesdk.view.opengl.renderer;

import android.content.Context;
import android.graphics.Bitmap;
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.microedition.khronos.egl.EGLConfig;
import org.webrtc.RendererCommon;
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
  private float[] stMatrix = new float[16];
  private CameraInfo cameraInfo;
  private float surfaceWidth = 1, surfaceHeight = 1;
  private int framesSkipped = 0;
  public static int widthOut = 480, heightOut = 480;
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
    Timber.d("updateCameraInfo FrameRotation : " + cameraInfo.getFrameOrientation());
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

    if (shouldUpdateAllocations) {
      shouldUpdateAllocations = false;
      libYuvConverter.initPBO(widthOut, heightOut);

      byteBuffer = ByteBuffer.allocateDirect(widthOut * heightOut * 4);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    ulsRenderer.ulsSurfaceChanged(null, width, height);

    if (rendererCallback != null) {
      mainHandler.post(() -> rendererCallback.onSurfaceChanged(width, height));
    }
  }

  @Override public synchronized void onDrawFrame() {
    if (updateSurface && previewTexture != null) {
      previewTexture.updateTexImage();
      previewTexture.getTransformMatrix(stMatrix);
      if (cameraInfo.isFrontFacing()) {
        stMatrix = RendererCommon.multiplyMatrices(stMatrix, RendererCommon.horizontalFlipMatrix());
      }
      updateSurface = false;
    }

    glViewport(0, 0, (int) surfaceWidth, (int) surfaceHeight);
    glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (previewTexture != null && byteBuffer != null) {
      float[] texMatrix = com.tribe.tribelivesdk.webrtc.RendererCommon.rotateTextureMatrix(stMatrix,
          cameraInfo.getFrameOrientation());
      float[] drawMatrix;
      synchronized (this.layoutLock) {
        float[] layoutMatrix;
        if (this.layoutAspectRatio > 0.0F) {
          float videoAspectRatio =
              (float) cameraInfo.rotatedWidth() / (float) cameraInfo.rotatedHeight();
          layoutMatrix =
              RendererCommon.getLayoutMatrix(cameraInfo.isFrontFacing(), videoAspectRatio,
                  this.layoutAspectRatio);
        } else {
          layoutMatrix = cameraInfo.isFrontFacing() ? RendererCommon.horizontalFlipMatrix()
              : RendererCommon.identityMatrix();
        }

        drawMatrix = RendererCommon.multiplyMatrices(texMatrix, layoutMatrix);
      }

      float[] drawMatrixFBO;
      synchronized (this.layoutLock) {
        float[] layoutMatrix;
        if (this.layoutAspectRatio > 0.0F) {
          float videoAspectRatio =
              (float) cameraInfo.rotatedWidth() / (float) cameraInfo.rotatedHeight();
          layoutMatrix =
              RendererCommon.getLayoutMatrix(cameraInfo.isFrontFacing(), videoAspectRatio,
                  (float) widthOut / (float) heightOut);
        } else {
          layoutMatrix = cameraInfo.isFrontFacing() ? RendererCommon.horizontalFlipMatrix()
              : RendererCommon.identityMatrix();
        }

        drawMatrixFBO = RendererCommon.multiplyMatrices(texMatrix, layoutMatrix);
      }

      filter.draw(previewTexture, drawMatrix, drawMatrixFBO, 0, 0, (int) surfaceWidth,
          (int) surfaceHeight);

      if (maskFilter != null) {
        int rotation = ulseeManager.getCameraRotation();
        if (rotation != 90 && rotation != 270) rotation = 180 - ulseeManager.getCameraRotation();

        for (int i = 0; i < UlseeManager.MAX_TRACKER; i++) {
          glViewport(0, 0, widthOut, heightOut);
          filter.getFbo().bind();
          draw(i, rotation);
          glViewport(0, 0, (int) surfaceWidth, (int) surfaceHeight);
          GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
          //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
          draw(i, rotation);
        }
      }

      synchronized (frameListenerLock) {
        if ((shouldSkipFrames && framesSkipped < FRAMES_SKIP ||
            (widthOut * heightOut * 4 != byteBuffer.capacity())) && frame != null) {
          framesSkipped++;
          onFrameAvailable.onNext(frame);
          return;
        } else {
          filter.getFbo().bind();
          //long timeStart = System.nanoTime();
          byteBuffer.rewind();
          libYuvConverter.readFromPBO(byteBuffer, widthOut, heightOut);
          byteBuffer.flip();
          GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
          //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

          //long timeEndReadPBO = System.nanoTime();
          //Timber.d("Total time of read PBO frame "
          //    + " / "
          //    + (timeEndReadPBO - timeStart) / 1000000.0f
          //    + " ms");
          frame =
              new Frame(byteBuffer.array(), widthOut, heightOut, 0, previewTexture.getTimestamp(),
                  cameraInfo.isFrontFacing());
          onFrameAvailable.onNext(frame);

          //ByteBuffer byteBufferBitmap = ByteBuffer.wrap(byteBuffer.array());
          //Bitmap bitmap = Bitmap.createBitmap(widthOut, heightOut, Bitmap.Config.ARGB_8888);
          //bitmap.copyPixelsFromBuffer(byteBufferBitmap);
          //savePNGImageToGallery(bitmap, context, "tribe_test.jpg");

          //long timeEndFrame = System.nanoTime();
          //Timber.d("Total time of end frame "
          //    + " / "
          //    + (timeEndFrame - timeEndReadPBO) / 1000000.0f
          //    + " ms");
        }
      }
    }
  }

  // Save the processed image as a PNG file on the SD card and shown in the Android Gallery.
  protected void savePNGImageToGallery(Bitmap bmp, Context context, String baseFilename) {
    try {
      // Get the file path to the SD card.
      String baseFolder =
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
              .getAbsolutePath() + "/";
      File file = new File(baseFolder + baseFilename);

      // Open the file.
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
      // Save the image file as PNG.
      bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
      out.flush();    // Make sure it is saved to file soon, because we are about to add it to the Gallery.
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
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
    resetMatrix();
  }

  private void resetMatrix() {
    Matrix.setIdentityM(stMatrix, 0);
  }

  private void computeSizeOutput() {
    int maxWidth = cameraInfo.getCaptureFormat().width;
    int maxHeight = cameraInfo.getCaptureFormat().width;

    if (maxWidth > 0 && maxWidth > 0) {
      int width = (int) surfaceWidth;
      int height = (int) surfaceHeight;
      float ratioBitmap = (float) width / (float) height;
      float ratioMax = (float) maxWidth / (float) maxHeight;

      widthOut = maxWidth;
      height = maxHeight;
      if (ratioMax > 1) {
        widthOut = (int) ((float) maxHeight * ratioBitmap);
      } else {
        heightOut = (int) ((float) maxWidth / ratioBitmap);
      }
    } else {
      widthOut = (int) surfaceWidth;
      heightOut = (int) surfaceHeight;
    }
  }

  private void flipFaceShape(float[] flippedFaceShape, float[] oriFaceShape) {
    int width = cameraInfo.getCaptureFormat().width, height = cameraInfo.getCaptureFormat().height;

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
      filter = filterManager.getBaseFilter();
      return;
    }

    if (filterMask instanceof ImageFilter) {
      clearMask();

      ImageFilter shader = (ImageFilter) filterMask;
      if (shader == null || shader.equals(filter)) {
        shader = filterManager.getBaseFilter();
      }

      clearImageFilter();

      this.filter = shader;
    } else {
      clearMask();
      clearImageFilter();
      filter = filterManager.getBaseFilter();

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
