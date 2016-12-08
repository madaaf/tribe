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
import com.tribe.app.domain.entity.Message;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class TopBarContainer extends FrameLayout {

    public static final int MIN_LENGTH = 1250; // ms

    private static final SpringConfig PULL_TO_SEARCH_SPRING_CONFIG = SpringConfig.fromBouncinessAndSpeed(0f, 5f);
    private static final float DRAG_RATE = 0.5f;
    private static final int DRAG_THRESHOLD = 20;
    private static final int INVALID_POINTER = -1;

    @Inject
    SoundManager soundManager;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerView;

    @BindView(R.id.topBarView)
    TopBarView topBarView;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springTop;
    private TopSpringListener springTopListener;

    // VARIABLES
    private ScreenUtils screenUtils;
    private float currentDragPercent;
    private boolean beingDragged = false;
    private float lastDownX;
    private float lastDownXTr;
    private float lastDownY;
    private float lastDownYTr;
    private int activePointerId;
    private VelocityTracker velocityTracker;
    private int touchSlop;
    private int currentOffsetTop;
    private boolean isRefreshing = false;

    // DIMENS
    private int thresholdEnd;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Boolean> onRefresh = PublishSubject.create();
    private PublishSubject<Void> clickSettings = PublishSubject.create();
    private PublishSubject<Void> clickSearch = PublishSubject.create();
    private PublishSubject<Void> clickInvites = PublishSubject.create();
    private PublishSubject<Void> clickGroups = PublishSubject.create();

    public TopBarContainer(Context context) {
        super(context);
    }

    public TopBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        springTop.addListener(springTopListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        springTop.removeListener(springTopListener);

        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDetachedFromWindow();
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
        initSubscriptions();
    }

    private void initUI() {
        springSystem = SpringSystem.create();
        springTop = springSystem.createSpring();
        springTop.setSpringConfig(PULL_TO_SEARCH_SPRING_CONFIG);
        springTopListener = new TopSpringListener();
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        recyclerView.setOnTouchListener((v, event) -> {
            int dy = recyclerView.computeVerticalScrollOffset();
            if (event.getY() < topBarView.getHeight() && dy < (topBarView.getHeight() >> 1)) return true;

            return super.onTouchEvent(event);
        });
    }

    private void initDimen() {
        thresholdEnd = getContext().getResources().getDimensionPixelSize(R.dimen.threshold_dismiss);
    }

    private void initSubscriptions() {
        subscriptions.add(
                topBarView.onClickRefresh()
                    .subscribe(aVoid -> {
                        setRefreshing(true);
                    })
        );

        subscriptions.add(
                topBarView.onErrorDone()
                        .subscribe(aVoid -> {
                            setRefreshing(false);
                        })
        );

        subscriptions.add(
                topBarView.onClickGroups()
                        .subscribe(clickGroups)
        );

        subscriptions.add(
                topBarView.onClickInvites()
                        .subscribe(clickInvites)
        );

        subscriptions.add(
                topBarView.onClickSearch()
                        .subscribe(clickSearch)
        );

        subscriptions.add(
                topBarView.onClickSettings()
                        .subscribe(clickSettings)
        );
    }

    public boolean beingDragged() {
        return beingDragged;
    }

    public boolean isRefreshing() { return isRefreshing; }

    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;

        if (!isRefreshing) {
            beingDragged = false;
            topBarView.reset();
            soundManager.playSound(SoundManager.END_REFRESH, SoundManager.SOUND_LOW);
        } else {
            refresh();
        }
    }

    private void refresh() {
        soundManager.playSound(SoundManager.START_REFRESH, SoundManager.SOUND_LOW);
        onRefresh.onNext(true);
    }

    public void showError() {
        topBarView.showError();
    }

    public void showNewMessages(List<Message> newMessages) {
        if (!isRefreshing) topBarView.showNewMessages(newMessages);
    }

    ///////////////////////
    //    TOUCH EVENTS   //
    ///////////////////////
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isTouchInTopBar = ev.getRawY() < topBarView.getHeight();
        if (!isEnabled() || canChildScrollUp() || isRefreshing || isTouchInTopBar) {
            if (isTouchInTopBar)
                topBarView.onTouchEvent(ev);

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

                if (isSwipingVertically && diffY > touchSlop && diffY > screenUtils.dpToPx(DRAG_THRESHOLD) && !beingDragged) {
                    beingDragged = true;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        final int location[] = {0, 0};
        getLocationOnScreen(location);

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE : {
                final int pointerIndex = event.findPointerIndex(activePointerId);

                if (pointerIndex != INVALID_POINTER && velocityTracker != null) {
                    float y = event.getY(pointerIndex) + location[1];
                    float offsetY = y - lastDownY + lastDownYTr;

                    if (offsetY >= 0)
                        applyOffsetTopWithTension(offsetY);

                    velocityTracker.addMovement(event);
                }

                break;
            }

            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL: {
                final int pointerIndex = event.findPointerIndex(activePointerId);

                activePointerId = INVALID_POINTER;

                if (pointerIndex != INVALID_POINTER && velocityTracker != null) {
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1000);

                    float y = event.getY(pointerIndex) - location[1];
                    float offsetY = y - lastDownY + lastDownYTr;

                    if (offsetY >= 0) {
                        springTop.setCurrentValue(currentOffsetTop);
                        springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
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
            if (ViewCompat.isAttachedToWindow(TopBarContainer.this)) {
                float value = (float) spring.getCurrentValue();
                translateTop(value);
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            super.onSpringAtRest(spring);
        }
    }

    private void translateTop(float value) {
        recyclerView.setTranslationY(value);

        if (beingDragged && !isRefreshing) {
            isRefreshing = topBarView.animatePull(value, 0, getTotalDragDistance());
            if (isRefreshing) refresh();
        }
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

    private float getTotalDragDistance() {
        return getHeight() / 8;
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

    ///////////////////////
    //    OBSERVABLES    //
    ///////////////////////

    public Observable<Boolean> onRefresh() {
        return onRefresh;
    }

    public Observable<Void> onClickSettings() {
        return clickSettings;
    }

    public Observable<Void> onClickSearch() {
        return clickSearch;
    }

    public Observable<Void> onClickInvites() {
        return clickInvites;
    }

    public Observable<Void> onClickGroups() {
        return clickGroups;
    }
}