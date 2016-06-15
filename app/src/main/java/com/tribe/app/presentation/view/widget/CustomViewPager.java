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

    public static final int ALL = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int DOWN = 3;
    public static final int UP = 4;
    public static final int NONE = 5;

    @IntDef({ALL, LEFT, RIGHT, DOWN, UP, NONE})
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

    private boolean isSwipeAllowed(MotionEvent event) {
        if (this.swipeDirection == ALL) return true;

        if (this.swipeDirection == NONE) return false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                float diffX = event.getX() - downX;

                if (diffX > 0 && swipeDirection == RIGHT) {
                    return false;
                } else if (diffX < 0 && swipeDirection == LEFT) {
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
    private void postInitViewPager() {
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