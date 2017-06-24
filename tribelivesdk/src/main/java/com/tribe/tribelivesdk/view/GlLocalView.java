package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.view.opengl.GlCameraPreview;
import com.tribe.tribelivesdk.webrtc.Frame;
import org.webrtc.CameraEnumerationAndroid;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class GlLocalView extends FrameLayout {

  private Unbinder unbinder;
  private GlCameraPreview glCameraPreview;

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

    glCameraPreview = new GlCameraPreview(getContext(), null);
    addView(glCameraPreview, ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    subscriptions.add(glCameraPreview.onSurfaceTextureReady().subscribe(onSurfaceTextureReady));
  }

  private void initResources() {

  }

  private void initDependencyInjector() {

  }

  public void initOnNewCaptureFormat(Observable<CameraEnumerationAndroid.CaptureFormat> obs) {
    subscriptions.add(obs.observeOn(AndroidSchedulers.mainThread())
        .subscribe(captureFormat -> onCameraCaptureFormatChange(captureFormat)));
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

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<SurfaceTexture> onSurfaceTextureReady() {
    return onSurfaceTextureReady;
  }
}