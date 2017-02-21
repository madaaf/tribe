package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.util.AttributeSet;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import org.webrtc.VideoRenderer;
import rx.Observable;
import rx.subjects.PublishSubject;

public class LocalPeerView extends PeerView {

  private Observable<Void> onSwitchCamera;
  private Observable<Boolean> onEnableCamera;
  private PublishSubject<TribePeerMediaConfiguration> shouldSwitchMode = PublishSubject.create();

  public LocalPeerView(Context context) {
    super(context);
    initVideoRenderer();
  }

  public LocalPeerView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    initVideoRenderer();
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

  public void initEnableCameraSubscription(Observable<Boolean> obs) {
    onEnableCamera = obs;
  }

  public void initSwitchCameraSubscription(Observable<Void> obs) {
    onSwitchCamera = obs;
  }

  public void shouldSwitchMode(TribePeerMediaConfiguration tribePeerMediaConfiguration) {
    shouldSwitchMode.onNext(tribePeerMediaConfiguration);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Boolean> onEnableCamera() {
    return onEnableCamera;
  }

  public Observable<Void> onSwitchCamera() {
    return onSwitchCamera;
  }

  public Observable<TribePeerMediaConfiguration> onShouldSwitchMode() {
    return shouldSwitchMode;
  }
}
