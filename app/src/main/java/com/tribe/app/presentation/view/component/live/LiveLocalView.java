package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.coolcams.CoolCamsModel;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.view.LocalPeerView;
import com.tribe.tribelivesdk.view.PeerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveLocalView extends LiveStreamView {

  private static final int DURATION = 300;

  @Inject PaletteGrid paletteGrid;

  private LocalPeerView viewPeerLocal;

  // VARIABLES
  private boolean hiddenControls = false;
  private GestureDetectorCompat gestureDetector;
  private TribePeerMediaConfiguration localMediaConfiguration;

  // RESOURCES
  private int translationY;

  // OBSERVABLES
  private PublishSubject<TribePeerMediaConfiguration> onEnableCamera;
  private PublishSubject<TribePeerMediaConfiguration> onEnableMicro;
  private PublishSubject<Void> onSwitchCamera;
  private PublishSubject<Void> onSwitchFilter;
  private PublishSubject<Game> onStartGame;
  private PublishSubject<Void> onClick;
  private PublishSubject<Void> onStopGame;
  private PublishSubject<Void> onSwipeUp;

  public LiveLocalView(Context context) {
    super(context);
  }

  public LiveLocalView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public LiveLocalView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void init() {
    initResources();
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_local, this);
    unbinder = ButterKnife.bind(this);

    ViewCompat.setElevation(viewPeerOverlay, 0);

    gestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());

    localMediaConfiguration = new TribePeerMediaConfiguration(
        new TribeSession(TribeSession.PUBLISHER_ID, TribeSession.PUBLISHER_ID));
    viewPeerOverlay.initMediaConfiguration(localMediaConfiguration);

    viewPeerLocal = new LocalPeerView(getContext(), localMediaConfiguration);
    viewPeerLocal.setBackgroundColor(Color.BLACK);

    // We add the view in between the background and overlay
    layoutStream.addView(viewPeerLocal, 0,
        new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT,
            CardView.LayoutParams.MATCH_PARENT));

    onEnableCamera = PublishSubject.create();
    onEnableMicro = PublishSubject.create();
    onSwitchCamera = PublishSubject.create();
    onSwitchFilter = PublishSubject.create();
    onStartGame = PublishSubject.create();
    onClick = PublishSubject.create();
    onStopGame = PublishSubject.create();
    onSwipeUp = PublishSubject.create();

    viewPeerLocal.initEnableCameraSubscription(onEnableCamera);
    viewPeerLocal.initEnableMicroSubscription(onEnableMicro);
    viewPeerLocal.initSwitchCameraSubscription(onSwitchCamera);
    viewPeerLocal.initSwitchFilterSubscription(onSwitchFilter);
    viewPeerLocal.initStartGameSubscription(onStartGame);
    viewPeerLocal.initStopGameSubscription(onStopGame);

    viewPeerOverlay.setGuest(
        new TribeGuest(user.getId(), user.getDisplayName(), user.getProfilePicture(), false, false,
            user.getUsername(), user.getTrophy()));

    initSubscriptions();

    endInit();
  }

  @Override protected PeerView getPeerView() {
    return getLocalPeerView();
  }

  private void initSubscriptions() {
    subscriptions.add(Observable.merge(onEnableCamera, onEnableMicro)
        .debounce(1000, TimeUnit.MILLISECONDS)
        .distinct()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            mediaConfiguration -> viewPeerOverlay.setMediaConfiguration(mediaConfiguration)));
  }

  private void initResources() {
    translationY =
        getContext().getResources().getDimensionPixelOffset(R.dimen.top_bar_height_small);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void computeDisplay(boolean animate) {
    if (!localMediaConfiguration.isVideoEnabled()) {
      UIUtils.hideReveal(viewPeerLocal, animate, new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          if (animation != null) animation.removeAllListeners();
          viewPeerLocal.setVisibility(View.GONE);
        }
      });
    } else {
      UIUtils.showReveal(viewPeerLocal, animate, new AnimatorListenerAdapter() {
        @Override public void onAnimationStart(Animator animation) {
          viewPeerLocal.setVisibility(View.VISIBLE);
        }

        @Override public void onAnimationEnd(Animator animation) {
          if (animation != null) animation.removeAllListeners();
        }
      });
    }

    viewPeerOverlay.setMediaConfiguration(localMediaConfiguration);
  }

  /////////////////
  //   CLICKS    //
  /////////////////

  @Override public boolean onTouchEvent(MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    return super.onTouchEvent(event);
  }

  class GestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override public boolean onDoubleTap(MotionEvent e) {
      switchCamera();
      return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

      if (velocityY < 0) {
        onSwipeUp.onNext(null);
        return true;
      }

      return false;
    }

    @Override public boolean onSingleTapConfirmed(MotionEvent e) {
      onClick.onNext(null);
      return true;
    }
  }

  ////////////////
  // ANIMATIONS //
  ////////////////

  /////////////////
  //   PUBLIC    //
  /////////////////

  public void switchCamera() {
    if (!hiddenControls) {
      onSwitchCamera.onNext(null);
    }
  }

  public void switchFilter() {
    if (!hiddenControls) {
      onSwitchFilter.onNext(null);
    }
  }

  public void startGame(Game game) {
    onStartGame.onNext(game);
  }

  public void stopGame() {
    onStopGame.onNext(null);
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.unsubscribe();
    viewPeerLocal.dispose();
    viewPeerOverlay.dispose();
  }

  public LocalPeerView getLocalPeerView() {
    return viewPeerLocal;
  }

  public void hideControls(boolean hiddenControls) {
    this.hiddenControls = hiddenControls;
  }

  public void enableMicro(boolean isMicroActivated,
      @TribePeerMediaConfiguration.MediaConfigurationType String type) {
    localMediaConfiguration.setAudioEnabled(isMicroActivated);
    localMediaConfiguration.setMediaConfigurationType(type);
    onEnableMicro.onNext(localMediaConfiguration);
    computeDisplay(false);
  }

  public void enableCamera(boolean animate) {
    localMediaConfiguration.setVideoEnabled(true);
    onEnableCamera.onNext(localMediaConfiguration);
    computeDisplay(animate);
  }

  public void disableCamera(boolean animate,
      @TribePeerMediaConfiguration.MediaConfigurationType String type) {
    localMediaConfiguration.setVideoEnabled(false);
    localMediaConfiguration.setMediaConfigurationType(type);
    onEnableCamera.onNext(localMediaConfiguration);
    computeDisplay(animate);
  }

  public boolean isFrontFacing() {
    return viewPeerLocal.isFrontFacing();
  }

  public View getLayoutStream() {
    return layoutStream;
  }

  //////////////////
  //  OBSERVABLES //
  //////////////////

  public Observable<Void> onClick() {
    return onClick;
  }

  public Observable<Void> onSwipeUp() {
    return onSwipeUp;
  }
}
