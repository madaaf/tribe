package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ScrollerCustomDuration;

import java.lang.reflect.Field;

public class CustomViewPager extends ViewPager {

    public static final int SWIPING_THRESHOLD = 20;

    public static final int SWIPE_MODE_ALL = 0;
    public static final int SWIPE_MODE_LEFT = 1;
    public static final int SWIPE_MODE_RIGHT = 2;
    public static final int SWIPE_MODE_DOWN = 3;
    public static final int SWIPE_MODE_UP = 4;
    public static final int SWIPE_MODE_NONE = 5;
    public static final int SWIPE_MODE_UNKNOWN = -1;

    @IntDef({SWIPE_MODE_ALL, SWIPE_MODE_LEFT, SWIPE_MODE_RIGHT, SWIPE_MODE_DOWN, SWIPE_MODE_UP, SWIPE_MODE_NONE, SWIPE_MODE_UNKNOWN})
    public @interface SwipeDirection {}

    // VARIABLES
    private ScreenUtils screenUtils;
    private ScrollerCustomDuration scroller = null;
    private @SwipeDirection int swipeDirection;
    protected @SwipeDirection int currentSwipeDirection;
    protected boolean isInMotion = false;
    private float downX, downY;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        postInitViewPager();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    public boolean computeSwipeDirection(MotionEvent event) {
        if (this.swipeDirection == SWIPE_MODE_NONE) return false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            isInMotion = false;
            currentSwipeDirection = SWIPE_MODE_UNKNOWN;
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                float diffY = event.getY() - downY;
                float diffX = event.getX() - downX;

                final boolean isSwipingHorizontally = Math.abs(diffX) > screenUtils.dpToPx(SWIPING_THRESHOLD);
                final boolean isSwipingVertically = Math.abs(diffY) > screenUtils.dpToPx(SWIPING_THRESHOLD);

                if (isSwipingHorizontally && (!isInMotion || currentSwipeDirection == SWIPE_MODE_RIGHT || currentSwipeDirection == SWIPE_MODE_LEFT)) {
                    if (diffX < 0) {
                        return setCurrentSwipeDirection(SWIPE_MODE_RIGHT);
                    } else {
                        return setCurrentSwipeDirection(SWIPE_MODE_LEFT);
                    }
                }

                if (isSwipingVertically && (!isInMotion || currentSwipeDirection == SWIPE_MODE_DOWN || currentSwipeDirection == SWIPE_MODE_UP)) {
                    if (diffY > 0) {
                        return setCurrentSwipeDirection(SWIPE_MODE_DOWN);
                    } else {
                        return setCurrentSwipeDirection(SWIPE_MODE_UP);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return true;
    }

    private boolean setCurrentSwipeDirection(@SwipeDirection int newSwipeDirection) {
        isInMotion = true;
        currentSwipeDirection = newSwipeDirection;
        return true;
    }

    public void setAllowedSwipeDirection(@SwipeDirection int direction) {
        this.swipeDirection = direction;
    }

    /**
     * Override the Scroller instance with our own class so we can change the
     * duration
     */
    protected void postInitViewPager() {
        screenUtils = ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().screenUtils();

        try {
            Field rScroller = ViewPager.class.getDeclaredField("mScroller");
            rScroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            scroller = new ScrollerCustomDuration(getContext(),
                    (Interpolator) interpolator.get(null));
            rScroller.set(this, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor) {
        scroller.setScrollDurationFactor(scrollFactor);
    }

    public int getCurrentSwipeDirection() {
        return currentSwipeDirection;
    }
}