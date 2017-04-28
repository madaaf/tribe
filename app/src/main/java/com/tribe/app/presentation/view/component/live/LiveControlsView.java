package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveControlsView extends FrameLayout {
  private final static int MAX_DURATION_LAYOUT_CONTROLS = 5;
  private static final int DURATION = 300;
  private static final int DURATION_PARAM = 450;

  @Inject ScreenUtils screenUtils;

  @Inject StateManager stateManager;

  @BindView(R.id.btnInviteLive) View btnInviteLive;

  @BindView(R.id.btnNotify) View btnNotify;

  @BindView(R.id.btnCameraOn) View btnCameraOn;

  @BindView(R.id.btnCameraOff) View btnCameraOff;

/*  @BindView(R.id.btnFilter) View btnFilter;*/

  @BindView(R.id.btnExpand) ImageView btnExpand;

  @BindView(R.id.btnOrientationCamera) View btnOrientationCamera;

  @BindView(R.id.btnMicro) ImageView btnMicro;

  @BindView(R.id.layoutContainerParamLive) FrameLayout layoutContainerParamLive;

  @BindView(R.id.layoutContainerParamExtendedLive) LinearLayout layoutContainerParamExtendedLive;

  // VARIABLES
  private Unbinder unbinder;
  private boolean cameraEnabled = true, microEnabled = true, isParamExpanded = false;
  private float xTranslation;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
  private PublishSubject<Void> onClickCameraOrientation = PublishSubject.create();
  private PublishSubject<Boolean> onClickMicro = PublishSubject.create();
  private PublishSubject<Boolean> onClickParamExpand = PublishSubject.create();
  private PublishSubject<Void> onClickCameraEnable = PublishSubject.create();
  private PublishSubject<Void> onClickCameraDisable = PublishSubject.create();
  private PublishSubject<Void> onClickNotify = PublishSubject.create();
  private PublishSubject<Void> onNotifyAnimationDone = PublishSubject.create();
  private Subscription timerSubscription;

  public LiveControlsView(Context context) {
    super(context);
    init();
  }

  public LiveControlsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveControlsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();
    if (timerSubscription != null) {
      timerSubscription.unsubscribe();
      timerSubscription = null;
    }
    super.onDetachedFromWindow();
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_controls, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);
    initUI();
  }

  private void initUI() {
    xTranslation = getResources().getDimension(R.dimen.nav_icon_size) + screenUtils.dpToPx(10);
    btnNotify.setEnabled(false);
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

  private void setTimer() {
    if (timerSubscription != null) timerSubscription.unsubscribe();

    timerSubscription = Observable.timer(MAX_DURATION_LAYOUT_CONTROLS, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> {
          reduceParam();
        });
  }

  private void resetTimer() {
    if (timerSubscription != null) timerSubscription.unsubscribe();
    setTimer();
  }

  private void clickOnParam() {
    if (!isParamExpanded) {
      expendParam();
    } else {
      reduceParam();
    }
  }

  private void expendParam() {
    resetTimer();
    isParamExpanded = true;

    onClickParamExpand.onNext(isParamExpanded);

    int widthExtended = layoutContainerParamExtendedLive.getWidth();
    layoutContainerParamExtendedLive.setTranslationX(-(screenUtils.getWidthPx()));
    layoutContainerParamExtendedLive.setVisibility(VISIBLE);

    btnExpand.setImageResource(R.drawable.picto_extend_left_live);

    setXTranslateAnimation(layoutContainerParamExtendedLive, 0);
    setXTranslateAnimation(layoutContainerParamLive, getWidth());

    if (cameraEnabled) {
      setXTranslateAnimation(btnExpand, widthExtended);
    } else {
      setXTranslateAnimation(btnExpand, layoutContainerParamExtendedLive.getWidth() - xTranslation);
    }
  }

  public void reduceParam() {
    isParamExpanded = false;
    onClickParamExpand.onNext(isParamExpanded);
    layoutContainerParamExtendedLive.setTranslationX(0);
    btnExpand.setImageResource(R.drawable.picto_extend_right_live);

    setXTranslateAnimation(layoutContainerParamExtendedLive, -screenUtils.getWidthPx());
    setXTranslateAnimation(layoutContainerParamLive, 0);
    setXTranslateAnimation(btnExpand, 0);
  }

  private ViewPropertyAnimator setXTranslateAnimation(View view, float translation) {
    ViewPropertyAnimator xAnim = view.animate();
    xAnim.translationX(translation)
        .setInterpolator(new OvershootInterpolator(0.45f))
        .alpha(1)
        .setDuration(DURATION_PARAM)
        .setListener(null)
        .start();

    return xAnim;
  }

  ///////////////
  //  ONCLICK  //
  ///////////////

  @OnClick(R.id.btnInviteLive) void openInvite() {
    onOpenInvite.onNext(null);
    resetTimer();
  }

  @OnClick(R.id.btnOrientationCamera) void clickOrientationCamera() {
    btnOrientationCamera.animate()
        .rotation(btnOrientationCamera.getRotation() == 0 ? 180 : 0)
        .setDuration(DURATION)
        .start();
    onClickCameraOrientation.onNext(null);
    resetTimer();
  }

  @OnClick(R.id.btnMicro) void clickMicro() {
    microEnabled = !microEnabled;
    onClickMicro.onNext(microEnabled);
    resetTimer();
  }

  @OnClick(R.id.btnExpand) void clickExpandParam() {
    clickOnParam();
  }

  @OnClick(R.id.btnCameraOn) void clickCameraEnable() {
    resetTimer();

    cameraEnabled = false;
    btnCameraOn.setVisibility(GONE);
    btnCameraOff.setVisibility(VISIBLE);

    Animation scaleAnimation =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.scale_disappear);

    //btnFilter.setAnimation(scaleAnimation);
    btnOrientationCamera.setAnimation(scaleAnimation);

    subscriptions.add(Observable.timer(scaleAnimation.getDuration() / 3, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          setXTranslateAnimation(btnMicro, -xTranslation);
          setXTranslateAnimation(btnExpand,
              layoutContainerParamExtendedLive.getWidth() - xTranslation);
        }));

    onClickCameraEnable.onNext(null);
  }

  @OnClick(R.id.btnCameraOff) void clickCameraDisable() {
    resetTimer();

    cameraEnabled = true;
    btnCameraOff.setVisibility(GONE);
    btnCameraOn.setVisibility(VISIBLE);

    Animation scaleAnimation =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.scale_appear);

    layoutContainerParamLive.setVisibility(VISIBLE);

    setXTranslateAnimation(btnMicro, 0);
    setXTranslateAnimation(btnExpand, layoutContainerParamExtendedLive.getWidth());

    btnOrientationCamera.setAnimation(scaleAnimation);
    // btnFilter.setAnimation(scaleAnimation);

    onClickCameraDisable.onNext(null);
  }

  @OnClick(R.id.btnNotify) void clickNotify() {
    stateManager.addTutorialKey(StateManager.BUZZ_FRIEND_POPUP);
    resetTimer();

    btnNotify.setEnabled(false);
    btnNotify.animate()
        .alpha(0.2f)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            onNotifyAnimationDone.onNext(null);
            btnNotify.animate().setListener(null);
          }
        })
        .start();

    onClickNotify.onNext(null);
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void dispose() {
    btnNotify.clearAnimation();
    btnNotify.animate().setListener(null);
  }

  public void setNotifyEnabled(boolean enable) {
    btnNotify.setEnabled(enable);
  }

  public void prepareForScreenshot() {
    btnNotify.setAlpha(0f);
  }

  public void screenshotDone() {
    btnNotify.setAlpha(1f);
  }

  public void setMicroEnabled(boolean enabled) {
    btnMicro.setImageResource(
        enabled ? R.drawable.picto_micro_on_live : R.drawable.picto_micro_off_live);
  }

  public void refactorNotifyButton(boolean enable) {
    if (!enable) {
      btnNotify.setVisibility(View.GONE);
      return;
    } else {
      btnNotify.setVisibility(View.VISIBLE);
    }

    if (enable != btnNotify.isEnabled()) {
      btnNotify.animate().alpha(1).setDuration(DURATION);
      btnNotify.setEnabled(true);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onOpenInvite() {
    return onOpenInvite;
  }

  public Observable<Void> onClickCameraOrientation() {
    return onClickCameraOrientation;
  }

  public Observable<Boolean> onClickMicro() {
    return onClickMicro;
  }

  public Observable<Boolean> onClickParamExpand() {
    return onClickParamExpand;
  }

  public Observable<Void> onClickCameraEnable() {
    return onClickCameraEnable;
  }

  public Observable<Void> onClickCameraDisable() {
    return onClickCameraDisable;
  }

  public Observable<Void> onClickNotify() {
    return onClickNotify;
  }

  public Observable<Void> onNotifyAnimationDone() {
    return onNotifyAnimationDone;
  }
}
