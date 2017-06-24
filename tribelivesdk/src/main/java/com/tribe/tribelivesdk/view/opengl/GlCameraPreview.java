package com.tribe.tribelivesdk.view.opengl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import com.tribe.tribelivesdk.view.opengl.gles.DefaultConfigChooser;
import com.tribe.tribelivesdk.view.opengl.gles.DefaultContextFactory;
import com.tribe.tribelivesdk.view.opengl.gles.GlTextureView;
import com.tribe.tribelivesdk.view.opengl.renderer.PreviewRenderer;
import rx.Observable;

import static android.opengl.GLES20.GL_MAX_RENDERBUFFER_SIZE;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.glGetIntegerv;

public class GlCameraPreview extends GlTextureView implements PreviewRenderer.RendererCallback {

  private int maxTextureSize;
  private int maxRenderBufferSize;

  private boolean isInitialized = false;

  @NonNull protected final PreviewRenderer renderer;

  final boolean faceMirror = true;

  public GlCameraPreview(@NonNull final Context context) {
    this(context, null);
  }

  public GlCameraPreview(@NonNull final Context context, final AttributeSet attrs) {
    super(context, attrs);

    setEGLConfigChooser(new DefaultConfigChooser(false, 2));
    setEGLContextFactory(new DefaultContextFactory(2));

    renderer = new PreviewRenderer(context, this);

    setRenderer(renderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
  }

  @Override public void onSurfaceChanged(int width, int height) {

  }

  public synchronized void startPreview() {
    if (!isInitialized) return;
    queueEvent(new Runnable() {
      @Override public void run() {
        if (maxTextureSize == 0) {

          final int[] args = new int[1];
          glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
          maxTextureSize = args[0];

          glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, args, 0);
          maxRenderBufferSize = args[0];
        }

        startPreviewAfterTextureSizeAvailable();
      }
    });
  }

  public synchronized void onRendererInitialized() {
    isInitialized = true;
    startPreview();
  }

  private synchronized void startPreviewAfterTextureSizeAvailable() {
    post(() -> {
      requestLayout();
      queueEvent(() -> renderer.onStartPreview(faceMirror));
    });
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<SurfaceTexture> onSurfaceTextureReady() {
    return renderer.onSurfaceTextureReady();
  }
}