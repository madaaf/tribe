package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.CustomViewPager;

import rx.Observable;
import rx.subjects.PublishSubject;

public class TribeViewPager extends CustomViewPager {

    private static final SpringConfig ORIGAMI_SPRING_BOUNCE_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(132, 13);
    private static final SpringConfig ORIGAMI_SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(70, 11);

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
    private int backToMessageHeight;
    private int snoozeModeWidth;

    // TOUCH HANDLING
    private float lastDownX;
    private float lastDownXTr;
    private float lastDownY;
    private float lastDownYTr;
    private int activePointerId;
    private VelocityTracker velocityTracker;

    // CALLBACKS
    private final PublishSubject<Void> onDismissHorizontal = PublishSubject.create();
    private final PublishSubject<Void> onDismissVertical = PublishSubject.create();

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
        springRight.addListener(springRightListener);
        springTop.addListener(springTopListener);
        springBottom.addListener(springBottomListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        springLeft.removeListener(springLeftListener);
        springRight.addListener(springRightListener);
        springTop.addListener(springTopListener);
        springBottom.addListener(springBottomListener);
    }

    @Override
    protected void postInitViewPager() {
        super.postInitViewPager();

        springSystem = SpringSystem.create();

        springLeft = springSystem.createSpring();
        springRight = springSystem.createSpring();
        springTop = springSystem.createSpring();
        springBottom = springSystem.createSpring();

        springLeft.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springRight.setSpringConfig(ORIGAMI_SPRING_BOUNCE_CONFIG);
        springTop.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springBottom.setSpringConfig(ORIGAMI_SPRING_CONFIG);

        springLeftListener = new LeftSpringListener();
        springRightListener = new RightSpringListener();
        springTopListener = new TopSpringListener();
        springBottomListener = new BottomSpringListener();

        thresholdEnd = getContext().getResources().getDimensionPixelSize(R.dimen.threshold_end_tribe);
        backToMessageHeight = getContext().getResources().getDimensionPixelSize(R.dimen.back_to_message_height);
        snoozeModeWidth = getContext().getResources().getDimensionPixelSize(R.dimen.snooze_mode_width);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isSwipeAllowed(event)) {
            int action = event.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        activePointerId = event.getPointerId(0);
                        lastDownXTr = getTranslationX();
                        lastDownX = event.getRawX();

                        lastDownYTr = getTranslationY();
                        lastDownY = event.getRawY();

                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(event);

                        springLeft.setAtRest();
                        springBottom.setAtRest();
                        springRight.setAtRest();
                        springTop.setAtRest();

                        break;
                    case MotionEvent.ACTION_MOVE: {
                        final int pointerIndex = event.findPointerIndex(activePointerId);

                        if (pointerIndex != -1) {
                            final int location[] = {0, 0};
                            getLocationOnScreen(location);

                            if (currentSwipeDirection == SWIPE_MODE_RIGHT || currentSwipeDirection == SWIPE_MODE_LEFT) {
                                float x = event.getX(pointerIndex) + location[0];
                                float offsetX = x - lastDownX + lastDownXTr;

                                if (getCurrentItem() == countItems - 1) {
                                    if (offsetX <= 0) {
                                        springLeft.setCurrentValue(offsetX);
                                    } else {
                                        springRight.setCurrentValue(offsetX);
                                    }
                                } else {
                                    if (offsetX <= 0) {
                                        return super.onTouchEvent(event);
                                    } else {
                                        springRight.setCurrentValue(offsetX);
                                    }
                                }
                            } else if (currentSwipeDirection == SWIPE_MODE_DOWN || currentSwipeDirection == SWIPE_MODE_UP) {
                                float y = event.getY(pointerIndex) + location[1];
                                float offsetY = y - lastDownY + lastDownYTr;

                                if (offsetY >= 0) {
                                    springBottom.setCurrentValue(offsetY);
                                } else {
                                    springTop.setCurrentValue(offsetY);
                                }
                            }

                            velocityTracker.addMovement(event);
                        }

                        break;
                    }

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        final int pointerIndex = event.findPointerIndex(activePointerId);
                        if (pointerIndex != -1) {
                            velocityTracker.addMovement(event);
                            velocityTracker.computeCurrentVelocity(1000);

                            final int location[] = {0, 0};
                            getLocationOnScreen(location);

                            if (currentSwipeDirection == SWIPE_MODE_RIGHT || currentSwipeDirection == SWIPE_MODE_LEFT) {
                                float x = event.getX(pointerIndex) + location[0];
                                float offsetX = x - lastDownX + lastDownXTr;

                                if (getCurrentItem() == countItems - 1) {
                                    if (offsetX <= 0) {
                                        if (offsetX < -thresholdEnd) {
                                            springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(-getWidth());
                                            onDismissHorizontal.onNext(null);
                                        } else {
                                            springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
                                        }
                                    } else {
                                        if (offsetX > thresholdEnd) {
                                            springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(getWidth() - snoozeModeWidth);
                                        } else {
                                            springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
                                        }
                                    }
                                } else {
                                    if (offsetX <= 0) {
                                        springRight.setCurrentValue(0);
                                        return super.onTouchEvent(event);
                                    } else {
                                        if (offsetX > thresholdEnd) {
                                            springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(getWidth() - snoozeModeWidth);
                                        } else {
                                            springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
                                        }
                                    }
                                }
                            } else if (currentSwipeDirection == SWIPE_MODE_DOWN || currentSwipeDirection == SWIPE_MODE_UP) {
                                float y = event.getY(pointerIndex) - location[1];
                                float offsetY = y - lastDownY + lastDownYTr;

                                if (offsetY <= 0) {
                                    if (offsetY < -thresholdEnd) {
                                        springBottom.setVelocity(velocityTracker.getYVelocity()).setEndValue(getHeight());
                                        onDismissVertical.onNext(null);
                                    } else {
                                        springBottom.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                                    }
                                } else {
                                    if (offsetY > thresholdEnd) {
                                        springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(-getHeight() + backToMessageHeight);
                                    } else {
                                        springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                                    }
                                }
                            }
                        }

                        break;
                }

                return true;
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
            setTranslationY(value);
        }
    }

    private class BottomSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            setTranslationY(value);
        }
    }

    public Observable<Void> onDismissHorizontal() {
        return onDismissHorizontal;
    }

    public Observable<Void> onDismissVertical() {
        return onDismissVertical;
    }
}