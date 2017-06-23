package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.R2;
import com.tribe.tribelivesdk.view.opengl.filter.FrameRenderer;
import com.tribe.tribelivesdk.view.opengl.filter.FrameRendererToneCurve;
import com.tribe.tribelivesdk.view.opengl.render.GLTextureView;
import com.tribe.tribelivesdk.view.opengl.utils.Common;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.io.BufferedInputStream;
import java.io.IOException;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.webrtc.CameraEnumerationAndroid;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class GlLocalView extends FrameLayout {

  @BindView(R2.id.viewContainer) FrameLayout previewContainer;
  private FilterGLTextureView previewGLTexture;

  private Unbinder unbinder;
  private int previewWidth, previewHeight;
  private Object frameListenerLock = new Object();
  private boolean frontFacing = true;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Frame> onNewFrame = PublishSubject.create();
  private PublishSubject<SurfaceTexture> onSurfaceTextureReady = PublishSubject.create();

  public GlLocalView(Context context) {
    super(context);
    init();
  }

  public GlLocalView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    initResources();
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_gl_local, this);
    unbinder = ButterKnife.bind(this);

    previewGLTexture = new FilterGLTextureView(getContext(), null);
    previewContainer.addView(previewGLTexture, ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
  }

  private void initResources() {

  }

  private void initDependencyInjector() {

  }

  public void initOnNewCaptureFormat(Observable<CameraEnumerationAndroid.CaptureFormat> obs) {
    subscriptions.add(obs.observeOn(AndroidSchedulers.mainThread()).subscribe(captureFormat -> onCameraCaptureFormatChange(captureFormat)));
  }

  private void onCameraCaptureFormatChange(CameraEnumerationAndroid.CaptureFormat captureFormat) {
    //ViewGroup.LayoutParams plp = previewGLTexture.getLayoutParams();
    //plp.width = getWidth();
    //plp.height = getWidth() * (captureFormat.width / captureFormat.height);
    //previewGLTexture.setLayoutParams(plp);
    //
    //previewWidth = plp.width;
    //previewHeight = plp.height;
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

    private FrameRenderer.Viewport drawViewport;

    public class ClearColor {
      public float r, g, b, a;
    }

    public ClearColor clearColor;

    public synchronized void setFrameRenderer(final RendererCreator rendererCreator) {
      queueEvent(() -> {
        FrameRenderer renderer1 = rendererCreator.createRenderer();

        if (renderer1 == null) {
          return;
        }

        myRenderer.release();
        myRenderer = renderer1;
        myRenderer.setTextureSize(viewWidth, viewHeight);
        myRenderer.setRotation((float) Math.PI / 2.0f);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Common.checkGLError("setFrameRenderer...");
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
      Timber.d("onSurfaceCreated...");

      GLES20.glDisable(GLES20.GL_DEPTH_TEST);
      GLES20.glDisable(GLES20.GL_STENCIL_TEST);

      textureID = genSurfaceTextureID();
      surfaceTexture = new SurfaceTexture(textureID);
      surfaceTexture.setOnFrameAvailableListener(this);

      FrameRenderer renderer =
          new CurveFilterCreator(FrameRendererToneCurve.CURVE_FILTERS[1]).createRenderer();

      if (!renderer.init(true)) {
        renderer.release();
        return;
      }

      myRenderer = renderer;

      myRenderer.setRotation((float) Math.PI / 2.0f);

      requestRender();

      onSurfaceTextureReady.onNext(surfaceTexture);
      startPreview();
    }

    public void startPreview() {
      final int rotationX;
      if (frontFacing) {
        rotationX = 180;
      } else {
        rotationX = 0;
      }
      post(() -> setRotationX(rotationX));
    }

    private void calcViewport() {
      drawViewport = new FrameRenderer.Viewport();

      drawViewport.width = viewWidth;
      drawViewport.height = viewHeight;
      drawViewport.x = 0;
      drawViewport.y = 0;
    }

    @Override public void onSurfaceChanged(GL10 gl, final int width, final int height) {
      Timber.d("onSurfaceChanged: %d x %d", width, height);

      GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);

      viewWidth = width;
      viewHeight = height;

      calcViewport();

      post(() -> {
        previewGLTexture.setPivotX(width / 2);
        previewGLTexture.setPivotY(height / 2);
        float scale = 1.0f * previewContainer.getWidth() / width;
        previewGLTexture.setScaleX(scale);
        previewGLTexture.setScaleY(scale);

        setX((previewContainer.getWidth() - width) / 2);
        setY((previewContainer.getHeight() - height) / 2);
      });
    }

    @Override public void onDrawFrame(GL10 gl) {
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

      myRenderer.renderTexture(textureID, drawViewport);

      //synchronized (frameListenerLock) {
      //  Timber.d("New Frame: " + System.currentTimeMillis());
      //  //onNewFrame.onNext(new Frame()); TODO SEND FRAME after glReadPixels
      //
      //}

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

  class CurveFilterCreator implements RendererCreator {
    private FrameRendererToneCurve.CurveFilter curveFilter;

    public CurveFilterCreator(FrameRendererToneCurve.CurveFilter curveFilter) {
      this.curveFilter = curveFilter;
    }

    @Override public FrameRenderer createRenderer() {
      FrameRendererToneCurve renderer = new FrameRendererToneCurve();
      try {
        renderer.setFromCurveFileInputStream(new BufferedInputStream(
            getResources().getAssets().open(curveFilter.getAssertFileName())));
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (renderer.init(true)) {
        return renderer;
      } else {
        return null;
      }
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<SurfaceTexture> onSurfaceTextureReady() {
    return onSurfaceTextureReady;
  }
}