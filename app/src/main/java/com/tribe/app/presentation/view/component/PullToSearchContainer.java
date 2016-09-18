package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class PullToSearchContainer extends FrameLayout {

    private static final SpringConfig PULL_TO_SEARCH_BOUNCE_SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(132, 7f);
    private static final SpringConfig PULL_TO_SEARCH_SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(200, 17);
    private static final float DRAG_RATE = 0.5f;
    private static final int DRAG_THRESHOLD = 20;
    private static final int INVALID_POINTER = -1;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerView;

    @BindView(R.id.ptsView)
    PullToSearchView ptsView;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springTop;
    private TopSpringListener springTopListener;

    // VARIABLES
    private ScreenUtils screenUtils;
    private float currentDragPercent;
    private boolean beingDragged = false;
    private boolean pullToSearchActive = false;
    private float lastDownX;
    private float lastDownXTr;
    private float lastDownY;
    private float lastDownYTr;
    private int activePointerId;
    private VelocityTracker velocityTracker;
    private int touchSlop;
    private int currentOffsetTop;

    // DIMENS
    private int thresholdEnd;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Boolean> pullToSearchActiveSubject = PublishSubject.create();

    public PullToSearchContainer(Context context) {
        super(context);
    }

    public PullToSearchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        springTop.addListener(springTopListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        springTop.addListener(springTopListener);
        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        unbinder = ButterKnife.bind(this);

        ApplicationComponent applicationComponent = ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
        applicationComponent.inject(this);
        screenUtils = applicationComponent.screenUtils();

        initDimen();
        initUI();
    }

    private void initUI() {
        springSystem = SpringSystem.create();
        springTop = springSystem.createSpring();
        springTop.setSpringConfig(PULL_TO_SEARCH_BOUNCE_SPRING_CONFIG);
        springTopListener = new TopSpringListener();
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private void initDimen() {
        thresholdEnd = getContext().getResources().getDimensionPixelSize(R.dimen.threshold_dismiss);
    }

    public boolean beingDragged() {
        return beingDragged;
    }

    public void updatePTSList(List<Recipient> recipientList) {
        ptsView.updatePTSList(recipientList);
    }

    ///////////////////////
    //    TOUCH EVENTS   //
    ///////////////////////
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || canChildScrollUp()) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        if (ev.getRawY() < screenUtils.dpToPx(DRAG_THRESHOLD)) return false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // RESET
                if (!pullToSearchActive)
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

                if (!pullToSearchActive && isSwipingVertically && diffY > touchSlop && !beingDragged) {
                    beingDragged = true;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                beingDragged = false;
                activePointerId = INVALID_POINTER;
                break;
        }

        return beingDragged || pullToSearchActive;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        final int location[] = {0, 0};
        getLocationOnScreen(location);

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE : {
                final int pointerIndex = event.findPointerIndex(activePointerId);

                if (pointerIndex != INVALID_POINTER) {
                    float y = event.getY(pointerIndex) + location[1];
                    float offsetY = y - lastDownY + lastDownYTr;

                    if (offsetY >= 0 && !pullToSearchActive)
                        applyOffsetTopWithTension(offsetY);

                    velocityTracker.addMovement(event);
                }

                break;
            }

            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL: {
                final int pointerIndex = event.findPointerIndex(activePointerId);

                if (pointerIndex != INVALID_POINTER) {
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1000);

                    float y = event.getY(pointerIndex) - location[1];
                    float offsetY = y - lastDownY + lastDownYTr;

                    if (!pullToSearchActive) {
                        if (offsetY >= 0) {
                            springTop.setCurrentValue(currentOffsetTop);

                            if (offsetY > thresholdEnd) {
                                openPullToSearch();
                            } else {
                                springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                            }
                        }
                    } else {
                        closePullToSearch();
                    }
                }

                break;
            }
        }

        return true;
    }

    private boolean canChildScrollUp() {
        return ViewCompat.canScrollVertically(recyclerView, -1);
    }

    ///////////////////////
    //    ANIMATIONS     //
    ///////////////////////
    private class TopSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();
                scrollTop(value);
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            super.onSpringAtRest(spring);
            if (spring.getEndValue() == 0) {
                pullToSearchActiveSubject.onNext(pullToSearchActive = false);
            } else {
                pullToSearchActiveSubject.onNext(pullToSearchActive = true);
            }
        }
    }

    private void scrollTop(float value) {
        recyclerView.setTranslationY(value);
    }

    private boolean applyOffsetTopWithTension(float offsetY) {
        float totalDragDistance = getHeight() / 3;
        final float scrollTop = offsetY * DRAG_RATE;
        currentDragPercent = scrollTop / totalDragDistance;

        if (currentDragPercent < 0) {
            return false;
        }

        currentOffsetTop = computeOffsetWithTension(scrollTop, totalDragDistance);
        scrollTop(currentOffsetTop);

        return true;
    }

    private int computeOffsetWithTension(float scrollDist, float totalDragDistance) {
        float boundedDragPercent = Math.min(1f, Math.abs(currentDragPercent));
        float extraOS = Math.abs(scrollDist) - totalDragDistance;
        float slingshotDist = totalDragDistance;
        float tensionSlingshotPercent = Math.max(0,
                Math.min(extraOS, slingshotDist * 2) / slingshotDist);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                (tensionSlingshotPercent / 4), 2)) * 2f;
        float extraMove = (slingshotDist) * tensionPercent / 2;
        return (int) ((slingshotDist * boundedDragPercent) + extraMove);
    }

    private void openPullToSearch() {
        springTop.setSpringConfig(PULL_TO_SEARCH_BOUNCE_SPRING_CONFIG);
        springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(getHeight() - thresholdEnd);
    }

    private void closePullToSearch() {
        //springTop.setSpringConfig(PULL_TO_SEARCH_SPRING_CONFIG);
        //springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
    }

    ///////////////////////
    //    OBSERVABLES    //
    ///////////////////////
    public Observable<Boolean> pullToSearchActive() {
        return pullToSearchActiveSubject;
    }
}