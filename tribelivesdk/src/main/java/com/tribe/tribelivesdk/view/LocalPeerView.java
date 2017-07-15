package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.tribe.tribelivesdk.entity.CameraInfo;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.view.opengl.filter.FilterMask;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LocalPeerView extends FrameLayout {

  // VARIABLES
  private boolean frontFacing = true;
  private TribePeerMediaConfiguration mediaConfiguration;
  private GlLocalView glLocalView;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Observable<Void> onSwitchCamera;
  private Observable<FilterMask> onSwitchFilter;
  private Observable<Game> onStartGame;
  private Observable<Void> onStopGame;
  private Observable<TribePeerMediaConfiguration> onEnableCamera;
  private Observable<TribePeerMediaConfiguration> onEnableMicro;
  private PublishSubject<TribePeerMediaConfiguration> shouldSwitchMode = PublishSubject.create();
  private PublishSubject<Pair<Integer, Integer>> onPreviewSizeChanged = PublishSubject.create();

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
    initGlLocalView();
  }

  private void initGlLocalView() {
    glLocalView = new GlLocalView(getContext());
    addView(glLocalView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
  }

  public void initEnableCameraSubscription(Observable<TribePeerMediaConfiguration> obs) {
    onEnableCamera = obs;
  }

  public void initEnableMicroSubscription(Observable<TribePeerMediaConfiguration> obs) {
    onEnableMicro = obs;
  }

  public void initSwitchCameraSubscription(Observable<Void> obs) {
    onSwitchCamera = obs;
    //subscriptions.add(onSwitchCamera.doOnNext(aVoid -> frontFacing = !frontFacing)
    //    .delay(200, TimeUnit.MILLISECONDS)
    //    .observeOn(AndroidSchedulers.mainThread())
    //    .subscribe(aVoid -> setMirror(frontFacing)));
  }

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    glLocalView.initInviteOpenSubscription(obs);
  }

  public void initSwitchFilterSubscription(Observable<FilterMask> obs) {
    onSwitchFilter = obs;
    glLocalView.initSwitchFilterSubscription(onSwitchFilter);
  }

  public void initStartGameSubscription(Observable<Game> obs) {
    onStartGame = obs;
  }

  public void initStopGameSubscription(Observable<Void> obs) {
    onStopGame = obs;
  }

  public void initOnNewCameraInfo(Observable<CameraInfo> obs) {
    glLocalView.initOnNewCameraInfo(obs);
  }

  public TribePeerMediaConfiguration getMediaConfiguration() {
    return mediaConfiguration;
  }

  public boolean isFrontFacing() {
    return frontFacing;
  }

  public GlLocalView getGlLocalView() {
    return glLocalView;
  }

  public boolean isFreeze() {
    // TODO REDO
    return false;
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

  public Observable<FilterMask> onSwitchFilter() {
    return onSwitchFilter;
  }

  public Observable<Game> onStartGame() {
    return onStartGame;
  }

  public Observable<Void> onStopGame() {
    return onStopGame;
  }

  public Observable<Pair<Integer, Integer>> onPreviewSizeChanged() {
    return onPreviewSizeChanged;
  }
}
