package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
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
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.view.LocalPeerView;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveLocalView extends FrameLayout {

  private static final int DURATION = 300;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject PaletteGrid paletteGrid;

  @BindView(R.id.viewPeerOverlay) LivePeerOverlayView viewPeerOverlay;

  @BindView(R.id.viewShareOverlay) LiveShareOverlayView viewShareOverlay;

  @BindView(R.id.cardViewStreamLayout) CardView cardViewStreamLayout;

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

    cardViewStreamLayout.setPreventCornerOverlap(false);
    cardViewStreamLayout.setMaxCardElevation(0);
    cardViewStreamLayout.setCardElevation(0);
    ViewCompat.setElevation(viewPeerOverlay, 0);

    gestureDetector = new GestureDetectorCompat(getContext(), new TapGestureListener());

    localMediaConfiguration = new TribePeerMediaConfiguration(
        new TribeSession(TribeSession.PUBLISHER_ID, TribeSession.PUBLISHER_ID));
    viewPeerOverlay.initMediaConfiguration(localMediaConfiguration);

    viewPeerLocal = new LocalPeerView(getContext(), localMediaConfiguration);
    viewPeerLocal.setBackgroundColor(PaletteGrid.getRandomColorExcluding(Color.BLACK));

    // We add the view in between the background and overlay
    cardViewStreamLayout.addView(viewPeerLocal,
        new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));

    viewPeerLocal.initEnableCameraSubscription(onEnableCamera);
    viewPeerLocal.initEnableMicroSubscription(onEnableMicro);
    viewPeerLocal.initSwitchCameraSubscription(onSwitchCamera);

    viewPeerOverlay.setGuest(
        new TribeGuest(user.getId(), user.getDisplayName(), user.getProfilePicture(), false, false,
            null, false, user.getUsername()));

    initSubscriptions();
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
      UIUtils.hideReveal(cardViewStreamLayout, animate, new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          if (animation != null) animation.removeAllListeners();
          cardViewStreamLayout.setVisibility(View.GONE);
        }
      });
    } else {
      UIUtils.showReveal(cardViewStreamLayout, animate, new AnimatorListenerAdapter() {
        @Override public void onAnimationStart(Animator animation) {
          cardViewStreamLayout.setVisibility(View.VISIBLE);
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

  public void switchCamera() {
    if (!hiddenControls) {
      onSwitchCamera.onNext(null);
    }
  }

  public void computeAlpha(float alpha) {
    viewShareOverlay.setAlpha(alpha);
  }

  public void hideShareOverlay() {
    viewShareOverlay.hide();
  }

  public void showShareOverlay() {
    viewShareOverlay.show();
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

  //////////////////
  //  OBSERVABLES //
  //////////////////

  public Observable<Void> onClick() {
    return onClick;
  }

  public Observable<Void> onShare() {
    return viewShareOverlay.onShare();
  }
}
