package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class ChasingDotsView extends FrameLayout {

  private final static int DURATION = 1500;
  private final static int NB_VIEWS = 8;
  private final static int TRANSLATION_FROM_CENTER = 15;

  @Inject ScreenUtils screenUtils;

  // VARIABLES
  private Unbinder unbinder;
  private List<View> viewDots;
  private boolean stopped = false;

  // RESOURCES
  private int sizeDot;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public ChasingDotsView(Context context) {
    super(context);
    init();
  }

  public ChasingDotsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ChasingDotsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    animateSpin();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();
    super.onDetachedFromWindow();
  }

  private void init() {
    viewDots = new ArrayList<>();

    initDependencyInjector();
    initResources();

    setBackground(null);
    setClipToPadding(false);

    initViews();
  }

  private void initResources() {
    sizeDot = getResources().getDimensionPixelSize(R.dimen.waiting_view_dot_size);
  }

  private void initViews() {
    int translationFromCenter = screenUtils.dpToPx(TRANSLATION_FROM_CENTER);

    for (int i = 0; i < NB_VIEWS; i++) {
      View v = new View(getContext());
      v.setScaleX(0);
      v.setScaleY(0);
      //v.setAlpha(0);
      v.setBackgroundResource(R.drawable.shape_oval_white);
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot, sizeDot);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);

      float angleDeg = (i * (360.0f / NB_VIEWS)) - 90.0f;
      float angleRad = (float) (angleDeg * Math.PI / 180.0f);
      v.setTranslationX(translationFromCenter * (float) Math.cos(angleRad));
      v.setTranslationY(translationFromCenter * (float) Math.sin(angleRad));

      viewDots.add(v);
      addView(v);
    }
  }

  private void animateSpin() {
    for (int i = 0; i < viewDots.size(); i++) {
      final View viewDot = viewDots.get(i);
      final boolean last = (i == (viewDots.size() - 1));

      subscriptions.add(Observable.timer(i * (DURATION / viewDots.size()), TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f, 0f);
            animator.setDuration(DURATION);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
              float value = (float) animation.getAnimatedValue();
              viewDot.setScaleX(value);
              viewDot.setScaleY(value);
              //viewDot.setAlpha(value);
            });

            if (last) {
              animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationStart(Animator animation) {
                  if (!stopped) animateSpin();
                }

                @Override public void onAnimationEnd(Animator animation) {
                  animator.removeAllListeners();
                }

                @Override public void onAnimationCancel(Animator animation) {
                  animator.removeAllListeners();
                }
              });
            }

            animator.start();
          }));
    }
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

  public void dispose() {
    stopped = true;
    subscriptions.clear();
  }
}
