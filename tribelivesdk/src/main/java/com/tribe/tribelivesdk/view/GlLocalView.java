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
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.view.opengl.GlCameraPreview;
import com.tribe.tribelivesdk.webrtc.FrameTexture;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class GlLocalView extends FrameLayout {

  private Unbinder unbinder;
  private GlCameraPreview glCameraPreview;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<SurfaceTexture> onSurfaceTextureReady = PublishSubject.create();
  private PublishSubject<FrameTexture> onFrameAvailable = PublishSubject.create();

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
    subscriptions.add(glCameraPreview.onFrameAvailable().subscribe(onFrameAvailable));
  }

  private void initResources() {

  }

  private void initDependencyInjector() {

  }

  public void initOnNewCameraInfo(Observable<CameraInfo> obs) {
    subscriptions.add(obs.observeOn(AndroidSchedulers.mainThread())
        .subscribe(cameraInfo -> onNewCameraInfo(cameraInfo)));
  }

  private void onNewCameraInfo(CameraInfo cameraInfo) {
    glCameraPreview.updateCameraInfo(cameraInfo);
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