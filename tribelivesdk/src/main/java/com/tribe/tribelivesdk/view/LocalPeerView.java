package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.util.AttributeSet;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.webrtc.TribeVideoRenderer;
import java.util.concurrent.TimeUnit;
import org.webrtc.VideoRenderer;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class LocalPeerView extends PeerView {

  // VARIABLES
  private boolean frontFacing = true;
  private TribePeerMediaConfiguration mediaConfiguration;
  private TribeVideoRenderer localRenderer;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Observable<Void> onSwitchCamera;
  private Observable<Void> onSwitchFilter;
  private Observable<Game> onStartGame;
  private Observable<TribePeerMediaConfiguration> onEnableCamera;
  private Observable<TribePeerMediaConfiguration> onEnableMicro;
  private PublishSubject<TribePeerMediaConfiguration> shouldSwitchMode = PublishSubject.create();

  public LocalPeerView(Context context) {
    super(context);
    init();
  }

  public LocalPeerView(Context context, TribePeerMediaConfiguration mediaConfiguration) {
    super(context);
    this.mediaConfiguration = mediaConfiguration;
    init();
  }

  public LocalPeerView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    init();
  }

  private void init() {
    TextureViewRenderer textureViewRenderer = getTextureViewRenderer();
    textureViewRenderer.init(null, rendererEvents);
    initRemoteRenderer();
    initLocalRenderer();
    setMirror(true);
  }

  public void initRemoteRenderer() {
    remoteRenderer = new VideoRenderer(textureViewRenderer);
  }

  private void initLocalRenderer() {
    localRenderer = new TribeVideoRenderer(textureViewRenderer);
  }

  protected void removeLocalRenderer() {
    if (localRenderer != null) {
      Timber.d("localRenderer dispose");
      localRenderer.dispose();
      localRenderer = null;
    }
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void dispose() {
    super.dispose();
    removeLocalRenderer();
    if (subscriptions != null) subscriptions.clear();
  }

  @Override public void onFirstFrameRendered() {
  }

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

  public void initStartGameSubscription(Observable<Game> obs) {
    onStartGame = obs;
  }

  public TribePeerMediaConfiguration getMediaConfiguration() {
    return mediaConfiguration;
  }

  public boolean isFrontFacing() {
    return frontFacing;
  }

  public TribeVideoRenderer getLocalRenderer() {
    return localRenderer;
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

  public Observable<Game> onStartGame() {
    return onStartGame;
  }
}
