package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.view.LocalPeerView;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveLocalView extends FrameLayout {

  private static final int DURATION = 300;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewPeerState) LivePeerStateView viewPeerState;

  private LocalPeerView viewPeerLocal;

  // VARIABLES
  private Unbinder unbinder;
  private boolean hiddenControls = false;
  private GestureDetectorCompat gestureDetector;
  private TribePeerMediaConfiguration localMediaConfiguration;

  // RESOURCES
  private int translationY;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<TribePeerMediaConfiguration> onEnableCamera = PublishSubject.create();
  private PublishSubject<TribePeerMediaConfiguration> onEnableMicro = PublishSubject.create();
  private PublishSubject<Void> onSwitchCamera = PublishSubject.create();
  private PublishSubject<Void> onClick = PublishSubject.create();

  public LiveLocalView(Context context) {
    super(context);
    init();
  }

  public LiveLocalView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveLocalView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public void init() {
    initResources();
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_local, this);
    unbinder = ButterKnife.bind(this);

    gestureDetector = new GestureDetectorCompat(getContext(), new TapGestureListener());

    localMediaConfiguration = new TribePeerMediaConfiguration(
        new TribeSession(TribeSession.PUBLISHER_ID, TribeSession.PUBLISHER_ID));

    viewPeerLocal = new LocalPeerView(getContext(), localMediaConfiguration);
    viewPeerLocal.setBackgroundColor(Color.BLACK);
    addView(viewPeerLocal, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    viewPeerLocal.initEnableCameraSubscription(onEnableCamera);
    viewPeerLocal.initEnableMicroSubscription(onEnableMicro);
    viewPeerLocal.initSwitchCameraSubscription(onSwitchCamera);

    viewPeerState.setGuest(
        new TribeGuest(user.getId(), user.getDisplayName(), user.getProfilePicture(), false, false,
            null, false));

    initSubscriptions();
  }

  private void initSubscriptions() {
    subscriptions.add(Observable.merge(onEnableCamera, onEnableMicro)
        .filter(mediaConfiguration -> (!mediaConfiguration.isVideoEnabled() || !mediaConfiguration.isAudioEnabled()))
        .subscribe(mediaConfiguration -> viewPeerState.setMediaConfiguration(mediaConfiguration)));
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

  public void switchCamera() {
    if (!hiddenControls) {
      onSwitchCamera.onNext(null);
    }
  }

  private void computeDisplay(boolean animate) {
    if (!localMediaConfiguration.isVideoEnabled() || !localMediaConfiguration.isAudioEnabled()) {
      UIUtils.showReveal(viewPeerState, animate, new AnimatorListenerAdapter() {
        @Override public void onAnimationStart(Animator animation) {
          animation.removeAllListeners();
          viewPeerState.setVisibility(View.VISIBLE);
        }
      });
    } else {
      UIUtils.hideReveal(viewPeerState, animate, new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          animation.removeAllListeners();
          viewPeerState.setVisibility(View.GONE);
        }
      });
    }
  }

  /////////////////
  //   CLICKS    //
  /////////////////

  @Override public boolean onTouchEvent(MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    return super.onTouchEvent(event);
  }

  class TapGestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override public boolean onDoubleTap(MotionEvent e) {
      switchCamera();
      return true;
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

  public void dispose() {
    if (subscriptions != null) subscriptions.unsubscribe();
    viewPeerLocal.onDestroy();
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

  //////////////////
  //  OBSERVABLES //
  //////////////////

  public Observable<Void> onClick() {
    return onClick;
  }
}
