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
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.view.LocalPeerView;
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

  @BindView(R.id.viewAudio) LiveAudioView viewAudio;

  @BindView(R.id.backLiveLocalView) FrameLayout backLiveLocalView;

  @BindView(R.id.backMicroDisabled) View backMicroDisabled;

  @BindView(R.id.imgMicroDisabled) ImageView imgMicroDisabled;

  @BindView(R.id.txtLowConnectivity) TextViewFont txtLowConnectivity;

  private LocalPeerView viewPeerLocal;

  // VARIABLES
  private Unbinder unbinder;
  private boolean hiddenControls = false;
  private boolean cameraEnabled = true;
  private GestureDetectorCompat gestureDetector;

  // RESOURCES
  private int translationY;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onEnableCamera = PublishSubject.create();
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

    viewPeerLocal = new LocalPeerView(getContext());
    viewPeerLocal.setBackgroundColor(Color.BLACK);
    addView(viewPeerLocal, 1, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    viewPeerLocal.initEnableCameraSubscription(onEnableCamera);
    viewPeerLocal.initSwitchCameraSubscription(onSwitchCamera);

    viewAudio.setGuest(
        new TribeGuest(user.getId(), user.getDisplayName(), user.getProfilePicture(), false, false,
            null));

    txtLowConnectivity.setTranslationY(-translationY);
    txtLowConnectivity.setText(
        EmojiParser.demojizedText(getContext().getString(R.string.live_low_connectivity)));

    initSubscriptions();
  }

  private void initSubscriptions() {
    subscriptions.add(viewPeerLocal.onShouldSwitchMode()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(mediaConfiguration -> {
          if (!cameraEnabled && mediaConfiguration.isVideoEnabled()) {
            enableCamera(true);

            if (mediaConfiguration.isLowConnectivityMode()
                && txtLowConnectivity.getTranslationX() == 0) {
              txtLowConnectivity.animate()
                  .translationY(-translationY)
                  .setDuration(DURATION)
                  .setInterpolator(new OvershootInterpolator(1f))
                  .start();
            }
          } else if (cameraEnabled && !mediaConfiguration.isVideoEnabled()) {
            disableCamera(true);

            if (mediaConfiguration.isLowConnectivityMode()) {
              txtLowConnectivity.animate()
                  .translationY(0)
                  .setDuration(DURATION)
                  .setInterpolator(new OvershootInterpolator(1f))
                  .start();
            }
          }
        }));
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

  /////////////////
  //   CLICKS    //
  /////////////////

  ////////////////
  // ANIMATIONS //
  ////////////////

  public void enableMicro(boolean isMicroActivated, boolean isCameraActivated) {
    backMicroDisabled.setVisibility(GONE);
    if (!isMicroActivated) {
      backLiveLocalView.setVisibility(VISIBLE);
      imgMicroDisabled.setVisibility(VISIBLE);

      if (!isCameraActivated) {
        backLiveLocalView.setVisibility(GONE);
        backMicroDisabled.setVisibility(VISIBLE);
      }
    } else {
      backLiveLocalView.setVisibility(GONE);
      imgMicroDisabled.setVisibility(GONE);
    }
  }

  public void enableCamera(boolean animate) {
    cameraEnabled = true;
    onEnableCamera.onNext(cameraEnabled);

    UIUtils.showReveal(viewPeerLocal, animate, new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        viewAudio.setVisibility(View.GONE);
      }

      @Override public void onAnimationStart(Animator animation) {
        viewPeerLocal.setVisibility(View.VISIBLE);
      }
    });
  }

  public void disableCamera(boolean animate) {
    cameraEnabled = false;
    onEnableCamera.onNext(cameraEnabled);

    UIUtils.hideReveal(viewPeerLocal, animate, new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        viewAudio.setVisibility(View.VISIBLE);
      }

      @Override public void onAnimationEnd(Animator animation) {
        viewPeerLocal.setVisibility(View.GONE);
      }
    });
  }

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

  //////////////////
  //  OBSERVABLES //
  //////////////////

  public Observable<Void> onClick() {
    return onClick;
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
}
