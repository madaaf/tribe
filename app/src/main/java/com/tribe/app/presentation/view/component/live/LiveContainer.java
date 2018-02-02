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
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.utils.ViewUtils;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
  private final int LONG_PRESS = 120;
  private final int DRAG_END_DELAY = 300;
  private final int DRAG_END_DROP_DELAY = 450;
  private final int DURATION = 300;

  private static final SpringConfig ORIGAMI_SPRING_CONFIG =
      SpringConfig.fromBouncinessAndSpeed(0f, 100f);
  private static final float DRAG_RATE = 0.1f;
  private static final float DRAG_RATE_HANG_UP = 0.4f;
  private static final int DRAG_THRESHOLD = 20;
  private static final int INVALID_POINTER = -1;

  @Inject ScreenUtils screenUtils;

  @Inject StateManager stateManager;

  @BindView(R.id.viewLive) LiveView viewLive;

  @BindView(R.id.viewLiveInvite) LiveInviteView viewLiveInvite;

  @BindView(R.id.viewLiveDropZone) LiveDropZoneView viewLiveDropZone;

  @BindView(R.id.viewLiveHangUp) LiveHangUpView viewLiveHangUp;

  @BindView(R.id.viewRinging) LiveRingingView viewRinging;

  // SPRINGS
  private SpringSystem springSystem = null;
  private Spring springRight;
  private RightSpringListener springRightListener;
  private Spring springLeft;
  private LeftSpringListener springLeftListener;

  // VARIABLES
  private float currentDragPercent, lastDownX, lastDownY, downX, downY, currentX, currentY,
      diffDown, scrollTolerance, initDistance = 0;
  private boolean beingDragged = false, isOpenedPartially = false, isOpenedFully = false, isDown =
      false, hasNotifiedAtRest = false, dropEnabled = false, hasJoined = false, chatOpened = false,
      gameMenuOpen = false, touchEnabled = true, isEndCallOpened = false;
  private int activePointerId, touchSlop, currentOffsetRight, currentOffsetLeft, overallScrollY = 0,
      statusBarHeight = 0;
  private VelocityTracker velocityTracker;
  private long longDown = 0L;
  private TileInviteView currentTileView, draggedTileView;
  private int[] tileLocationStart = new int[2], tileLocationLast = new int[2];

  // DIMENS
  private int thresholdEnd, thresholdEndCall;

  //// BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private Subscription timerLongPress, timerEndDrag;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Integer> onEventChange = PublishSubject.create();
  private PublishSubject<TileInviteView> onStartDrag = PublishSubject.create();
  private PublishSubject<Void> onEndDrag = PublishSubject.create();
  private PublishSubject<Boolean> onDropZone = PublishSubject.create();
  private PublishSubject<TileInviteView> onDropped = PublishSubject.create();
  private PublishSubject<Void> onEndCall = PublishSubject.create();

  public LiveContainer(Context context) {
    super(context);
  }

  public LiveContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    springRight.addListener(springRightListener);
    springLeft.addListener(springLeftListener);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

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
    springRight.removeListener(springRightListener);
    springLeft.removeListener(springLeftListener);

    try {
      if (unbinder != null) unbinder.unbind();
    } catch (IllegalStateException ex) {
    }

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.clear();
    }
  }

  private void initUI() {
    springSystem = SpringSystem.create();
    springRight = springSystem.createSpring();
    springRight.setSpringConfig(ORIGAMI_SPRING_CONFIG);
    springRightListener = new RightSpringListener();
    springLeft = springSystem.createSpring();
    springLeft.setSpringConfig(ORIGAMI_SPRING_CONFIG);
    springLeftListener = new LeftSpringListener();

    touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    diffDown = screenUtils.dpToPx(DIFF_DOWN);
    scrollTolerance = screenUtils.dpToPx(SCROLL_TOLERANCE);

    tileLocationLast = new int[2];
  }

  private void initResources() {
    thresholdEnd =
        getContext().getResources().getDimensionPixelSize(R.dimen.threshold_open_live_invite);
    thresholdEndCall = screenUtils.getWidthPx() >> 1;
  }

  private void initSubscriptions() {
    viewLive.initDrawerEventChangeObservable(onEventChange);
    viewLive.initDrawerEndCallObservable(onEndCall);
    viewLiveInvite.initOnInviteDropped(onDropped);

    subscriptions.add(viewLive.onJoined().subscribe(tribeJoinRoom -> hasJoined = true));

    subscriptions.add(viewLive.onGameMenuOpen().subscribe(aBoolean -> gameMenuOpen = aBoolean));

    subscriptions.add(viewLiveInvite.onScroll().subscribe(dy -> overallScrollY += dy));

    subscriptions.add(viewLiveInvite.onScrollStateChanged().subscribe(newState -> {
      if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
        overallScrollY = 0;
      } else if (timerLongPress != null) timerLongPress.unsubscribe();
    }));

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

    subscriptions.add(viewLive.onTouchEnabled().subscribe(aBoolean -> touchEnabled = aBoolean));

    subscriptions.add(viewLive.onOpenChat().subscribe(aBoolean -> chatOpened = aBoolean));

    subscriptions.add(viewLiveInvite.onDisplayDropZone().subscribe(display -> {
      if (display) {
        viewLiveDropZone.show();
        viewRinging.hide();
      } else {
        viewLiveDropZone.hide();
        viewRinging.show();
      }
    }));

    subscriptions.add(viewLiveHangUp.onEndCall().subscribe(aVoid -> endCallSuccess()));
  }

  public void setStatusBarHeight(int height) {
    statusBarHeight = height;
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
    boolean isInHangUpButton =
        ViewUtils.isIn(viewLiveHangUp.getHangUpButton(), (int) ev.getRawX(), (int) ev.getRawY());
    if (!isEnabled() ||
        gameMenuOpen ||
        (!touchEnabled && !isOpenedFully && !isOpenedPartially) ||
        chatOpened ||
        (isInHangUpButton && isEndCallOpened)) { //!hasJoined ||
      return false;
    }

    final int action = MotionEventCompat.getActionMasked(ev);

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        if (!isOpenedPartially && !isOpenedFully && !isEndCallOpened) {
          springRight.setCurrentValue(0).setAtRest();
          springLeft.setCurrentValue(0).setAtRest();
        }

        activePointerId = ev.getPointerId(0);

        lastDownX = ev.getRawX();
        lastDownY = ev.getRawY();

        if ((!isOpenedPartially && !isOpenedFully && !isEndCallOpened) || !isTouchInInviteView) {
          beingDragged = false;

          velocityTracker = VelocityTracker.obtain();
          velocityTracker.addMovement(ev);
        } else if (isTouchInInviteView && (isOpenedPartially || isOpenedFully)) {
          longDown = System.currentTimeMillis();
          downX = currentX = ev.getRawX();
          downY = currentY = ev.getRawY();
          isDown = true;

          timerLongPress = Observable.timer(LONG_PRESS, TimeUnit.MILLISECONDS)
              .onBackpressureDrop()
              .subscribeOn(Schedulers.computation())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(time -> {
                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS &&
                    isDown &&
                    Math.abs(currentX - downX) < diffDown &&
                    Math.abs(currentY - downY) < diffDown &&
                    overallScrollY < scrollTolerance) {
                  int nbInRoom = viewLive.nbInRoom();

                  if (nbInRoom == LiveView.LIVE_MAX) {
                    Toast.makeText(getContext(),
                        getContext().getString(R.string.live_drop_friend_impossible,
                            LiveView.LIVE_MAX), Toast.LENGTH_SHORT).show();
                  } else {
                    currentTileView = viewLiveInvite.findViewByCoords(downX, downY);
                    if (currentTileView != null && currentTileView.getUser() != null) {
                      createTileForDrag();
                    }
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

        if ((!isOpenedPartially || !isOpenedFully || !isTouchInInviteView) &&
            currentTileView == null) {
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
        if (isEndCallOpened) closeEndCall();
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

        if (isEndCallOpened) break;

        if (pointerIndex != INVALID_POINTER) {
          float x = event.getX(pointerIndex) + location[0];
          float offsetX = x - lastDownX;
          float y = event.getY(pointerIndex) + location[1];
          float offsetY = y - lastDownY;

          if (currentTileView == null && !chatOpened && velocityTracker != null) {
            if (offsetX <= 0 && !isOpenedPartially) {
              applyOffsetRightWithTension(offsetX);
            } else if (offsetX >= 0 && !isOpenedPartially && !isOpenedFully && !isEndCallOpened) {
              applyOffsetLeftWithTension(offsetX);
            }

            velocityTracker.addMovement(event);
          } else if (currentTileView != null) {
            draggedTileView.getLocationOnScreen(tileLocationLast);
            int futurX = (int) (tileLocationStart[0] + offsetX);
            int futurY = (int) (tileLocationStart[1] + offsetY) - statusBarHeight;
            positionViewAt(draggedTileView, futurX, futurY);

            boolean isIn = ViewUtils.isIn(viewLiveDropZone, (int) x, (int) y);

            if (isIn) {
              startTileDrop();
              int[] locationOfRing = viewLiveDropZone.getLocationOfRing();
              int dx = (int) x - locationOfRing[0] - (viewLiveDropZone.getWidthOfRing() >> 1);
              int dy = (int) y - locationOfRing[1] - (viewLiveDropZone.getWidthOfRing() >> 1);

              double distance = Math.sqrt((dx * dx) + (dy * dy));
              if (initDistance == 0) initDistance = (float) distance;

              float scale = Math.min(Math.max((float) (TileInviteView.SCALE_MIN +
                      ((TileInviteView.SCALE_MAX - TileInviteView.SCALE_MIN) *
                          (1 - (distance / initDistance)))), TileInviteView.SCALE_MIN),
                  TileInviteView.SCALE_MAX);

              float scaleDrop = Math.min(Math.max(
                  (float) (1 + ((TileInviteView.SCALE_MAX - 1) * (1 - (distance / initDistance)))),
                  1), TileInviteView.SCALE_MAX);

              draggedTileView.scaleAvatar(scale);
              viewLiveDropZone.scaleRing(scaleDrop);
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

          if (isOpenedFully) {
            if (draggedTileView != null) removeTileForDrag();
            springRight.setCurrentValue(-viewLive.getLiveInviteViewFullWidth()).setAtRest();
            closeFullInviteView();
            clearTouch();
            break;
          }

          if (isEndCallOpened) {
            if (velocityTracker != null) velocityTracker.addMovement(event);
            springLeft.setCurrentValue(viewLiveHangUp.getMaxWidth()).setAtRest();
            closeEndCall();
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
            } else if (!isOpenedFully) {
              springRight.setCurrentValue(-viewLive.getLiveInviteViewPartialWidth()).setAtRest();
              openFullInviteView();
              clearTouch();
              break;
            }
          } else {
            if (isOpenedPartially) {
              springRight.setCurrentValue(-viewLive.getLiveInviteViewPartialWidth()).setAtRest();
              closePartialInviteView();
            } else if (!isOpenedFully && !isOpenedPartially) {
              springLeft.setCurrentValue(currentOffsetLeft).setAtRest();

              if (offsetX > thresholdEndCall) {
                openEndCall();
                clearTouch();
                break;
              } else {
                springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
              }
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
        viewLiveInvite.setDragging(false);

        if (!dropEnabled) {
          endTileDrag();
          AnimationUtils.animateLeftMargin(draggedTileView, tileLocationStart[0], DRAG_END_DELAY,
              new DecelerateInterpolator());
          AnimationUtils.animateTopMargin(draggedTileView, tileLocationStart[1] - statusBarHeight,
              DRAG_END_DELAY, new DecelerateInterpolator());
          prepareRemoveTileForDrag(DRAG_END_DELAY);
        } else {
          onDropped.onNext(currentTileView);
          int[] locationInviteView = new int[2];
          viewLiveInvite.getLocationOnScreen(locationInviteView);

          int xEnd = locationInviteView[0] - tileLocationLast[0] - currentTileView.getWidth() +
              (int) (currentTileView.getWidth() * 0.10f);
          int yEnd = screenUtils.getHeightPx() - tileLocationLast[1] - currentTileView.getHeight();
          int duration = draggedTileView.animateOnDrop(xEnd, yEnd);
          prepareRemoveTileForDrag(duration);
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

  private void createTileForDrag() {
    viewLiveInvite.setDragging(true);

    User user = currentTileView.getUser();

    draggedTileView = new TileInviteView(getContext());
    draggedTileView.setUser(user);
    currentTileView.getLocationOnScreen(tileLocationStart);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(currentTileView.getWidth(), currentTileView.getHeight());
    tileLocationStart[0] = tileLocationStart[0] +
        (currentTileView.getUser().isSelected() ? -screenUtils.dpToPx(10) : 0);
    tileLocationStart[1] = tileLocationStart[1] +
        (currentTileView.getUser().isSelected() ? screenUtils.dpToPx(10) : 0);
    params.leftMargin = tileLocationStart[0];
    params.topMargin = tileLocationStart[1] - statusBarHeight;
    addView(draggedTileView, params);
    draggedTileView.updateWidth(currentTileView.getRealWidth());

    currentTileView.setVisibility(View.GONE);

    startTileDrag();
  }

  private void positionViewAt(View view, int x, int y) {
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
    params.leftMargin = x;
    params.topMargin = y;
    draggedTileView.setLayoutParams(params);
    draggedTileView.requestLayout();
  }

  private void startTileDrag() {
    draggedTileView.startDrag();
    viewLiveDropZone.show();
    viewRinging.hide();
    onStartDrag.onNext(draggedTileView);
  }

  private void endTileDrag() {
    draggedTileView.endDrag();
    viewLiveDropZone.hide();
    viewRinging.show();
    onEndDrag.onNext(null);
  }

  private void startTileDrop() {
    dropEnabled = true;
    onDropZone.onNext(true);
  }

  private void endTileDrop() {
    dropEnabled = false;
    onDropZone.onNext(false);
  }

  private void prepareRemoveTileForDrag(int delay) {
    timerEndDrag = Observable.timer(delay, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(time -> {
          clearCurrentTile();
          viewLiveDropZone.hide();
          viewRinging.show();
          removeTileForDrag();
          timerEndDrag.unsubscribe();
        });
  }

  private void removeTileForDrag() {
    removeView(draggedTileView);
    draggedTileView = null;
  }

  private void clearCurrentTile() {
    if (currentTileView != null) {
      currentTileView.setVisibility(View.VISIBLE);
      currentTileView.getUser().setSelected(false);
      currentTileView.endDrag();
      currentTileView = null;
    }
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
    viewLive.applyTranslateX(value, true);
    viewLiveDropZone.applyTranslationX(value);
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
    return screenUtils.dpToPx(25);
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

  private class LeftSpringListener extends SimpleSpringListener {

    @Override public void onSpringUpdate(Spring spring) {
      if (ViewCompat.isAttachedToWindow(LiveContainer.this)) {
        float value = (float) spring.getCurrentValue();
        applyLeft(value);
      }
    }

    @Override public void onSpringActivate(Spring spring) {
      hasNotifiedAtRest = false;
    }
  }

  private void applyLeft(float value) {
    viewLiveHangUp.applyTranslationX(value);
    viewLive.applyTranslateX(value, false);
    viewLiveInvite.setTranslationX(value);
  }

  private boolean applyOffsetLeftWithTension(float offsetX) {
    final float scrollLeft = offsetX;
    currentOffsetLeft = (int) scrollLeft;
    float appliedValue = Math.min(Math.max(offsetX, 0), viewLiveHangUp.getMaxWidth());
    applyLeft(appliedValue);

    return true;
  }

  private void openEndCall() {
    isEndCallOpened = true;

    int endValue = viewLiveHangUp.getMaxWidth();
    if (velocityTracker != null) {
      springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(endValue);
    } else {
      springLeft.setEndValue(endValue);
    }

    viewLiveHangUp.showEndCall();
  }

  private void closeEndCall() {
    isEndCallOpened = false;

    viewLiveHangUp.hideEndCall();

    if (velocityTracker != null) {
      springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
    } else {
      springLeft.setEndValue(0);
    }
  }

  private void endCallSuccess() {
    springLeft.setEndValue(screenUtils.getWidthPx());
    AnimationUtils.fadeOut(viewLiveHangUp, DURATION);
    subscriptions.add(Observable.timer(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> onEndCall.onNext(null)));
  }

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////

  public Observable<Integer> onEventChange() {
    return onEventChange;
  }
}