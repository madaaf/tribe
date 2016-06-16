package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import com.tribe.app.presentation.view.utils.ScrollerCustomDuration;

import java.lang.reflect.Field;

public class CustomViewPager extends ViewPager {

    public static final int SWIPE_MODE_ALL = 0;
    public static final int SWIPE_MODE_LEFT = 1;
    public static final int SWIPE_MODE_RIGHT = 2;
    public static final int SWIPE_MODE_DOWN = 3;
    public static final int SWIPE_MODE_UP = 4;
    public static final int SWIPE_MODE_NONE = 5;

    @IntDef({SWIPE_MODE_ALL, SWIPE_MODE_LEFT, SWIPE_MODE_RIGHT, SWIPE_MODE_DOWN, SWIPE_MODE_UP, SWIPE_MODE_NONE})
    public @interface SwipeDirection {}

    private ScrollerCustomDuration scroller = null;
    private @SwipeDirection int swipeDirection;
    private float downX, downY;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        postInitViewPager();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isSwipeAllowed(event)) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.isSwipeAllowed(event)) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    protected boolean isSwipeAllowed(MotionEvent event) {
        if (this.swipeDirection == SWIPE_MODE_ALL) return true;

        if (this.swipeDirection == SWIPE_MODE_NONE) return false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                float diffX = event.getX() - downX;

                if (diffX > 0 && swipeDirection == SWIPE_MODE_RIGHT) {
                    return false;
                } else if (diffX < 0 && swipeDirection == SWIPE_MODE_LEFT) {
                    return false;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

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
}