package com.tribe.tribelivesdk.view.opengl.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.view.opengl.filter.ColorFilterBW;
import com.tribe.tribelivesdk.view.opengl.filter.ImageFilter;
import com.tribe.tribelivesdk.view.opengl.gles.GlSurfaceTexture;
import com.tribe.tribelivesdk.view.opengl.gles.PreviewTextureInterface;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

  private static final File FILES_DIR = Environment.getExternalStorageDirectory();

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
  private Frame frame;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<SurfaceTexture> onSurfaceTextureReady = PublishSubject.create();
  private PublishSubject<Frame> onFrameAvailable = PublishSubject.create();

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

    byteBuffer = ByteBuffer.allocateDirect((int) surfaceWidth * (int) surfaceHeight * 4);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

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

    if (previewTexture != null && byteBuffer != null) {
      filter.draw(previewTexture, mvpMatrix, stMatrix, cameraRatio);

      synchronized (frameListenerLock) {
        byteBuffer.rewind();
        long start = System.currentTimeMillis();

        //  Timber.d("glReadPixels: " + (end - start));

        int width = (int) surfaceWidth;
        int height = (int) surfaceHeight;
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
            byteBuffer);
        byteBuffer.flip();
        long end = System.currentTimeMillis();
        Timber.d("glReadPixels: " + (end - start));

        //BufferedOutputStream bos = null;
        //try {
        //  File file =
        //      new File(FILES_DIR, String.format("frame-" + System.currentTimeMillis() + ".png"));
        //  bos = new BufferedOutputStream(new FileOutputStream(file.toString()));
        //  Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //  byteBuffer.rewind();
        //  bmp.copyPixelsFromBuffer(byteBuffer);
        //  bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
        //  bmp.recycle();
        //} catch (FileNotFoundException e) {
        //  e.printStackTrace();
        //} finally {
        //  if (bos != null) {
        //    try {
        //      bos.close();
        //    } catch (IOException e) {
        //      e.printStackTrace();
        //    }
        //  }
        //}
        frame = new Frame(byteBuffer.array(), width, height, cameraInfo.getFrameOrientation(),
            previewTexture.getTimestamp(), cameraInfo.isFrontFacing());
        onFrameAvailable.onNext(frame);
      }
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
