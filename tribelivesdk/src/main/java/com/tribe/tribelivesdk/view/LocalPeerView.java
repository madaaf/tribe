package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.util.AttributeSet;
import org.webrtc.VideoRenderer;
import rx.Observable;

public class LocalPeerView extends PeerView {

  private Observable<Void> onSwitchCamera;
  private Observable<Boolean> onEnableCamera;

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

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Boolean> onEnableCamera() {
    return onEnableCamera;
  }

  public Observable<Void> onSwitchCamera() {
    return onSwitchCamera;
  }
}
