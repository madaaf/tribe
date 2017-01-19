package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveContainer extends FrameLayout {

    private static final SpringConfig ORIGAMI_SPRING_CONFIG = SpringConfig.fromBouncinessAndSpeed(0f, 20f);
    private static final float DRAG_RATE = 0.1f;
    private static final int DRAG_THRESHOLD = 20;
    private static final int INVALID_POINTER = -1;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.viewLive)
    LiveView liveView;

    @BindView(R.id.viewShadowRight)
    View viewShadowRight;

    @BindView(R.id.viewInviteLive)
    LiveInviteView liveInviteView;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springRight;
    private RightSpringListener springRightListener;

    // VARIABLES
    private float currentDragPercent;
    private boolean beingDragged = false;
    private float lastDownX;
    private float lastDownXTr;
    private float lastDownY;
    private float lastDownYTr;
    private int activePointerId;
    private VelocityTracker velocityTracker;
    private int touchSlop;
    private int currentOffsetRight;
    private boolean isOpened = false;

    // DIMENS
    private int thresholdEnd;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public LiveContainer(Context context) {
        super(context);
    }

    public LiveContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        springRight.addListener(springRightListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        springRight.removeListener(springRightListener);

        if (unbinder != null) unbinder.unbind();

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
        springRight = springSystem.createSpring();
        springRight.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springRightListener = new RightSpringListener();
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        setBackgroundResource(R.color.grey_background_profile_info);

//        liveView.setOnTouchListener((v, event) -> {
//            int dy = recyclerView.computeVerticalScrollOffset();
//            if (event.getY() < topBarView.getHeight() && dy < (topBarView.getHeight() >> 1)) return true;
//
//            return super.onTouchEvent(event);
//        });
    }

    private void initDimen() {
        thresholdEnd = getContext().getResources().getDimensionPixelSize(R.dimen.threshold_open_live_invite);
    }

    private void initSubscriptions() {

    }

    public boolean beingDragged() {
        return beingDragged;
    }

    public boolean isOpened() { return isOpened; }

    ///////////////////////
    //    TOUCH EVENTS   //
    ///////////////////////
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            // TODO CONTROL IF TOUCH IS IN LIVEINVITEVIEW
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // RESET
                if (!isOpened) {
                    springRight.setCurrentValue(0).setAtRest();
                }

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

                final boolean isSwipingHorizontally = Math.abs(diffX) > Math.abs(diffY);

                if (isSwipingHorizontally && Math.abs(diffX) > touchSlop && Math.abs(diffX) > screenUtils.dpToPx(DRAG_THRESHOLD) && !beingDragged) {
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
                    float x = event.getX(pointerIndex) + location[0];
                    float offsetX = x - lastDownX + lastDownXTr;

                    if (offsetX <= 0 && !isOpened)
                        applyOffsetRightWithTension(offsetX);

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

                    if (isOpened) {
                        closeInviteView();
                        break;
                    }

                    float x = event.getX(pointerIndex) - location[0];
                    float offsetX = x - lastDownX + lastDownXTr;

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
        @Override
        public void onSpringUpdate(Spring spring) {
            if (ViewCompat.isAttachedToWindow(LiveContainer.this)) {
                float value = (float) spring.getCurrentValue();
                applyRight(Math.min(Math.max(value, -liveInviteView.getWidth()), 0));
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            super.onSpringAtRest(spring);
        }
    }

    private void applyRight(float value) {
        ViewGroup.LayoutParams params = liveView.getLayoutParams();
        params.width = (int) (screenUtils.getWidthPx() + value);
        liveView.setLayoutParams(params);
        liveView.requestLayout();
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
        springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(-liveInviteView.getWidth());
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
}