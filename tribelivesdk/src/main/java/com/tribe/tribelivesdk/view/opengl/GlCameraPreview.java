package com.tribe.tribelivesdk.view.opengl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.view.opengl.filter.FilterManager;
import com.tribe.tribelivesdk.view.opengl.filter.FilterMask;
import com.tribe.tribelivesdk.view.opengl.gles.DefaultConfigChooser;
import com.tribe.tribelivesdk.view.opengl.gles.DefaultContextFactory;
import com.tribe.tribelivesdk.view.opengl.gles.GlTextureView;
import com.tribe.tribelivesdk.view.opengl.renderer.PreviewRenderer;
import com.tribe.tribelivesdk.webrtc.Frame;
import org.webrtc.ThreadUtils;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.opengl.GLES20.GL_MAX_RENDERBUFFER_SIZE;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.glGetIntegerv;

public class GlCameraPreview extends GlTextureView implements PreviewRenderer.RendererCallback {

  private final PreviewRenderer renderer;
  private int maxTextureSize;
  private int maxRenderBufferSize;
  private boolean isInitialized = false;
  private final Object layoutLock = new Object();
  private CameraInfo cameraInfo;
  private FilterManager filterManager;

  // OBSERVABLE
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GlCameraPreview(@NonNull final Context context) {
    this(context, null);
  }

  public GlCameraPreview(@NonNull final Context context, final AttributeSet attrs) {
    super(context, attrs);

    setScaleType(CENTER_CROP_FILL);
    setEGLConfigChooser(
        new DefaultConfigChooser(8, 8, 8, 8, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_PBUFFER_BIT,
            EGL14.EGL_NONE));
    setEGLContextFactory(new DefaultContextFactory(2));

    renderer = new PreviewRenderer(context, this);

    setRenderer(renderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    initSubscriptions();
    initFilterManager();
  }

  private void initSubscriptions() {

  }

  public void initFilterManager() {
    filterManager = FilterManager.getInstance(getContext());
    subscriptions.add(filterManager.onFilterChange().subscribe(filterMask -> {
      queueEvent(() -> renderer.switchFilter(filterMask));
    }));
  }

  @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    super.onSurfaceTextureAvailable(surface, width, height);
    queueEvent(() -> renderer.switchFilter(filterManager.getFilter()));
  }

  @Override public void onSurfaceChanged(int width, int height) {
    if (cameraInfo != null) {
      Timber.d("height * ((float) cameraInfo.rotatedWidth() / cameraInfo.rotatedHeight() : " +
          height * cameraInfo.rotatedRatio());

      if (height > cameraInfo.rotatedHeight() && height * cameraInfo.rotatedRatio() > width) {
        Timber.d("CENTER_CROP_FILL");
        setScaleType(CENTER_CROP_FILL);
      } else {
        Timber.d("CENTER_CROP");
        setScaleType(CENTER_CROP);
      }
    }
    updateTextureViewSize();
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    ThreadUtils.checkIsOnMainThread();
    renderer.setLayoutAspectRatio((right - left) / (float) (bottom - top));
  }

  public void initSwitchFilterSubscription(Observable<FilterMask> obs) {
    renderer.initSwitchFilterSubscription(obs);
  }

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    renderer.initInviteOpenSubscription(obs);
  }

  public void updateCameraInfo(CameraInfo cameraInfo) {
    this.cameraInfo = cameraInfo;
    setContentWidth(cameraInfo.rotatedWidth());
    setContentHeight(cameraInfo.rotatedHeight());
    updateTextureViewSize();
    renderer.updateCameraInfo(cameraInfo);
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

  public void dispose() {
    queueEvent(() -> renderer.disposeFilter());
    renderer.dispose();
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