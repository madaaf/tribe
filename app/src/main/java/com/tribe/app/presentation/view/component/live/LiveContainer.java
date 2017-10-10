package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.annotation.IntDef;
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
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/05/2017.
 */
public class LiveContainer extends FrameLayout {

  //public static final int MAX_DURATION_INVITE_VIEW_OPEN = 5;

  @IntDef({ OPEN_PARTIAL, OPEN_FULL, CLOSED }) public @interface Event {
  }

  public static final int OPEN_PARTIAL = 0;
  public static final int OPEN_FULL = 1;
  public static final int CLOSED = 2;

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
  private boolean isOpenedPartially = false;
  private boolean isOpenedFully = false;
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
  private PublishSubject<Integer> onEventChange = PublishSubject.create();

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
    viewLive.initDrawerEventChangeObservable(onEventChange);

    subscriptions.add(viewLive.onInviteMoreClick().subscribe(aVoid -> {
      if (!isOpenedPartially && !isOpenedFully) {
        openPartialInviteView();
      } else if (isOpenedPartially) {
        openFullInviteView();
      } else if (isOpenedFully) closeFullInviteView();
    }));

    subscriptions.add(viewLive.onOpenInvite().subscribe(aVoid -> {
      if (!isOpenedPartially) {
        openPartialInviteView();
      } else if (isOpenedPartially) {
        closePartialInviteView();
      }
    }));
  }

  public void setStatusBarHeight(int height) {
    //statusBarHeight = height;
  }

  public boolean beingDragged() {
    return beingDragged;
  }

  public boolean isOpenedPartially() {
    return isOpenedPartially;
  }

  public boolean isOpenedFully() {
    return isOpenedFully;
  }

  ///////////////////////
  //    TOUCH EVENTS   //
  ///////////////////////

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    int widthOpen = 0;

    if (isOpenedFully) {
      widthOpen = viewLive.getLiveInviteViewFullWidth();
    } else if (isOpenedPartially) widthOpen = viewLive.getLiveInviteViewPartialWidth();

    boolean isTouchInInviteView = ev.getRawX() >= screenUtils.getWidthPx() - widthOpen;
    if (!isEnabled()) {
      return false;
    }

    final int action = MotionEventCompat.getActionMasked(ev);

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        if (!isOpenedPartially && !isOpenedFully) springRight.setCurrentValue(0).setAtRest();

        activePointerId = ev.getPointerId(0);

        lastDownX = ev.getRawX();
        lastDownY = ev.getRawY();

        if (!isOpenedPartially || !isOpenedFully || !isTouchInInviteView) {
          beingDragged = false;

          velocityTracker = VelocityTracker.obtain();
          velocityTracker.addMovement(ev);
        } else if (isTouchInInviteView && isOpenedPartially) {
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

        if (!isOpenedPartially || !isOpenedFully || !isTouchInInviteView) {
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
        clearTouch();
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

          if (velocityTracker != null) {
            if (offsetX <= 0 && !isOpenedPartially) {
              applyOffsetRightWithTension(offsetX);
            }

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

          if (isOpenedFully) {
            springRight.setCurrentValue(-viewLive.getLiveInviteViewFullWidth()).setAtRest();
            closeFullInviteView();
            clearTouch();
            break;
          }

          float x = event.getX(pointerIndex) - location[0];
          float offsetX = x - lastDownX;

          if (offsetX <= 0) {
            if (!isOpenedPartially) {
              springRight.setCurrentValue(currentOffsetRight).setAtRest();

              if (offsetX < -thresholdEnd) {
                openPartialInviteView();
                clearTouch();
                break;
              } else {
                springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
              }
            } else {
              springRight.setCurrentValue(-viewLive.getLiveInviteViewPartialWidth()).setAtRest();
              openFullInviteView();
              clearTouch();
              break;
            }
          } else if (isOpenedPartially) {
            springRight.setCurrentValue(-viewLive.getLiveInviteViewPartialWidth()).setAtRest();
            closePartialInviteView();
          }
        }

        clearTouch();
        break;
      }
    }

    return true;
  }

  private void clearTouch() {
    beingDragged = false;
    activePointerId = INVALID_POINTER;
    overallScrollY = 0;
    isDown = false;
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  private class RightSpringListener extends SimpleSpringListener {

    @Override public void onSpringUpdate(Spring spring) {
      if (ViewCompat.isAttachedToWindow(LiveContainer.this)) {
        float value = (float) spring.getCurrentValue();
        float appliedValue = 0;

        if (isOpenedFully || spring.getStartValue() == -viewLive.getLiveInviteViewFullWidth()) {
          appliedValue = Math.min(Math.max(value, -viewLive.getLiveInviteViewFullWidth()),
              -viewLive.getLiveInviteViewPartialWidth());
        } else {
          appliedValue = Math.min(Math.max(value, -viewLive.getLiveInviteViewPartialWidth()), 0);
        }

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

  public void openPartialInviteView() {
    isOpenedPartially = true;
    isOpenedFully = false;

    if (velocityTracker != null) {
      springRight.setVelocity(velocityTracker.getXVelocity())
          .setEndValue(-viewLive.getLiveInviteViewPartialWidth());
    } else {
      springRight.setEndValue(-viewLive.getLiveInviteViewPartialWidth());
    }

    onEventChange.onNext(OPEN_PARTIAL);
  }

  public void openFullInviteView() {
    isOpenedFully = true;
    isOpenedPartially = false;

    if (velocityTracker != null) {
      springRight.setVelocity(velocityTracker.getXVelocity())
          .setEndValue(-viewLive.getLiveInviteViewFullWidth());
    } else {
      springRight.setEndValue(-viewLive.getLiveInviteViewFullWidth());
    }

    onEventChange.onNext(OPEN_FULL);
  }

  private void closePartialInviteView() {
    isOpenedPartially = false;
    isOpenedFully = false;

    if (velocityTracker != null) {
      springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
    } else {
      springRight.setEndValue(0);
    }

    onEventChange.onNext(CLOSED);
  }

  private void closeFullInviteView() {
    isOpenedFully = false;
    isOpenedPartially = true;

    if (velocityTracker != null) {
      springRight.setVelocity(velocityTracker.getXVelocity())
          .setEndValue(-viewLive.getLiveInviteViewPartialWidth());
    } else {
      springRight.setEndValue(-viewLive.getLiveInviteViewPartialWidth());
    }

    onEventChange.onNext(OPEN_PARTIAL);
  }

  private float getTotalDragDistance() {
    return !isOpenedPartially ? viewLive.getLiveInviteViewPartialWidth()
        : viewLive.getLiveInviteViewFullWidth();
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

  public Observable<Integer> onEventChange() {
    return onEventChange;
  }
}