package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/05/2017.
 */
public class LiveContainer extends FrameLayout {

  //public static final int MAX_DURATION_INVITE_VIEW_OPEN = 5;

  private final int DIFF_DOWN = 20;
  private final int SCROLL_TOLERANCE = 10;
  private final int LONG_PRESS = 80;
  private final int DRAG_END_DELAY = 300;
  private final int DRAG_END_DROP_DELAY = 450;
  private final int DURATION = 300;

  private static final SpringConfig ORIGAMI_SPRING_CONFIG =
      SpringConfig.fromBouncinessAndSpeed(0f, 100f);
  private static final float DRAG_RATE = 0.1f;
  private static final int DRAG_THRESHOLD = 20;
  private static final int INVALID_POINTER = -1;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewLive) LiveView viewLive;

  // SPRINGS
  private SpringSystem springSystem = null;
  private Spring springRight;
  private RightSpringListener springRightListener;

  // VARIABLES
  private float currentDragPercent;
  private boolean beingDragged = false;
  private float lastDownX;
  private float lastDownY;
  private int activePointerId;
  private VelocityTracker velocityTracker;
  private int touchSlop;
  private int currentOffsetRight;
  private boolean isOpened = false;
  private boolean isDown = false;
  private long longDown = 0L;
  private float downX, downY, currentX, currentY, diffDown, scrollTolerance;
  private int overallScrollY = 0;
  private boolean hasNotifiedAtRest = false;

  // DIMENS
  private int thresholdEnd;

  //// BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private Subscription timerLongPress, timerEndDrag;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveContainer(Context context) {
    super(context);
  }

  public LiveContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    springRight.addListener(springRightListener);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    unbinder = ButterKnife.bind(this);

    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
    screenUtils = applicationComponent.screenUtils();

    initRessource();
    initUI();
    initSubscriptions();
  }

  public void dispose() {
    springRight.removeListener(springRightListener);

    try {
      if (unbinder != null) unbinder.unbind();
    } catch (IllegalStateException ex) {
    }

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }
  }

  private void initUI() {
    springSystem = SpringSystem.create();
    springRight = springSystem.createSpring();
    springRight.setSpringConfig(ORIGAMI_SPRING_CONFIG);
    springRightListener = new RightSpringListener();
    touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    diffDown = screenUtils.dpToPx(DIFF_DOWN);
    scrollTolerance = screenUtils.dpToPx(SCROLL_TOLERANCE);
  }

  private void initRessource() {
    thresholdEnd =
        getContext().getResources().getDimensionPixelSize(R.dimen.threshold_open_live_invite);
  }

  private void initSubscriptions() {

  }

  public void setStatusBarHeight(int height) {
    //statusBarHeight = height;
  }

  public boolean beingDragged() {
    return beingDragged;
  }

  public boolean isOpened() {
    return isOpened;
  }

  ///////////////////////
  //    TOUCH EVENTS   //
  ///////////////////////

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    boolean isTouchInInviteView =
        ev.getRawX() >= screenUtils.getWidthPx() - screenUtils.dpToPx(LiveInviteView.WIDTH);
    if (!isEnabled()) {
      return false;
    }

    final int action = MotionEventCompat.getActionMasked(ev);

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        if (!isOpened) springRight.setCurrentValue(0).setAtRest();

        activePointerId = ev.getPointerId(0);

        lastDownX = ev.getRawX();
        lastDownY = ev.getRawY();

        if (!isOpened || !isTouchInInviteView) {
          beingDragged = false;

          velocityTracker = VelocityTracker.obtain();
          velocityTracker.addMovement(ev);
        } else if (isTouchInInviteView && isOpened) {
          longDown = System.currentTimeMillis();
          downX = currentX = ev.getRawX();
          downY = currentY = ev.getRawY();
          isDown = true;
        }

        break;
      case MotionEvent.ACTION_MOVE:
        if (activePointerId == INVALID_POINTER) {
          return false;
        }

        float diffY = ev.getY() - lastDownY;
        float diffX = ev.getX() - lastDownX;

        if ((!isOpened || !isTouchInInviteView)) {
          final boolean isSwipingHorizontally = Math.abs(diffX) > Math.abs(diffY);

          if (isSwipingHorizontally &&
              Math.abs(diffX) > touchSlop &&
              Math.abs(diffX) > screenUtils.dpToPx(DRAG_THRESHOLD) &&
              !beingDragged) {
            beingDragged = true;
          }
        }

        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        break;
    }

    return beingDragged;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();

    final int location[] = { 0, 0 };
    getLocationOnScreen(location);

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = event.findPointerIndex(activePointerId);

        if (pointerIndex != INVALID_POINTER) {
          float x = event.getX(pointerIndex) + location[0];
          float offsetX = x - lastDownX;
          float y = event.getY(pointerIndex) + location[1];
          float offsetY = y - lastDownY;

          if (velocityTracker != null) {
            if (offsetX <= 0 && !isOpened) applyOffsetRightWithTension(offsetX);

            velocityTracker.addMovement(event);
          }
        }

        break;
      }

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL: {
        final int pointerIndex = event.findPointerIndex(activePointerId);

        if (pointerIndex != INVALID_POINTER && velocityTracker != null) {
          velocityTracker.addMovement(event);
          velocityTracker.computeCurrentVelocity(1000);

          if (isOpened) {
            closeInviteView();
            break;
          }

          float x = event.getX(pointerIndex) - location[0];
          float offsetX = x - lastDownX;

          if (offsetX <= 0) {
            springRight.setCurrentValue(currentOffsetRight).setAtRest();
            if (offsetX < -thresholdEnd && !isOpened) {
              openInviteView();
            } else {
              springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
            }
          }
        }

        break;
      }
    }

    return true;
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  private class RightSpringListener extends SimpleSpringListener {

    @Override public void onSpringUpdate(Spring spring) {
      if (ViewCompat.isAttachedToWindow(LiveContainer.this)) {
        float value = (float) spring.getCurrentValue();
        float appliedValue = Math.min(Math.max(value, -viewLive.getViewInviteWidth()), 0);

        if (Math.abs(appliedValue - spring.getEndValue()) < screenUtils.dpToPx(2.5f)) {
          appliedValue = (float) spring.getEndValue();

          if (!hasNotifiedAtRest) {
            hasNotifiedAtRest = true;
          }
        }

        applyRight(appliedValue);
      }
    }

    @Override public void onSpringActivate(Spring spring) {
      hasNotifiedAtRest = false;
    }
  }

  private void applyRight(float value) {
    viewLive.applyTranslateX(value);
  }

  private boolean applyOffsetRightWithTension(float offsetX) {
    float totalDragDistance = getTotalDragDistance();
    final float scrollRight = -offsetX * DRAG_RATE;
    currentDragPercent = scrollRight / totalDragDistance;

    if (currentDragPercent < 0) {
      return false;
    }
    currentOffsetRight = -computeOffsetWithTension(scrollRight, totalDragDistance);
    applyRight(currentOffsetRight);

    return true;
  }

  public void openInviteView() {
    isOpened = true;

    if (velocityTracker != null) {
      springRight.setVelocity(velocityTracker.getXVelocity())
          .setEndValue(-viewLive.getViewInviteWidth());
    } else {
      springRight.setEndValue(-viewLive.getViewInviteWidth());
    }
  }

  private void closeInviteView() {
    isOpened = false;
    if (velocityTracker != null) {
      springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
    } else {
      springRight.setEndValue(0);
    }
  }

  private float getTotalDragDistance() {
    return getHeight() / 3;
  }

  private int computeOffsetWithTension(float scrollDist, float totalDragDistance) {
    float boundedDragPercent = Math.min(1f, Math.abs(currentDragPercent));
    float extraOS = Math.abs(scrollDist) - totalDragDistance;
    float slingshotDist = totalDragDistance;
    float tensionSlingshotPercent =
        Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
    float tensionPercent =
        (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
    float extraMove = (slingshotDist) * tensionPercent / 2;
    return (int) ((slingshotDist * boundedDragPercent) + extraMove);
  }

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////
}