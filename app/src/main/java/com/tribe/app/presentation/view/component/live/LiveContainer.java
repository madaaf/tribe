package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewUtils;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveContainer extends FrameLayout {

  @IntDef({ EVENT_CLOSED, EVENT_OPENED }) public @interface Event {
  }

  public static final int EVENT_CLOSED = 0;
  public static final int EVENT_OPENED = 1;

  private final int DIFF_DOWN = 20;
  private final int SCROLL_TOLERANCE = 10;
  private final int LONG_PRESS = 80;
  private final int DRAG_END_DELAY = 300;
  private final int DURATION = 300;

  private static final SpringConfig ORIGAMI_SPRING_CONFIG =
      SpringConfig.fromBouncinessAndSpeed(0f, 100f);
  private static final float DRAG_RATE = 0.1f;
  private static final int DRAG_THRESHOLD = 20;
  private static final int INVALID_POINTER = -1;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewLive) LiveView viewLive;

  @BindView(R.id.viewInviteLive) LiveInviteView viewInviteLive;

  @BindView(R.id.viewShadowRight) View viewShadowRight;

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
  private TileView currentTileView, draggedTileView;
  private int[] tileLocationStart = new int[2];
  private int statusBarHeight = 0;
  private boolean hasNotifiedAtRest = false;
  private boolean dropEnabled = false;
  private int initialTileHeight = 0;
  private boolean hiddenControls = false;

  // DIMENS
  private int thresholdEnd;

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private Subscription timerLongPress, timerEndDrag;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Integer> onEvent = PublishSubject.create();
  private PublishSubject<TileView> onStartDrag = PublishSubject.create();
  private PublishSubject<Void> onEndDrag = PublishSubject.create();
  private PublishSubject<Float> onAlpha = PublishSubject.create();
  private PublishSubject<Boolean> onDropEnabled = PublishSubject.create();
  private PublishSubject<TileView> onDropped = PublishSubject.create();

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

  @Override protected void onDetachedFromWindow() {
    springRight.removeListener(springRightListener);

    if (unbinder != null) unbinder.unbind();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    unbinder = ButterKnife.bind(this);

    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
    screenUtils = applicationComponent.screenUtils();

    initDimen();
    initUI();
    initSubscriptions();
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

  private void initDimen() {
    thresholdEnd =
        getContext().getResources().getDimensionPixelSize(R.dimen.threshold_open_live_invite);
  }

  private void initSubscriptions() {
    subscriptions.add(viewInviteLive.onScroll().subscribe(dy -> {
      overallScrollY += dy;
    }));

    subscriptions.add(viewLive.onHiddenControls().subscribe(aBoolean -> hiddenControls = aBoolean));

    subscriptions.add(viewLive.onShouldCloseInvites().subscribe(aVoid -> closeInviteView()));

    subscriptions.add(viewInviteLive.onScrollStateChanged().subscribe(newState -> {
      if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
        overallScrollY = 0;
      } else if (timerLongPress != null) timerLongPress.unsubscribe();
    }));

    subscriptions.add(viewLive.onOpenInvite().subscribe(aVoid -> {
      if (isEnabled()) {
        if (!isOpened) {
          openInviteView();
        } else {
          closeInviteView();
        }
      }
    }));

    viewLive.initInviteOpenSubscription(onEvent());
    viewLive.initOnStartDragSubscription(onStartDrag());
    viewLive.initOnEndDragSubscription(onEndDrag());
    viewLive.initOnAlphaSubscription(onAlpha());
    viewLive.initDropSubscription(onDropped());
  }

  public void setStatusBarHeight(int height) {
    statusBarHeight = height;
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
        ev.getRawX() >= screenUtils.getWidthPx() - viewInviteLive.getWidth();
    if (!isEnabled() || hiddenControls) {
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

          timerLongPress = Observable.timer(LONG_PRESS, TimeUnit.MILLISECONDS)
              .onBackpressureDrop()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(time -> {
                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS
                    && isDown
                    && Math.abs(currentX - downX) < diffDown
                    && Math.abs(currentY - downY) < diffDown
                    && overallScrollY < scrollTolerance) {
                  currentTileView = viewInviteLive.findViewByCoords(downX, downY);
                  if (currentTileView != null) {
                    createTileForDrag();
                  }
                }
              });
        }

        break;
      case MotionEvent.ACTION_MOVE:
        if (activePointerId == INVALID_POINTER) {
          return false;
        }

        float diffY = ev.getY() - lastDownY;
        float diffX = ev.getX() - lastDownX;

        if ((!isOpened || !isTouchInInviteView) && currentTileView == null) {
          final boolean isSwipingHorizontally = Math.abs(diffX) > Math.abs(diffY);

          if (isSwipingHorizontally
              && Math.abs(diffX) > touchSlop
              && Math.abs(diffX) > screenUtils.dpToPx(DRAG_THRESHOLD)
              && !beingDragged) {
            beingDragged = true;
          }
        }

        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        clearTouch();

        break;
    }

    return beingDragged || currentTileView != null;
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

          if (currentTileView == null && velocityTracker != null) {
            if (offsetX <= 0 && !isOpened) applyOffsetRightWithTension(offsetX);

            velocityTracker.addMovement(event);
          } else if (draggedTileView != null && draggedTileView.getParent() != null) {
            final int locationDraggedView[] = { 0, 0 };
            draggedTileView.getLocationOnScreen(locationDraggedView);
            positionViewAt(draggedTileView, (int) (tileLocationStart[0] + offsetX),
                (int) (tileLocationStart[1] + offsetY) - statusBarHeight);

            boolean isIn = ViewUtils.isIn(viewLive, (int) x, (int) y);

            if (isIn && !dropEnabled) {
              startTileDrop();
            } else if (!isIn && dropEnabled) {
              endTileDrop();
            }
          }
        }

        break;
      }

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL: {
        final int pointerIndex = event.findPointerIndex(activePointerId);

        if (currentTileView == null && pointerIndex != INVALID_POINTER && velocityTracker != null) {
          velocityTracker.addMovement(event);
          velocityTracker.computeCurrentVelocity(1000);

          if (isOpened) {
            if (draggedTileView != null) removeTileForDrag();
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

        clearTouch();

        break;
      }
    }

    return true;
  }

  private void clearTouch() {
    if (currentTileView != null) {
      if (draggedTileView != null) {
        if (!dropEnabled) {
          endTileDrag();
          AnimationUtils.animateLeftMargin(draggedTileView, tileLocationStart[0], DRAG_END_DELAY,
              new DecelerateInterpolator());
          AnimationUtils.animateTopMargin(draggedTileView, tileLocationStart[1] - statusBarHeight,
              DRAG_END_DELAY, new DecelerateInterpolator());
          prepareRemoveTileForDrag();
        } else {
          onDropped.onNext(draggedTileView);
          prepareRemoveTileForDrag();
        }
      } else {
        clearCurrentTile();
      }
    }

    beingDragged = false;
    activePointerId = INVALID_POINTER;
    overallScrollY = 0;
    isDown = false;

    if (timerLongPress != null) timerLongPress.unsubscribe();

    dropEnabled = false;
  }

  private void clearCurrentTile() {
    if (currentTileView != null) {
      currentTileView.setVisibility(View.VISIBLE);
      currentTileView = null;
    }
  }

  private void createTileForDrag() {
    draggedTileView = new TileView(getContext(), currentTileView.getType());
    draggedTileView.setBackground(currentTileView.getPosition());
    draggedTileView.setInfo(currentTileView.getRecipient());
    currentTileView.getLocationOnScreen(tileLocationStart);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(currentTileView.getWidth(), currentTileView.getHeight());
    params.leftMargin = tileLocationStart[0];
    params.topMargin = tileLocationStart[1] - statusBarHeight;
    addView(draggedTileView, params);

    currentTileView.setVisibility(View.GONE);
    startTileDrag();
  }

  private void startTileDrag() {
    draggedTileView.startDrag();
    onStartDrag.onNext(draggedTileView);
  }

  private void endTileDrag() {
    draggedTileView.endDrag();
    onEndDrag.onNext(null);
  }

  private void prepareRemoveTileForDrag() {
    timerEndDrag = Observable.timer(DRAG_END_DELAY, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(time -> {
          Timber.d("Remove tile");
          clearCurrentTile();
          removeTileForDrag();
          timerEndDrag.unsubscribe();
        });
  }

  private void removeTileForDrag() {
    Timber.d("Removed tile");
    removeView(draggedTileView);
    draggedTileView = null;
  }

  private void startTileDrop() {
    dropEnabled = true;
    onDropEnabled.onNext(true);
    initialTileHeight = currentTileView.getHeight();
    AnimationUtils.animateSize(currentTileView, initialTileHeight, 0, DURATION);
    draggedTileView.startDrop();
  }

  private void endTileDrop() {
    dropEnabled = false;
    onDropEnabled.onNext(false);
    AnimationUtils.animateSize(currentTileView, 0, initialTileHeight, DURATION);
    draggedTileView.endDrop();
  }

  private void positionViewAt(View view, int x, int y) {
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
    params.leftMargin = x;
    params.topMargin = y;
    draggedTileView.setLayoutParams(params);
    draggedTileView.requestLayout();
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  private class RightSpringListener extends SimpleSpringListener {

    @Override public void onSpringUpdate(Spring spring) {
      if (ViewCompat.isAttachedToWindow(LiveContainer.this)) {
        float value = (float) spring.getCurrentValue();
        float appliedValue = Math.min(Math.max(value, -viewInviteLive.getWidth()), 0);

        if (Math.abs(appliedValue - spring.getEndValue()) < screenUtils.dpToPx(2.5f)) {
          appliedValue = (float) spring.getEndValue();

          if (!hasNotifiedAtRest) {
            if (spring.getEndValue() == 0) {
              onEvent.onNext(EVENT_CLOSED);
            } else {
              onEvent.onNext(EVENT_OPENED);
            }
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
    onAlpha.onNext(
        (float) SpringUtil.mapValueFromRangeToRange(value, -viewInviteLive.getWidth(), 0, 0, 1));

    ViewGroup.LayoutParams params = viewLive.getLayoutParams();
    params.width = (int) (screenUtils.getWidthPx() + value);
    viewLive.setLayoutParams(params);
    viewLive.requestLayout();

    viewShadowRight.setTranslationX(value);
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

  private void openInviteView() {
    isOpened = true;
    if (velocityTracker != null) {
      springRight.setVelocity(velocityTracker.getXVelocity())
          .setEndValue(-viewInviteLive.getWidth());
    } else {
      springRight.setEndValue(-viewInviteLive.getWidth());
    }
  }

  private void closeInviteView() {
    isOpened = false;
    springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
  }

  private float getTotalDragDistance() {
    return getHeight() / 4;
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

  public Observable<Integer> onEvent() {
    return onEvent;
  }

  public Observable<TileView> onStartDrag() {
    return onStartDrag;
  }

  public Observable<Void> onEndDrag() {
    return onEndDrag;
  }

  public Observable<Float> onAlpha() {
    return onAlpha;
  }

  public Observable<Boolean> onDropEnabled() {
    return onDropEnabled;
  }

  public Observable<TileView> onDropped() {
    return onDropped;
  }
}