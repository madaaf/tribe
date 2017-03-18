package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveControlsView extends FrameLayout {

  private static final int DURATION = 300;
  private static final int DURATION_PARAM = 450;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.btnInviteLive) View btnInviteLive;

  @BindView(R.id.btnNotify) View btnNotify;

  @BindView(R.id.btnCameraOn) View btnCameraOn;

  @BindView(R.id.btnCameraOff) View btnCameraOff;

  @BindView(R.id.btnFilter) View btnFilter;

  @BindView(R.id.btnExpand) ImageView btnExpand;

  @BindView(R.id.btnOrientationCamera) View btnOrientationCamera;

  @BindView(R.id.btnMicro) ImageView btnMicro;

  @BindView(R.id.layoutContainerParamLive) FrameLayout layoutContainerParamLive;

  @BindView(R.id.layoutContainerParamExtendedLive) LinearLayout layoutContainerParamExtendedLive;

  // VARIABLES
  private Unbinder unbinder;
  private ObjectAnimator animatorRotation;
  private boolean cameraEnabled = true, microEnabled = true, isParamExpanded = false;
  private float xTranslation;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
  private PublishSubject<Void> onClickCameraOrientation = PublishSubject.create();
  private PublishSubject<Boolean> onClickMicro = PublishSubject.create();
  private PublishSubject<Void> onClickParamExpand = PublishSubject.create();
  private PublishSubject<Void> onClickCameraEnable = PublishSubject.create();
  private PublishSubject<Void> onClickCameraDisable = PublishSubject.create();
  private PublishSubject<Void> onClickNotify = PublishSubject.create();
  private PublishSubject<Void> onNotifyAnimationDone = PublishSubject.create();

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
    xTranslation = getResources().getDimension(R.dimen.nav_icon_size);
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

  private void expandParam() {
    if (!isParamExpanded) {
      isParamExpanded = true;

      int widthExtended = layoutContainerParamExtendedLive.getWidth();
      layoutContainerParamExtendedLive.setTranslationX(-(screenUtils.getWidthPx()));
      layoutContainerParamExtendedLive.setVisibility(VISIBLE);

      btnExpand.setImageResource(R.drawable.picto_extend_left_live);

      setXTranslateAnimation(layoutContainerParamExtendedLive, 0);
      setXTranslateAnimation(layoutContainerParamLive, widthExtended);

      if (cameraEnabled) {
        setXTranslateAnimation(btnExpand, widthExtended);
      } else {
        setXTranslateAnimation(btnExpand, 3 * xTranslation);
      }
    } else {
      isParamExpanded = false;
      layoutContainerParamExtendedLive.setTranslationX(0);
      btnExpand.setImageResource(R.drawable.picto_extend_right_live);

      setXTranslateAnimation(layoutContainerParamExtendedLive, -screenUtils.getWidthPx());
      setXTranslateAnimation(layoutContainerParamLive, 0);
      setXTranslateAnimation(btnExpand, 0);
    }
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
  }

  @OnClick(R.id.btnOrientationCamera) void clickOrientationCamera() {
    btnOrientationCamera.animate()
        .rotation(btnOrientationCamera.getRotation() == 0 ? 180 : 0)
        .setDuration(DURATION)
        .start();
    onClickCameraOrientation.onNext(null);
  }

  @OnClick(R.id.btnMicro) void clickMicro() {
    microEnabled = !microEnabled;
    onClickMicro.onNext(microEnabled);
  }

  @OnClick(R.id.btnExpand) void clickExpandParam() {
    expandParam();
  }

  @OnClick(R.id.btnCameraOn) void clickCameraEnable() {
    cameraEnabled = false;
    btnCameraOn.setVisibility(GONE);
    btnCameraOff.setVisibility(VISIBLE);

    Animation scaleAnimation =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.scale_disappear);

    btnFilter.setAnimation(scaleAnimation);
    btnOrientationCamera.setAnimation(scaleAnimation);

    scaleAnimation.setAnimationListener(new AnimationListenerAdapter() {

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        setXTranslateAnimation(btnMicro, -xTranslation);
        setXTranslateAnimation(btnExpand, 3 * xTranslation);
      }
    });

    onClickCameraEnable.onNext(null);
  }

  @OnClick(R.id.btnCameraOff) void clickCameraDisable() {
    cameraEnabled = true;
    btnCameraOff.setVisibility(GONE);
    btnCameraOn.setVisibility(VISIBLE);

    Animation scaleAnimation =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.scale_appear);

    layoutContainerParamLive.setVisibility(VISIBLE);

    setXTranslateAnimation(btnMicro, 0);
    setXTranslateAnimation(btnExpand, layoutContainerParamExtendedLive.getWidth());

    btnOrientationCamera.setAnimation(scaleAnimation);
    btnFilter.setAnimation(scaleAnimation);

    onClickCameraDisable.onNext(null);
  }

  @OnClick(R.id.btnNotify) void clickNotify() {
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

    if (animatorRotation != null) animatorRotation.cancel();
  }

  public void setNotifyEnabled(boolean enable) {
    btnNotify.setEnabled(enable);
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
      btnNotify.animate()
          .alpha(1f)
          .scaleX(1.25f)
          .scaleY(1.25f)
          .translationY(-screenUtils.dpToPx(10))
          .rotation(10)
          .setDuration(DURATION)
          .setInterpolator(new DecelerateInterpolator())
          .setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
              if (btnNotify == null) return;

              btnNotify.animate().setListener(null);

              animatorRotation = ObjectAnimator.ofFloat(btnNotify, ROTATION, 7, -7);
              animatorRotation.setDuration(100);
              animatorRotation.setRepeatCount(3);
              animatorRotation.setRepeatMode(ValueAnimator.REVERSE);
              animatorRotation.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                  animatorRotation.removeAllListeners();

                  if (btnNotify != null) {
                    btnNotify.animate()
                        .scaleX(1)
                        .scaleY(1)
                        .rotation(0)
                        .translationY(0)
                        .setDuration(DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                          @Override public void onAnimationEnd(Animator animation) {
                            if (btnNotify != null) {
                              btnNotify.setEnabled(true);
                              btnNotify.animate().setListener(null);
                            }
                          }

                          @Override public void onAnimationCancel(Animator animation) {
                            if (btnNotify != null) btnNotify.animate().setListener(null);
                          }
                        });
                  }
                }

                @Override public void onAnimationCancel(Animator animation) {
                  animatorRotation.removeAllListeners();
                }
              });

              animatorRotation.start();
            }
          })
          .start();
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

  public Observable<Void> onClickParamExpand() {
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
