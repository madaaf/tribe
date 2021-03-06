package com.tribe.app.presentation.view.component.home;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
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
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TooltipView;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class TopBarContainer extends FrameLayout {

  private static final SpringConfig PULL_TO_SEARCH_SPRING_CONFIG =
      SpringConfig.fromBouncinessAndSpeed(0f, 5f);
  private static final float DRAG_RATE = 0.5f;
  private static final int DRAG_THRESHOLD = 20;
  private static final int INVALID_POINTER = -1;
  public static final int MIN_LENGTH = 1800; // ms

  @Inject SoundManager soundManager;

  @Inject StateManager stateManager;

  @BindView(R.id.recyclerViewFriends) RecyclerView recyclerView;

  @BindView(R.id.topBarView) TopBarView topBarView;

  @BindView(R.id.viewTopBarLogo) TopBarLogoView viewTopBarLogo;

  @BindView(R.id.txtNewContacts) TextViewFont txtNewContacts;

  // VARIABLES
  private ScreenUtils screenUtils;
  private float currentDragPercent;
  private boolean beingDragged = false;
  private float lastDownX, lastDownXTr, lastDownY, lastDownYTr;
  private int activePointerId;
  private VelocityTracker velocityTracker;
  private int touchSlop;
  private int currentOffsetTop;
  private boolean dropPullToRefresh = false, isRefreshing = false, wasRefreshing = false;

  private TooltipView tooltipView;
  private FrameLayout.LayoutParams tooltipParams;
  private int scrollThresholdFloatingButton = 0;

  // SPRINGS
  private SpringSystem springSystem = null;
  private Spring springTop;
  private TopSpringListener springTopListener;

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onRefresh = PublishSubject.create();
  private PublishSubject<Pair<Integer, Boolean>> onNewContactsInfos = PublishSubject.create();

  public TopBarContainer(Context context) {
    super(context);
  }

  public TopBarContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    springTop.addListener(springTopListener);
  }

  @Override protected void onDetachedFromWindow() {
    springTop.removeListener(springTopListener);
    unbinder.unbind();

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
    springTop = springSystem.createSpring();
    springTop.setSpringConfig(PULL_TO_SEARCH_SPRING_CONFIG);
    springTopListener = new TopSpringListener();
    touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    scrollThresholdFloatingButton =
        screenUtils.getWidthPx() / getResources().getInteger(R.integer.columnNumber);

    recyclerView.setOnTouchListener((v, event) -> {
      int dy = recyclerView.computeVerticalScrollOffset();
      if (event.getY() < topBarView.getHeight() && dy < (topBarView.getHeight() >> 1)) return true;

      return super.onTouchEvent(event);
    });

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      private int overallYScroll = 0;

      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        overallYScroll = overallYScroll + dy;

        if (tooltipView != null && ViewCompat.isAttachedToWindow(tooltipView)) {
          if (overallYScroll > 0) {
            tooltipView.setVisibility(View.GONE);
          } else if (overallYScroll == 0) {
            tooltipView.setVisibility(View.VISIBLE);
          }
        }
      }
    });
  }

  private void initDimen() {

  }

  private void initSubscriptions() {
    topBarView.initNewContactsObs(onNewContactsInfos);
  }

  public boolean isSearchMode() {
    return topBarView.isSearchMode();
  }

  public void openSearch() {
    topBarView.animateSearch();
  }

  public void closeSearch() {
    topBarView.closeSearch();
  }

  public void initNewContactsObs(Observable<Pair<Integer, Boolean>> obsContactList) {
    obsContactList.observeOn(AndroidSchedulers.mainThread()).subscribe(integerBooleanPair -> {
      onNewContactsInfos.onNext(integerBooleanPair);
    });
  }

  public void reloadUserUI() {
    topBarView.reloadUserUI();
  }

  ///////////////////////
  //    TOUCH EVENTS   //
  ///////////////////////

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    boolean isTouchInTopBar = ev.getRawY() < topBarView.getHeight();
    if (!isEnabled() || canChildScrollUp() || isTouchInTopBar) {
      if (isTouchInTopBar) topBarView.onTouchEvent(ev);

      return false;
    }

    final int action = MotionEventCompat.getActionMasked(ev);

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        // RESET
        springTop.setCurrentValue(0).setAtRest();

        activePointerId = ev.getPointerId(0);
        beingDragged = false;

        lastDownXTr = getTranslationX();
        lastDownX = ev.getRawX();

        lastDownYTr = getTranslationY();
        lastDownY = ev.getRawY();

        velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(ev);

        break;
      case MotionEvent.ACTION_MOVE:
        if (activePointerId == INVALID_POINTER) {
          return false;
        }

        float diffY = ev.getY() - lastDownY;
        float diffX = ev.getX() - lastDownX;

        final boolean isSwipingVertically = Math.abs(diffY) > Math.abs(diffX);

        if (isSwipingVertically &&
            diffY > touchSlop &&
            diffY > screenUtils.dpToPx(DRAG_THRESHOLD) &&
            !beingDragged) {
          beingDragged = true;
          viewTopBarLogo.setVisibility(View.VISIBLE);
          viewTopBarLogo.reset();
          viewTopBarLogo.setAlpha(0);
        }

        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        beingDragged = false;
        activePointerId = INVALID_POINTER;
        break;
    }

    return beingDragged;
  }

  private boolean canChildScrollUp() {
    return ViewCompat.canScrollVertically(recyclerView, -1);
  }

  public void onSyncDone() {
    topBarView.onSyncDone();
  }

  public void onSyncStart() {
    topBarView.onSyncStart();
  }

  public void onSyncError() {
    topBarView.onSyncError();
  }

  public void endRefresh() {
    isRefreshing = false;
    dropPullToRefresh = false;
    wasRefreshing = true;
    springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
    //viewTopBarLogo.endRefresh();
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  private class TopSpringListener extends SimpleSpringListener {
    @Override public void onSpringUpdate(Spring spring) {
      if (ViewCompat.isAttachedToWindow(TopBarContainer.this)) {
        float value = (float) spring.getCurrentValue();
        translateTop(value);
      }
    }

    @Override public void onSpringAtRest(Spring spring) {
      super.onSpringAtRest(spring);
      if (!beingDragged) viewTopBarLogo.setVisibility(View.GONE);
      if (wasRefreshing) {
        wasRefreshing = false;
      }
    }
  }

  private void translateTop(float value) {
    recyclerView.setTranslationY(value);
    float alphaValue = (value / getTotalDragDistance());
    topBarView.setAlpha(1 - alphaValue);
    viewTopBarLogo.setAlpha(alphaValue);
    topBarView.setTranslationY(value * 1.25f);
    //if (wasRefreshing) viewTopBarLogo.setTranslation(value);
    if (tooltipView != null && ViewCompat.isAttachedToWindow(tooltipView)) {
      tooltipView.setAlpha(1 - alphaValue);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();

    final int location[] = { 0, 0 };
    getLocationOnScreen(location);

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = event.findPointerIndex(activePointerId);

        if (pointerIndex != INVALID_POINTER && velocityTracker != null) {
          float y = event.getY(pointerIndex) + location[1];
          float offsetY = y - lastDownY + lastDownYTr;
          if (offsetY >= 0) applyOffsetTopWithTension(offsetY);
          dropPullToRefresh = false;
          velocityTracker.addMovement(event);
        }

        break;
      }

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL: {
        final int pointerIndex = event.findPointerIndex(activePointerId);
        dropPullToRefresh = true;
        activePointerId = INVALID_POINTER;

        if (pointerIndex != INVALID_POINTER && velocityTracker != null) {
          velocityTracker.addMovement(event);
          velocityTracker.computeCurrentVelocity(1000);

          float y = event.getY(pointerIndex) - location[1];
          float offsetY = y - lastDownY + lastDownYTr;
          final float overScrollTop = (y - lastDownY + lastDownYTr) * DRAG_RATE;

          springTop.setCurrentValue(currentOffsetTop);

          if (overScrollTop >= getTotalDragDistance()) {
            springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(currentOffsetTop);

            if (!isRefreshing) {
              onRefresh.onNext(true);
              isRefreshing = true;
              viewTopBarLogo.startRefresh(getTotalDragDistance() + topBarView.getHeight());
            }
          } else {
            springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
          }
        }

        break;
      }
    }

    return true;
  }

  private float getTotalDragDistance() {
    return getHeight() / 10;
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

  private boolean applyOffsetTopWithTension(float offsetY) {
    float totalDragDistance = getTotalDragDistance();
    final float scrollTop = offsetY * DRAG_RATE;
    currentDragPercent = scrollTop / totalDragDistance;

    if (currentDragPercent < 0) {
      return false;
    }

    currentOffsetTop = computeOffsetWithTension(scrollTop, totalDragDistance);
    translateTop(currentOffsetTop);

    return true;
  }

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////

  public Observable<Boolean> onRefresh() {
    return onRefresh;
  }

  public Observable<String> onSearch() {
    return topBarView.onSearch();
  }

  public Observable<Void> onSyncContacts() {
    return topBarView.onSyncContacts();
  }

  public Observable<Void> onClickProfile() {
    return topBarView.onClickProfile();
  }

  public Observable<Void> onBack() {
    return topBarView.onBack();
  }

  public Observable<Boolean> onOpenCloseSearch() {
    return topBarView.onOpenCloseSearch().doOnNext(aBoolean -> {
      if (aBoolean && tooltipView != null) {
        removeView(tooltipView);
        tooltipView = null;
      }
    });
  }
}