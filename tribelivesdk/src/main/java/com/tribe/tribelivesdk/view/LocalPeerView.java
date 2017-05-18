package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.util.AttributeSet;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import java.util.concurrent.TimeUnit;
import org.webrtc.VideoRenderer;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LocalPeerView extends PeerView {

  private Observable<Void> onSwitchCamera;
  private Observable<Void> onSwitchFilter;
  private Observable<TribePeerMediaConfiguration> onEnableCamera;
  private Observable<TribePeerMediaConfiguration> onEnableMicro;
  private boolean frontFacing = true;
  private TribePeerMediaConfiguration mediaConfiguration;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<TribePeerMediaConfiguration> shouldSwitchMode = PublishSubject.create();

  public LocalPeerView(Context context) {
    super(context);
    initVideoRenderer();
  }

  public LocalPeerView(Context context, TribePeerMediaConfiguration mediaConfiguration) {
    super(context);
    this.mediaConfiguration = mediaConfiguration;
    initVideoRenderer();
  }

  public LocalPeerView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    initVideoRenderer();
  }

  public void dispose() {
    super.dispose();
    if (subscriptions != null) subscriptions.unsubscribe();
  }

  private void initVideoRenderer() {
    TextureViewRenderer textureViewRenderer = getTextureViewRenderer();
    textureViewRenderer.init(null, rendererEvents);
    videoRenderer = new VideoRenderer(textureViewRenderer);
    setMirror(true);
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void initEnableCameraSubscription(Observable<TribePeerMediaConfiguration> obs) {
    onEnableCamera = obs;
  }

  public void initEnableMicroSubscription(Observable<TribePeerMediaConfiguration> obs) {
    onEnableMicro = obs;
  }

  public void initSwitchCameraSubscription(Observable<Void> obs) {
    onSwitchCamera = obs;
    subscriptions.add(onSwitchCamera.doOnNext(aVoid -> frontFacing = !frontFacing)
        .delay(200, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> setMirror(frontFacing)));
  }

  public void initSwitchFilterSubscription(Observable<Void> obs) {
    onSwitchFilter = obs;
  }

  public TribePeerMediaConfiguration getMediaConfiguration() {
    return mediaConfiguration;
  }

  public boolean isFrontFacing() {
    return frontFacing;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<TribePeerMediaConfiguration> onEnableCamera() {
    return onEnableCamera;
  }

  public Observable<TribePeerMediaConfiguration> onEnableMicro() {
    return onEnableMicro;
  }

  public Observable<Void> onSwitchCamera() {
    return onSwitchCamera;
  }

  public Observable<Void> onSwitchFilter() {
    return onSwitchFilter;
  }
}
