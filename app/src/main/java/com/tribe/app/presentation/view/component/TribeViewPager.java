package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewTreeObserver;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.CustomViewPager;

public class TribeViewPager extends CustomViewPager {

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springLeft;
    private Spring springRight;
    private Spring springTop;
    private Spring springBottom;
    private LeftSpringListener springLeftListener;
    private RightSpringListener springRightListener;
    private TopSpringListener springTopListener;
    private BottomSpringListener springBottomListener;

    // VARIABLES
    private int countItems;

    // DIMENS
    private int thresholdEnd;

    // TOUCH HANDLING
    private float lastDownX;
    private float lastDownXTr;
    private int activePointerId;
    private VelocityTracker velocityTracker;

    public TribeViewPager(Context context) {
        super(context);
    }

    public TribeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        springLeft.addListener(springLeftListener);
        //springRight.addListener(springRightListener);
        //springTop.addListener(springTopListener);
        //springBottom.addListener(springBottomListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        springLeft.removeListener(springLeftListener);
        //springRight.addListener(springRightListener);
        //springTop.addListener(springTopListener);
        //springBottom.addListener(springBottomListener);
    }

    @Override
    protected void postInitViewPager() {
        super.postInitViewPager();

        springSystem = SpringSystem.create();

        springLeft = springSystem.createSpring();
        springRight = springSystem.createSpring();
        springTop = springSystem.createSpring();
        springBottom = springSystem.createSpring();

        springLeftListener = new LeftSpringListener();
        springRightListener = new RightSpringListener();
        springTopListener = new TopSpringListener();
        springBottomListener = new BottomSpringListener();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                springLeft.setCurrentValue(-getWidth());
                springRight.setCurrentValue(-getWidth());
                springTop.setCurrentValue(-getWidth());
                springBottom.setCurrentValue(-getWidth());

                postDelayed(() -> {
                    springLeft.setEndValue(0);
                    springRight.setEndValue(0);
                    springTop.setEndValue(0);
                    springBottom.setEndValue(0);
                }, 500);
            }
        });

        thresholdEnd = getContext().getResources().getDimensionPixelSize(R.dimen.threshold_end_tribe);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isSwipeAllowed(event)) {
            if (getCurrentItem() == countItems - 1) {
                int action = event.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        activePointerId = event.getPointerId(0);
                        lastDownXTr = getTranslationX();
                        lastDownX = event.getRawX();

                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(event);

                        springLeft.setCurrentValue(lastDownXTr);
                        springRight.setCurrentValue(lastDownXTr);
                        springTop.setCurrentValue(lastDownXTr);
                        springBottom.setCurrentValue(lastDownXTr);

                        break;
                    case MotionEvent.ACTION_MOVE: {
                        final int pointerIndex = event.findPointerIndex(activePointerId);

                        if (pointerIndex != -1) {
                            final int location[] = {0, 0};
                            getLocationOnScreen(location);
                            float x = event.getX(pointerIndex) + location[0];
                            float offset = x - lastDownX + lastDownXTr;
                            springLeft.setCurrentValue(offset);
                            //springRight.setCurrentValue(offset);
                            //springTop.setCurrentValue(offset);
                            //springBottom.setCurrentValue(offset);
                            //velocityTracker.addMovement(event);
                        }
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        final int pointerIndex = event.findPointerIndex(activePointerId);
                        if (pointerIndex != -1) {
                            velocityTracker.addMovement(event);
                            velocityTracker.computeCurrentVelocity(10000);

                            final int location[] = {0, 0};
                            getLocationOnScreen(location);
                            float x = event.getX(pointerIndex) + location[0];
                            float offset = x - lastDownX + lastDownXTr;

                            if (offset < -thresholdEnd)
                                springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(-getWidth());
                            else
                                springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
                            //springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
                            //springTop.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
                            //springBottom.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
                        }

                        break;
                }
                return true;
            } else {
                return super.onTouchEvent(event);
            }
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

    public void setCount(int count) {
        this.countItems = count;
    }

    private class LeftSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            setTranslationX(value);
        }
    }

    private class RightSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            setTranslationX(value);
        }
    }

    private class TopSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            setTranslationX(value);
        }
    }

    private class BottomSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            setTranslationX(value);
        }
    }
}