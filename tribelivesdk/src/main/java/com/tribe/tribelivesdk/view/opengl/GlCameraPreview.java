package com.tribe.tribelivesdk.view.opengl;

import android.content.Context;
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
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

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

    setRenderer(renderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
  }

  @Override public void onSurfaceChanged(int width, int height) {

  }

  public void initSwitchFilterSubscription(Observable<FilterMask> obs) {
    renderer.initSwitchFilterSubscription(obs);
  }

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    renderer.initInviteOpenSubscription(obs);
  }

  public void updateCameraInfo(CameraInfo cameraInfo) {
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