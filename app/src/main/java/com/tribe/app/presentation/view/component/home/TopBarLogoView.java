package com.tribe.app.presentation.view.component.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 04/05/17.
 */
public class TopBarLogoView extends FrameLayout {

  public static final int DURATION = 400;
  private static final float OVERSHOOT = 0.75f;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewWave) View viewWave;

  @BindView(R.id.imgLogo) ImageView imgLogo;

  // RESOURCES

  // VARIABLES
  private Unbinder unbinder;
  private Animation animationWave;
  private float previousTranslation = 0f;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public TopBarLogoView(Context context) {
    super(context);
    init(context, null);
  }

  public TopBarLogoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public TopBarLogoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    if (animationWave != null) animationWave.cancel();

    super.onDetachedFromWindow();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());

    final int waveImgWidth =
        ContextCompat.getDrawable(getContext(), R.drawable.bg_pull_to_refresh).getIntrinsicWidth();

    final int count = getChildCount();
    for (int i = 0; i < count; i++) {
      final View v = getChildAt(i);
      if (v.getId() == R.id.viewWave) {
        v.measure(
            MeasureSpec.makeMeasureSpec(getMeasuredWidth() + waveImgWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
      }
    }

    initWave();
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_pull_to_refresh_wave, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);

    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewWave.getLayoutParams();
    params.width = (int) (screenUtils.getWidthPx() * 1.5f);
    params.height = screenUtils.getHeightPx();
    viewWave.setLayoutParams(params);

    imgLogo.setTranslationY(-screenUtils.getHeightPx() >> 1);
  }

  private void initResources() {

  }

  private void initWave() {
    if (animationWave != null) animationWave.cancel();

    final int waveImgWidth =
        ContextCompat.getDrawable(getContext(), R.drawable.bg_pull_to_refresh).getIntrinsicWidth();

    int measuredWidth = getMeasuredWidth();
    animationWave = new TranslateAnimation(0, waveImgWidth, 0, 0);
    animationWave.setInterpolator(new LinearInterpolator());
    animationWave.setRepeatCount(Animation.INFINITE);
    animationWave.setDuration(measuredWidth * 5);

    viewWave.startAnimation(animationWave);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void reset() {
    imgLogo.clearAnimation();
    imgLogo.setTranslationY(-screenUtils.getHeightPx() >> 1);
    imgLogo.setRotation(0);
  }

  public void startRefresh(float totalDragDistance) {
    imgLogo.animate()
        .translationY(totalDragDistance / 2 + (float) imgLogo.getHeight() / 2)
        .setDuration(DURATION)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            imgLogo.animate()
                .rotation(360)
                .setDuration(DURATION)
                .setStartDelay(DURATION)
                .setListener(null)
                .setInterpolator(new OvershootInterpolator(OVERSHOOT))
                .start();
          }
        })
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .start();
  }

  public void endRefresh() {
    //imgLogo.animate().setDuration(DURATION).translationY(-screenUtils.getHeightPx() >> 1).start();
  }

  public void setTranslation(float translationY) {
    if (previousTranslation == 0) previousTranslation = translationY;
    imgLogo.setTranslationY(imgLogo.getTranslationY() - (previousTranslation - translationY));
    previousTranslation = translationY;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////
}
