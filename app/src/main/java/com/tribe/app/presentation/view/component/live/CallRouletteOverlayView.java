package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
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
 * Created by madaaflak on 16/06/2017.
 */

public class CallRouletteOverlayView extends FrameLayout {

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  // VARIABLES
  private Unbinder unbinder;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public CallRouletteOverlayView(Context context) {
    super(context);
     //init();
  }

  public CallRouletteOverlayView(Context context, AttributeSet attrs) {
    super(context, attrs);
   // init();
  }

  public CallRouletteOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    //init();
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_call_roulette, this);
    unbinder = ButterKnife.bind(this);

    setBackground(
        ContextCompat.getDrawable(getContext(), R.drawable.shape_rect_black40_rounded_corners));
/*    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);*/
  }

  private void initResources() {

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

  ////////////
  // PUBLIC //
  ////////////

  public void show() {
    if (getVisibility() == View.VISIBLE) return;

    setAlpha(0f);
    animate().alpha(1f).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        animate().setListener(null).start();
        animation.removeAllListeners();
      }

      @Override public void onAnimationStart(Animator animation) {
        setVisibility(View.VISIBLE);
      }
    }).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
  }

  public void hide() {
    if (getVisibility() == View.GONE) return;

    animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        setVisibility(View.GONE);
        animate().setListener(null).start();
        animation.removeAllListeners();
      }
    }).setDuration(DURATION).setInterpolator(new DecelerateInterpolator()).start();
  }

  ////////////
  // CLICKS //
  ////////////

  /////////////////
  // OBSERVABLES //
  /////////////////
}
