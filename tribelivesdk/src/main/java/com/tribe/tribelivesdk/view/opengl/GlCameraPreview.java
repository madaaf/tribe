package com.tribe.tribelivesdk.view.opengl;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.view.opengl.filter.FilterMask;
import com.tribe.tribelivesdk.view.opengl.gles.DefaultConfigChooser;
import com.tribe.tribelivesdk.view.opengl.gles.DefaultContextFactory;
import com.tribe.tribelivesdk.view.opengl.gles.GlTextureView;
import com.tribe.tribelivesdk.view.opengl.renderer.PreviewRenderer;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.RendererCommon;
import org.webrtc.ThreadUtils;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.opengl.GLES20.GL_MAX_RENDERBUFFER_SIZE;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.glGetIntegerv;

public class GlCameraPreview extends GlTextureView implements PreviewRenderer.RendererCallback {

  private final PreviewRenderer renderer;
  private int surfaceWidth, surfaceHeight, frameRotation, rotatedFrameWidth, rotatedFrameHeight;
  private int maxTextureSize;
  private int maxRenderBufferSize;
  private boolean isInitialized = false;
  private final RendererCommon.VideoLayoutMeasure videoLayoutMeasure =
      new RendererCommon.VideoLayoutMeasure();
  private final Object layoutLock = new Object();

  // OBSERVABLE
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GlCameraPreview(@NonNull final Context context) {
    this(context, null);
  }

  public GlCameraPreview(@NonNull final Context context, final AttributeSet attrs) {
    super(context, attrs);

    setEGLConfigChooser(
        new DefaultConfigChooser(8, 8, 8, 8, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_PBUFFER_BIT,
            EGL14.EGL_NONE));
    setEGLContextFactory(new DefaultContextFactory(2));

    renderer = new PreviewRenderer(context, this);

    videoLayoutMeasure.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
    setRenderer(renderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    synchronized (layoutLock) {
      rotatedFrameWidth = 0;
      rotatedFrameHeight = 0;
      frameRotation = 0;
    }

    initSubscriptions();
  }

  private void initSubscriptions() {

  }

  @Override public void onSurfaceChanged(int width, int height) {
    surfaceWidth = width;
    surfaceHeight = height;
    updateSurfaceSize();
  }

  @Override protected void onMeasure(int widthSpec, int heightSpec) {
    ThreadUtils.checkIsOnMainThread();
    final Point size;
    synchronized (layoutLock) {
      size =
          videoLayoutMeasure.measure(widthSpec, heightSpec, rotatedFrameWidth, rotatedFrameHeight);
    }
    setMeasuredDimension(size.x, size.y);
    //Timber.d("onMeasure(). New size: " + size.x + "x" + size.y);
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    ThreadUtils.checkIsOnMainThread();
    renderer.setLayoutAspectRatio((right - left) / (float) (bottom - top));
    updateSurfaceSize();
  }

  private void updateSurfaceSize() {
    ThreadUtils.checkIsOnMainThread();
    synchronized (layoutLock) {
      if (rotatedFrameWidth != 0 &&
          rotatedFrameHeight != 0 &&
          getWidth() != 0 &&
          getHeight() != 0) {
        final float layoutAspectRatio = getWidth() / (float) getHeight();
        final float frameAspectRatio = rotatedFrameWidth / (float) rotatedFrameHeight;
        final int drawnFrameWidth;
        final int drawnFrameHeight;
        if (frameAspectRatio > layoutAspectRatio) {
          drawnFrameWidth = (int) (rotatedFrameHeight * layoutAspectRatio);
          drawnFrameHeight = rotatedFrameHeight;
        } else {
          drawnFrameWidth = rotatedFrameWidth;
          drawnFrameHeight = (int) (rotatedFrameWidth / layoutAspectRatio);
        }
        // Aspect ratio of the drawn frame and the view is the same.
        final int width = Math.min(getWidth(), drawnFrameWidth);
        final int height = Math.min(getHeight(), drawnFrameHeight);
        Timber.d("updateSurfaceSize. Layout size: " +
            getWidth() +
            "x" +
            getHeight() +
            ", frame size: " +
            rotatedFrameWidth +
            "x" +
            rotatedFrameHeight +
            ", requested surface size: " +
            width +
            "x" +
            height +
            ", old surface size: " +
            surfaceWidth +
            "x" +
            surfaceHeight);
        if (width != surfaceWidth || height != surfaceHeight) {
          surfaceWidth = width;
          surfaceHeight = height;
        }
      } else {
        surfaceWidth = surfaceHeight = 0;
      }
    }
  }

  public void initSwitchFilterSubscription(Observable<FilterMask> obs) {
    renderer.initSwitchFilterSubscription(obs);
  }

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    renderer.initInviteOpenSubscription(obs);
  }

  public void updateCameraInfo(CameraInfo cameraInfo) {
    renderer.updateCameraInfo(cameraInfo);

    synchronized (layoutLock) {
      if (rotatedFrameWidth != cameraInfo.rotatedWidth() ||
          rotatedFrameHeight != cameraInfo.rotatedHeight() ||
          frameRotation != cameraInfo.getFrameOrientation()) {
        Timber.d("Reporting frame resolution changed to " +
            cameraInfo.getCaptureFormat().width +
            "x" +
            cameraInfo.getCaptureFormat().height +
            " with rotation " +
            cameraInfo.getFrameOrientation());

        rotatedFrameWidth = cameraInfo.rotatedWidth();
        rotatedFrameHeight = cameraInfo.rotatedHeight();
        frameRotation = cameraInfo.getFrameOrientation();

        post(() -> {
          updateSurfaceSize();
          requestLayout();
        });
      }
    }
  }

  public synchronized void startPreview() {
    if (!isInitialized) return;
    queueEvent(() -> {
      if (maxTextureSize == 0) {

        final int[] args = new int[1];
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
        maxTextureSize = args[0];

        glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, args, 0);
        maxRenderBufferSize = args[0];
      }

      startPreviewAfterTextureSizeAvailable();
    });
  }

  public synchronized void onRendererInitialized() {
    isInitialized = true;
    startPreview();
  }

  private synchronized void startPreviewAfterTextureSizeAvailable() {
    post(() -> {
      requestLayout();
      queueEvent(() -> renderer.onStartPreview());
    });
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<SurfaceTexture> onSurfaceTextureReady() {
    return renderer.onSurfaceTextureReady();
  }

  public Observable<Frame> onFrameAvailable() {
    return renderer.onFrameAvailable();
  }
}