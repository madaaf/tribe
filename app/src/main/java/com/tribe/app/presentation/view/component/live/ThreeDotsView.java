package com.tribe.app.presentation.view.component.live;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class ThreeDotsView extends LinearLayout {

  private final static int DURATION = 1000;

  @BindViews({ R.id.viewDot1, R.id.viewDot2, R.id.viewDot3 }) List<View> viewDots;

  // VARIABLES
  private Unbinder unbinder;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public ThreeDotsView(Context context) {
    super(context);
    init();
  }

  public ThreeDotsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ThreeDotsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    subscriptions.add(Observable.interval((DURATION >> 1) * viewDots.size(), TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          for (int i = 0; i < viewDots.size(); i++) {
            View viewDot = viewDots.get(i);
            ValueAnimator animator = ValueAnimator.ofFloat(1f, 1.5f, 1f);
            animator.setDuration(DURATION);
            animator.setStartDelay(i * (DURATION >> 1));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
              float value = (float) animation.getAnimatedValue();
              viewDot.setScaleX(value);
              viewDot.setScaleY(value);
            });
            animator.start();
          }
        }));
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_three_dots, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);
    setOrientation(HORIZONTAL);
    setGravity(Gravity.CENTER);
    setClipToPadding(false);
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

  //////////////
  //  PUBLIC  //
  //////////////
}
