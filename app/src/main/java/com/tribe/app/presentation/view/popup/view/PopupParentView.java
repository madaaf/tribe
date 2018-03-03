package com.tribe.app.presentation.view.popup.view;

/**
 * Created by tiago on 26/02/2018.
 */

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.popup.PopupManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.chat.OnSwipeTouchListener;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PopupParentView extends FrameLayout implements Animation.AnimationListener {

  protected static final long DISPLAY_TIME_IN_SECONDS = 5000;
  protected static final int CLEAN_UP_DELAY_MILLIS = 100;
  protected static final int SCREEN_SCALE_FACTOR = 6;

  // VARIABLES
  protected PopupManager.Popup popup;
  protected Animation slideInAnimation;
  protected Animation slideOutAnimation;
  protected boolean marginSet;
  protected long duration = DISPLAY_TIME_IN_SECONDS;
  protected OnSwipeTouchListener swipeTouchListener;

  @Inject ScreenUtils screenUtils;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onDismiss = PublishSubject.create();

  public PopupParentView(@NonNull PopupManager.Popup popup) {
    super(popup.getActivityWR().get());

    this.setId(View.generateViewId());
    this.popup = popup;

    initView();
  }

  private void initView() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    int size = screenUtils.getWidthPx() - screenUtils.dpToPx(10) * 2;
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(size, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.CENTER;
    addView(popup.getView(), params);

    unbinder = ButterKnife.bind(this);

    popup.getView().setPopupListener(popup.getListener());
    subscriptions.add(popup.getView().onDone.subscribe(aVoid -> hide()));

    setBackground(null);
    setAnimation();
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  @Override protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (!marginSet) {
      marginSet = true;
      if (getLayoutParams() instanceof MarginLayoutParams) {
        // Add a negative top margin to compensate for overshoot enter animation
        final ViewGroup.MarginLayoutParams params =
            (ViewGroup.MarginLayoutParams) getLayoutParams();
        params.topMargin = params.topMargin - (getScreenHeight() / SCREEN_SCALE_FACTOR);
        requestLayout();
      }
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    slideInAnimation.setAnimationListener(null);
    unbinder.unbind();
  }

  /* Interface Method Implementations */

  @Override public void onAnimationStart(final Animation animation) {
    if (!isInEditMode()) {
      performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }
  }

  @Override public void onAnimationEnd(final Animation animation) {
    if (popup.isAutoDismiss()) postDelayed(() -> hide(), duration);
  }

  @Override public void onAnimationRepeat(final Animation animation) {
  }

  /////////////////
  //  PRIVATE   ///
  /////////////////

  public void hide() {
    setEnabled(false);

    try {
      slideOutAnimation.setAnimationListener(new AnimationListenerAdapter() {
        @Override public void onAnimationStart(final Animation animation) {
          setOnClickListener(null);
          setClickable(false);
        }

        @Override public void onAnimationEnd(final Animation animation) {
          removeFromParent();
        }
      });

      popup.getView().startAnimation(slideOutAnimation);
    } catch (Exception ex) {
      Log.e(getClass().getSimpleName(), Log.getStackTraceString(ex));
    }
  }

  private void removeFromParent() {
    postDelayed(() -> {
      onDismiss.onNext(null);

      try {
        if (getParent() == null) {
          Timber.d("getParent() returning Null");
        } else {
          try {
            ((ViewGroup) getParent()).removeView(PopupParentView.this);
          } catch (Exception ex) {
            Timber.d("Cannot remove from parent layout");
          }
        }
      } catch (Exception ex) {
        Timber.d(Log.getStackTraceString(ex));
      }
    }, CLEAN_UP_DELAY_MILLIS);
  }

  private int getScreenHeight() {
    return screenUtils.getHeightPx();
  }

  private void setAnimation() {
    slideInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_from_top);
    slideOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_out_to_top);
    slideInAnimation.setAnimationListener(this);
    popup.getView().setAnimation(slideInAnimation);
  }

  /**
   * PUBLIC
   */

  public void setTouch() {
    swipeTouchListener = new OnSwipeTouchListener(getContext());
    subscriptions.add(swipeTouchListener.onSwipeUp().subscribe((Void aVoid) -> hide()));
    setOnTouchListener(swipeTouchListener);
  }

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////

  public Observable<Void> onDismiss() {
    return onDismiss;
  }
}

