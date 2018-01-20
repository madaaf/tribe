package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameBirdRushBackground extends FrameLayout {

  private static final Long SPEED_BACK_SCROLL = 5000L;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.background_one) ImageView backgroundOne;
  @BindView(R.id.background_two) ImageView backgroundTwo;


  /**
   * VARIABLES
   */

  private Unbinder unbinder;
  private ValueAnimator animator;

  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsAnimation = new CompositeSubscription();

  public GameBirdRushBackground(@NonNull Context context) {
    super(context);
    init();
  }

  public GameBirdRushBackground(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    initDependencyInjector();
    initResources();
    initView();
  }

  private void initResources() {

  }

  private void initView() {
    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_bird_rush_background, this, true);
    unbinder = ButterKnife.bind(this);

    positionBird();
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void positionBird() {

  }

  private void initAnimations() {
    setBackScrolling();
  }

  private void setBackScrolling() {
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(SPEED_BACK_SCROLL);
        animator.addUpdateListener(animation -> {
          final float progress = (float) animation.getAnimatedValue();
          final float width = backgroundOne.getWidth();
          final float translationX = -width * progress;
          backgroundOne.setTranslationX(translationX);
          backgroundTwo.setTranslationX(translationX + width);
        });

        animator.addListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationRepeat(Animator animation) {
            super.onAnimationRepeat(animation);
          }
        });
        animator.start();
      }
    });
  }

  /**
   * PUBLIC
   */

  public void start() {
    initAnimations();
  }

  public void stop() {

  }

  public void dispose() {
    subscriptions.unsubscribe();
    subscriptionsAnimation.unsubscribe();
  }

  /**
   * OBSERVABLES
   */

}
