package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import com.tribe.tribelivesdk.view.opengl.filter.FrameRenderer;
import com.tribe.tribelivesdk.view.opengl.filter.FrameRendererDrawOrigin;
import com.tribe.tribelivesdk.view.opengl.render.GLTextureView;
import com.tribe.tribelivesdk.view.opengl.utils.Common;
import com.tribe.tribelivesdk.webrtc.Frame;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import timber.log.Timber;

public class GlLocalView extends View {

  @BindView(R.id.)
  private FrameLayout previewContainer;
  private FilterGLTextureView previewGLTexture;

  private int previewWidth;
  private int previewHeight;

  private Object frameListenerLock = new Object();
  private FrameListener frameListener;

  public GlLocalView(Context context) {
    super(context);
  }

  public GlLocalView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void init() {
    initResources();
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_local, this);
    unbinder = ButterKnife.bind(this);
  }

  public interface FrameListener {
    void onNewFrame(Frame frame, long timestamp);
  }

  public interface RendererCreator {
    FrameRenderer createRenderer();
  }

  private class FilterGLTextureView extends GLTextureView
      implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    public static final String LOG_TAG = Common.LOG_TAG;

    public int viewWidth;
    public int viewHeight;

    private FrameRenderer myRenderer;

    private SurfaceTexture surfaceTexture;
    private int textureID;

    public FrameRenderer.Viewport drawViewport;

    public class ClearColor {
      public float r, g, b, a;
    }

    public ClearColor clearColor;

    public synchronized void setFrameRenderer(final RendererCreator rendererCreator) {
      queueEvent(new Runnable() {
        @Override public void run() {
          FrameRenderer renderer = rendererCreator.createRenderer();

          if (renderer == null) {
            return;
          }

          myRenderer.release();
          myRenderer = renderer;
          myRenderer.setTextureSize(viewWidth, viewHeight);
          myRenderer.setRotation((float) Math.PI / 2.0f);

          GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

          Common.checkGLError("setFrameRenderer...");
        }
      });
    }

    public FilterGLTextureView(Context context, AttributeSet attrs) {
      super(context, attrs);

      setEGLContextClientVersion(2);
      setEGLConfigChooser(8, 8, 8, 8, 8, 0);
      setRenderer(this);
      setRenderMode(RENDERMODE_WHEN_DIRTY);

      clearColor = new ClearColor();
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      Log.i("stdzhu", "onSurfaceCreated...");

      GLES20.glDisable(GLES20.GL_DEPTH_TEST);
      GLES20.glDisable(GLES20.GL_STENCIL_TEST);

      textureID = genSurfaceTextureID();
      surfaceTexture = new SurfaceTexture(textureID);
      surfaceTexture.setOnFrameAvailableListener(this);

      FrameRenderer renderer = FrameRendererDrawOrigin.create(false);

      if (!renderer.init(true)) {
        renderer.release();
        return;
      }

      myRenderer = renderer;

      renderer.setRotation((float) Math.PI / 2.0f);

      requestRender();

      startPreview();
    }

    public void startPreview() {
      cameraInstance().startPreview(surfaceTexture);
      final int rotationX;
      if (cameraInstance().getCameraID() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        rotationX = 180;
      } else {
        rotationX = 0;
      }
      post(new Runnable() {
        @Override public void run() {
          setRotationX(rotationX);
        }
      });
    }

    private void calcViewport() {
      drawViewport = new FrameRenderer.Viewport();

      drawViewport.width = viewWidth;
      drawViewport.height = viewHeight;
      drawViewport.x = 0;
      drawViewport.y = 0;
    }

    @Override public void onSurfaceChanged(GL10 gl, final int width, final int height) {
      Log.i("stdzhu", String.format("onSurfaceChanged: %d x %d", width, height));

      GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);

      viewWidth = width;
      viewHeight = height;

      calcViewport();

      post(new Runnable() {
        @Override public void run() {
          previewGLTexture.setPivotX(width / 2);
          previewGLTexture.setPivotY(height / 2);
          float scale = 1.0f * previewContainer.getWidth() / width;
          previewGLTexture.setScaleX(scale);
          previewGLTexture.setScaleY(scale);

          setX((previewContainer.getWidth() - width) / 2);
          setY((previewContainer.getHeight() - height) / 2);
        }
      });
    }

    @Override public void onDrawFrame(GL10 gl) {
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

      myRenderer.renderTexture(textureID, drawViewport);

      synchronized (frameListenerLock) {
        if (frameListener != null) {
          Timber.d("New Frame: " + System.currentTimeMillis());
          //frameListener.onNewFrame(roiImage, System.nanoTime() / 1000);
        }
      }

      if (surfaceTexture != null) surfaceTexture.updateTexImage();
    }

    @Override public void onFrameAvailable(SurfaceTexture surfaceTexture) {
      requestRender();
    }

    private int genSurfaceTextureID() {
      int[] texID = new int[1];
      GLES20.glGenTextures(1, texID, 0);
      GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texID[0]);
      GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,
          GL10.GL_LINEAR);
      GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER,
          GL10.GL_LINEAR);
      GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S,
          GL10.GL_CLAMP_TO_EDGE);
      GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T,
          GL10.GL_CLAMP_TO_EDGE);
      return texID[0];
    }
  }
}