package com.tribe.tribelivesdk.view.opengl.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.view.opengl.filter.ColorFilterBW;
import com.tribe.tribelivesdk.view.opengl.filter.ImageFilter;
import com.tribe.tribelivesdk.view.opengl.gles.GlSurfaceTexture;
import com.tribe.tribelivesdk.view.opengl.gles.PreviewTextureInterface;
import com.tribe.tribelivesdk.webrtc.FrameTexture;
import com.tribe.tribelivesdk.webrtc.YuvConverter;
import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import org.webrtc.CameraEnumerationAndroid;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

public class PreviewRenderer extends GlFrameBufferObjectRenderer
    implements PreviewTextureInterface.OnFrameAvailableListener {
  private static final String TAG = "PreviewRenderer";

  @NonNull private final Handler mainHandler;

  private PreviewTextureInterface previewTexture;
  private boolean updateSurface = false;

  private final float[] mvpMatrix = new float[16], projMatrix = new float[16], matrix =
      new float[16], vMatrix = new float[16], stMatrix = new float[16], stMatrixBis = new float[16];
  private CameraInfo cameraInfo;
  private float cameraRatio = 1, surfaceWidth = 1, surfaceHeight = 1;
  private final RendererCallback rendererCallback;
  @Nullable private ImageFilter filter;
  private ByteBuffer byteBuffer;
  private Object frameListenerLock = new Object();
  private FrameTexture frameTexture;
  private YuvConverter yuvConverter;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<SurfaceTexture> onSurfaceTextureReady = PublishSubject.create();
  private PublishSubject<FrameTexture> onFrameAvailable = PublishSubject.create();

  public PreviewRenderer(@NonNull Context context, final RendererCallback callback) {
    super();
    resetMatrix();
    rendererCallback = callback;
    mainHandler = new Handler(context.getMainLooper());
  }

  private void computeMatrices() {
    final int orientation = (cameraInfo == null) ? 90 : cameraInfo.getFrameOrientation();

    resetMatrix();

    Matrix.setIdentityM(matrix, 0);
    Matrix.rotateM(matrix, 0, orientation, 0.0f, 0.0f, 1f);

    if (cameraInfo == null ||
        cameraInfo.getCameraInfo().facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      //Matrix.scaleM(matrix, 0, 1.0f, -1.0f, 1.0f);
    }

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

  @Nullable public ImageFilter getFilter() {
    return filter;
  }

  public void setFilter(@Nullable final ImageFilter shader) {
    if (shader != null && shader.equals(this.filter)) {
      return;
    }

    if (this.filter != null) {
      this.filter.release();
    }

    this.filter = shader;
    rendererCallback.requestRender();
  }

  public void updateCameraInfo(CameraInfo cameraInfo) {
    this.cameraInfo = cameraInfo;
    computeMatrices();
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

    filter = new ColorFilterBW();

    resetMatrix();

    synchronized (this) {
      updateSurface = false;
    }

    mainHandler.post(() -> rendererCallback.onRendererInitialized());
  }

  private float stageRatio = Float.MIN_VALUE;

  @Override public void onSurfaceChanged(final int width, final int height) {

    surfaceWidth = width;
    surfaceHeight = height;

    byteBuffer = ByteBuffer.allocateDirect(width * height * 4);

    stageRatio = (stageRatio == Float.MIN_VALUE) ? width / (float) height : stageRatio;
    try {
      Matrix.frustumM(projMatrix, 0, -stageRatio, stageRatio, -1, 1, 5, 7);
    } catch (Exception ignored) {
      Timber.e("onSurfaceChanged exception", ignored);
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

    glClear(GL_COLOR_BUFFER_BIT);
    Matrix.multiplyMM(mvpMatrix, 0, vMatrix, 0, matrix, 0);
    Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0);

    if (previewTexture != null) {
      filter.draw(previewTexture, mvpMatrix, stMatrix, cameraRatio);
    }

    if (yuvConverter == null) {
      yuvConverter = new YuvConverter();
    }

    if (previewTexture != null) {
      int width = cameraInfo.getCaptureFormat().width;
      int height = cameraInfo.getCaptureFormat().height;
      int uv_width = (width + 7) / 8;
      int stride = 8 * uv_width;

      previewTexture.getTransformMatrix(stMatrixBis);

      byteBuffer.clear();
      yuvConverter.convert(byteBuffer, width, height, stride, previewTexture.getTextureId(),
          stMatrixBis);
    }

    //if (previewTexture != null) {
    //  frameTexture = new FrameTexture(cameraInfo.getCaptureFormat().width,
    //      cameraInfo.getCaptureFormat().height, previewTexture.getTextureId(), stMatrix,
    //      cameraInfo.getFrameOrientation(), previewTexture.getTimestamp());
    //  onFrameAvailable.onNext(frameTexture);
    //}

    //synchronized (frameListenerLock) {
    //  long start = System.currentTimeMillis();
    //  GLES20.glReadPixels(0, 0, (int) surfaceWidth, (int) surfaceHeight, GLES20.GL_RGBA,
    //      GLES20.GL_UNSIGNED_BYTE, byteBuffer);
    //  long end = System.currentTimeMillis();
    //  Timber.d("glReadPixels: " + (end - start));
    //}
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

  public Observable<FrameTexture> onFrameAvailable() {
    return onFrameAvailable;
  }
}
