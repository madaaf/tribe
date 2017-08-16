package com.tribe.tribelivesdk.view.opengl.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import com.tribe.tribelivesdk.core.MediaConstraints;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.facetracking.UlseeManager;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.view.opengl.filter.FaceMaskFilter;
import com.tribe.tribelivesdk.view.opengl.filter.FilterManager;
import com.tribe.tribelivesdk.view.opengl.filter.FilterMask;
import com.tribe.tribelivesdk.view.opengl.filter.ImageFilter;
import com.tribe.tribelivesdk.view.opengl.gles.GlSurfaceTexture;
import com.tribe.tribelivesdk.view.opengl.gles.PreviewTextureInterface;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.RendererCommon;
import com.uls.renderer.GLRenderMask;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.microedition.khronos.egl.EGLConfig;
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
  private boolean updateSurface = false, openGLContextSet = false;
  private UlseeManager ulseeManager;
  private GameManager gameManager;
  private float[] stMatrix = new float[16];
  private CameraInfo cameraInfo;
  private float surfaceWidth = 1, surfaceHeight = 1;
  private int framesSkipped = 0;
  public int widthOut = MediaConstraints.MAX_HEIGHT, heightOut = MediaConstraints.MAX_WIDTH,
      previousWidthOut = 0, previousHeightOut = 0;
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
    libYuvConverter = LibYuvConverter.getInstance();
    gameManager = GameManager.getInstance(context);
    initFilterManager();
  }

  public void initFilterManager() {
    filterManager = FilterManager.getInstance(context);
  }

  public void initSwitchFilterSubscription(Observable<FilterMask> obs) {

  }

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    subscriptions.add(obs.subscribe(integer -> {
    }));
  }

  public void updateCameraInfo(CameraInfo cameraInfo) {
    this.cameraInfo = cameraInfo;
    computeMatrices();
    computeSizeOutput();

    if (previousWidthOut != widthOut || previousHeightOut != heightOut) {
      Timber.d("Change of size : %d / %d", widthOut, heightOut);
      byteBuffer = ByteBuffer.allocateDirect(widthOut * heightOut * 4);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

      if (openGLContextSet) updateOES();

      previousWidthOut = widthOut;
      previousHeightOut = heightOut;
    }

    if (ulsRenderer != null) ulsRenderer.updateCameraInfo(cameraInfo);
  }

  public void setLayoutAspectRatio(float layoutAspectRatio) {
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

  public void switchFilter(FilterMask filterMask) {
    if (filterMask == null || filterMask.equals(filter) || filterMask.equals(maskFilter)) {
      clearImageFilter();
      clearMask();
      filter = filterManager.getBaseFilter();
      filter.updateTextureSize(widthOut, heightOut);
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
      faceMaskFilter.computeMask(filterManager.getMaskAndGlassesPath(),
          cameraInfo == null || cameraInfo.isFrontFacing());
      maskFilter = (FaceMaskFilter) filterMask;
    }

    filter.updateTextureSize(widthOut, heightOut);

    rendererCallback.requestRender();
  }

  public void disposeFilter() {
    filter.release();
    filter = null;
  }

  public void dispose() {
    subscriptions.clear();

    if (byteBuffer != null) {
      byteBuffer.clear();
      byteBuffer = null;
    }

    previewTexture.release();
    previewTexture = null;

    if (maskFilter != null) {
      maskFilter.release();
      maskFilter = null;
    }

    libYuvConverter.releasePBO();

    maskRender = null;

    openGLContextSet = false;
  }

  @Override public void onSurfaceCreated(final EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

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

  @Override public void onSurfaceChanged(final int width, final int height) {
    surfaceWidth = width;
    surfaceHeight = height;

    if (previousWidthOut != widthOut || previousHeightOut != heightOut || !openGLContextSet) {
      openGLContextSet = true;
      updateOES();
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
      // TODO TASK_FORCE
      // The part of the code that constructs the openGL matrix that
      // will be used to display the camera preview
      float[] texMatrix =
          RendererCommon.rotateTextureMatrix(stMatrix, cameraInfo.getFrameOrientation());
      float[] drawMatrix;
      synchronized (this.layoutLock) {
        float[] layoutMatrix;
        layoutMatrix = cameraInfo.isFrontFacing() ? RendererCommon.horizontalFlipMatrix()
            : RendererCommon.identityMatrix();
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
          draw(i, rotation, widthOut, heightOut);
          glViewport(0, 0, (int) surfaceWidth, (int) surfaceHeight);
          GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
          //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
          draw(i, rotation, (int) surfaceWidth, (int) surfaceHeight);
        }
      }

      if (!gameManager.isLocalFrameDifferent()) {
        synchronized (frameListenerLock) {
          if (filter.getFbo() == null) return;
          if (widthOut * heightOut * 4 != byteBuffer.capacity()) return;

          filter.getFbo().bind();
          byteBuffer.rewind();
          libYuvConverter.readFromPBO(byteBuffer, widthOut, heightOut);
          byteBuffer.flip();
          GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

          frame =
              new Frame(byteBuffer.array(), widthOut, heightOut, 0, previewTexture.getTimestamp(),
                  cameraInfo.isFrontFacing());
          onFrameAvailable.onNext(frame);
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

  private void draw(int index, int rotation, float widthOut, float heightOut) {
    // TODO TASK_FORCE
    // The ratioH determines how the mask will expand vertically,
    // there might be something to change here too
    float ratioH =
        (float) cameraInfo.getCaptureFormat().height / (float) cameraInfo.getCaptureFormat().width;
    int width = cameraInfo.getCaptureFormat().width, height = cameraInfo.getCaptureFormat().height;
    float[][] shape = ulseeManager.getShape();
    float[][] shapeAdapted = new float[shape.length][];
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
      float[][] shapeFinal = shape;
      // TODO TASK_FORCE
      // Trying here to change the coordinates based on a ratio that was determined earlier
      // by the drawMatrix (I put 0.83f because it is the one given for New Call on my Nexus 6P)
      // Results not concluent so far

      //finalArray = shapeAdapted;
      //for (int k = 0; k < UlseeManager.MAX_TRACKER; k++) {
      //  if (shape[k] != null) {
      //    shapeAdapted[k] = new float[shape[k].length];
      //    for (int i = 0; i < (shape[k].length / 2); i++) {
      //      if (shape[k][i * 2] != 0 || shape[k][i * 2 + 1] != 0) {
      //        android.graphics.Matrix matrix = new android.graphics.Matrix();
      //        matrix.setScale(0.83f, 1f, 0f, 0f);
      //        float[] oldPoints = new float[] { shape[k][i * 2], shape[k][i * 2 + 1] };
      //        float[] newPoints = new float[2];
      //        matrix.mapPoints(newPoints, oldPoints);
      //        shapeAdapted[k][i * 2] = newPoints[0];
      //        shapeAdapted[k][i * 2 + 1] = newPoints[1];
      //        Timber.d("Old Points : " +
      //            Arrays.toString(oldPoints) +
      //            " / new points : " +
      //            Arrays.toString(newPoints));
      //      }
      //    }
      //  }
      //}

      if (pose != null && poseQuality[index] > 0.0f) {
        maskRender.drawMask(shapeFinal[index], confidence[index], 5.0f, rotation, width, height,
            ulsRenderer.getMaskFile(), isFrontFacing);
        if (isFrontFacing) {
          ulsRenderer.setTrackParam(width, height, shapeFinal[index], confidence[index],
              pupils[index], gaze[index], pose[index], poseQuality[index], isFrontFacing,
              cameraRotation, ratioH);
        } else {
          float[] flippedFaceShape = new float[99 * 2];
          flipFaceShape(flippedFaceShape, shapeFinal[index]);

          ulsRenderer.setTrackParam(width, height, flippedFaceShape, confidence[index],
              pupils[index], gaze[index], pose[index], poseQuality[index], isFrontFacing,
              cameraRotation, ratioH);
        }

        ulsRenderer.ulsDrawFrame(null, index, ratioH, true);
      }

      if (isFrontFacing) {
        ulsRenderer.setTrackParam(width, height, shapeFinal[index], confidence[index],
            pupils[index], gaze[index], pose[index], poseQuality[index], isFrontFacing,
            cameraRotation, ratioH);
      } else {
        float[] flippedFaceShape = new float[99 * 2];
        flipFaceShape(flippedFaceShape, shapeFinal[index]);

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
    //int maxWidth = cameraInfo.rotatedWidth();
    //int maxHeight = cameraInfo.rotatedHeight();

    //if (maxWidth > 0 && maxWidth > 0) {
    //  int width = (int) surfaceWidth;
    //  int height = (int) surfaceHeight;
    //  float ratioBitmap = (float) width / (float) height;
    //  float ratioMax = (float) maxWidth / (float) maxHeight;
    //
    //  widthOut = maxWidth;
    //  heightOut = maxHeight;
    //  if (ratioMax > 1) {
    //    widthOut = (int) ((float) maxHeight * ratioBitmap);
    //  } else {
    //    heightOut = (int) ((float) maxWidth / ratioBitmap);
    //  }
    //} else {
    //  widthOut = (int) surfaceWidth;
    //  heightOut = (int) surfaceHeight;
    //}
    //
    widthOut = cameraInfo.rotatedWidth();
    heightOut = cameraInfo.rotatedHeight();
    //widthOut += (widthOut % 2);
    //heightOut += (heightOut % 2);
  }

  private void updateOES() {
    libYuvConverter.releasePBO();
    libYuvConverter.initPBO(widthOut, heightOut);

    if (filter != null) filter.updateTextureSize(widthOut, heightOut);
  }

  private void flipFaceShape(float[] flippedFaceShape, float[] oriFaceShape) {
    int width = (int) surfaceWidth, height = (int) surfaceHeight;

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
