package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.listener.AnimatorCancelListener;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/05/2017.
 */
public class LiveDropZoneView extends RelativeLayout {

  private static final int DURATION = 300;
  private static final int DURATION_SLOW = 1000;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgArrow) ImageView imgArrow;
  @BindView(R.id.viewRing) View viewRing;
  @BindView(R.id.txtLabelDrop) TextViewFont txtLabelDrop;

  // VARIABLES
  private AnimatorSet animatorFloating;
  private AnimatorSet animatorScale;
  private int[] locationOfRing;

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveDropZoneView(Context context) {
    super(context);
  }

  public LiveDropZoneView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_drop_zone, this);
    unbinder = ButterKnife.bind(this);

    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
    screenUtils = applicationComponent.screenUtils();

    initResources();
    initUI();
    initSubscriptions();
  }

  public void dispose() {

  }

  private void initUI() {
    setAlpha(0);
    setVisibility(GONE);
    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black_opacity_50));
  }

  private void initResources() {
    locationOfRing = new int[2];
  }

  private void initSubscriptions() {

  }

  ///////////////////////
  //      PUBLIC       //
  ///////////////////////

  public void applyTranslationX(float x) {
    viewRing.setTranslationX(-x / 2);
    imgArrow.setTranslationX(-x / 2);
    txtLabelDrop.setTranslationX(-x / 2);
    setTranslationX(x);
  }

  public void show() {
    if (getVisibility() == View.VISIBLE) return;

    imgArrow.setTranslationY(0);
    viewRing.setScaleX(0);
    viewRing.setScaleY(0);
    setVisibility(VISIBLE);

    animate().alpha(1).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION).start();

    animatorScale = new AnimatorSet();
    ValueAnimator animatorScaleUp = ValueAnimator.ofFloat(0, 2f);
    animatorScaleUp.setDuration(DURATION >> 1);
    animatorScaleUp.setInterpolator(new DecelerateInterpolator());
    animatorScaleUp.addUpdateListener(valueAnimator -> {
      float value = (float) valueAnimator.getAnimatedValue();
      viewRing.setScaleX(value);
      viewRing.setScaleY(value);
    });

    ValueAnimator animatorScaleDown = ValueAnimator.ofFloat(2f, 1);
    animatorScaleDown.setDuration(DURATION).setInterpolator(new DecelerateInterpolator());
    animatorScaleDown.addUpdateListener(valueAnimator -> {
      float value = (float) valueAnimator.getAnimatedValue();
      viewRing.setScaleX(value);
      viewRing.setScaleY(value);
    });

    animatorScale.playSequentially(animatorScaleUp, animatorScaleDown);
    animatorScale.start();

    subscriptions.add(Observable.timer((DURATION >> 1) + 50, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> startAnimations()));
  }

  public void hide() {
    if (getVisibility() == View.GONE) return;

    animatorScale.cancel();

    animate().alpha(0)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            setVisibility(GONE);
            animate().setListener(null).start();
          }
        })
        .setDuration(DURATION)
        .start();

    stopAnimations();
  }

  public int[] getLocationOfRing() {
    viewRing.getLocationOnScreen(locationOfRing);
    return locationOfRing;
  }

  public int getWidthOfRing() {
    return viewRing.getWidth();
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  private void startAnimations() {
    startFloatingArrowAnimation();
  }

  private void startFloatingArrowAnimation() {
    animatorFloating = new AnimatorSet();
    ValueAnimator animatorTranslationTop = ValueAnimator.ofInt(0, -screenUtils.dpToPx(5f));
    animatorTranslationTop.setDuration(DURATION_SLOW);
    animatorTranslationTop.setInterpolator(new DecelerateInterpolator());
    animatorTranslationTop.addUpdateListener(
        valueAnimator -> imgArrow.setTranslationY((int) valueAnimator.getAnimatedValue()));

    ValueAnimator animatorTranslationBottom = ValueAnimator.ofInt(-screenUtils.dpToPx(5f), 0);
    animatorTranslationBottom.setDuration(DURATION).setInterpolator(new DecelerateInterpolator());
    animatorTranslationBottom.addUpdateListener(
        valueAnimator -> imgArrow.setTranslationY((int) valueAnimator.getAnimatedValue()));

    animatorFloating.playSequentially(animatorTranslationTop, animatorTranslationBottom);
    animatorFloating.addListener(new AnimatorCancelListener());
    animatorFloating.start();
  }

  private void stopAnimations() {
    stopFloatingArrowAnimation();
    clearAnimation();
  }

  private void stopFloatingArrowAnimation() {
    animatorFloating.cancel();
    animatorFloating.removeAllListeners();
  }

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////
}