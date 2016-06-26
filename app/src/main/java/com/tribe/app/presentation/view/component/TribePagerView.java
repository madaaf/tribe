package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.adapter.pager.TribePagerAdapter;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.SquareFrameLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class TribePagerView extends FrameLayout {

    private static final SpringConfig ORIGAMI_SPRING_BOUNCE_LIGHT_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(132, 11f);
    private static final SpringConfig ORIGAMI_SPRING_BOUNCE_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(132, 7f);
    private static final SpringConfig ORIGAMI_SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(200, 17);
    private static final float DRAG_RATE = 0.5f;
    private static final float OVERSHOOT = 0.75f;
    private static final float OVERSHOOT_MEDIUM = 2.5f;
    private static final float OVERSHOOT_BIG = 3f;
    private static final int DURATION = 200;
    private static final int DURATION_SLOW = 400;
    private static final int DELAY_BEFORE_CLOSE = 750;
    private static final int DELAY = 20;
    private static final int DELAY_INIT = 100;

    @Inject
    TribePagerAdapter tribePagerAdapter;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @BindView(R.id.viewShadowTop)
    View viewShadowTop;

    @BindView(R.id.viewShadowLeft)
    View viewShadowLeft;

    @BindView(R.id.viewShadowBottom)
    View viewShadowBottom;

    @BindView(R.id.layoutReply)
    FrameLayout layoutReply;

    @BindView(R.id.layoutSnooze)
    FrameLayout layoutSnooze;

    @BindView(R.id.imgSnooze)
    ImageView imgSnooze;

    @BindView(R.id.txtSnooze)
    TextViewFont txtSnooze;

    @BindView(R.id.layoutLater)
    RelativeLayout layoutLater;

    @BindView(R.id.layoutTomorrow)
    RelativeLayout layoutTomorrow;

    @BindView(R.id.layoutWeekend)
    RelativeLayout layoutWeekend;

    @BindView(R.id.layoutWeek)
    RelativeLayout layoutWeek;

    @BindViews({R.id.layoutLater, R.id.layoutTomorrow, R.id.layoutWeekend, R.id.layoutWeek})
    List<View> layoutSnoozeList;

    @BindView(R.id.imgBigSnooze)
    ImageView imgBigSnooze;

    @BindView(R.id.imgReply)
    ImageView imgReply;

    @BindView(R.id.layoutTile)
    SquareFrameLayout layoutTile;

    @BindView(R.id.txtName)
    TextViewFont txtName;

    @BindView(R.id.txtStatus)
    TextViewFont txtStatus;

    @BindView(R.id.avatar)
    AvatarView avatarView;

    @BindView(R.id.cameraWrapper)
    CameraWrapper cameraWrapper;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springLeft;
    private Spring springRight;
    private Spring springTop;
    private Spring springBottom;
    private Spring springAlpha;
    private Spring springAlphaSwipeUp;
    private Spring springAlphaSwipeDown;
    private LeftSpringListener springLeftListener;
    private RightSpringListener springRightListener;
    private TopSpringListener springTopListener;
    private BottomSpringListener springBottomListener;
    private AlphaSpringListener springAlphaListener;
    private AlphaSwipeUpSpringListener springAlphaSwipeUpListener;
    private AlphaSwipeDownSpringListener springAlphaSwipeDownListener;

    // VARIABLES
    private ScreenUtils screenUtils;
    private User currentUser;
    private int previousPosition;
    private int currentSwipeDirection;
    private float currentDragPercent;
    private int currentOffsetRight;
    private int currentOffsetTop;
    private int currentOffsetLeft;
    private int currentOffsetBottom;
    private List<Tribe> tribeList;
    private TribeComponentView currentView;
    private boolean inSnoozeMode = false;
    private boolean inReplyMode = false;

    // DIMENS
    private int thresholdEnd;
    private int backToMessageHeight;
    private int snoozeModeWidth;
    private int thresholdAlphaEnd;
    private int iconSize;
    private int iconSizeMax;
    private int marginBottomReplyTile;
    private int marginCameraLeftInit;
    private int marginCameraBottomInit;

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

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();


    public TribePagerView(Context context) {
        super(context);
    }

    public TribePagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        springLeft.addListener(springLeftListener);
        springRight.addListener(springRightListener);
        springTop.addListener(springTopListener);
        springBottom.addListener(springBottomListener);
        springAlpha.addListener(springAlphaListener);
        springAlphaSwipeUp.addListener(springAlphaSwipeUpListener);
        springAlphaSwipeDown.addListener(springAlphaSwipeDownListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        springLeft.removeListener(springLeftListener);
        springRight.addListener(springRightListener);
        springTop.addListener(springTopListener);
        springBottom.addListener(springBottomListener);
        springAlpha.addListener(springAlphaListener);
        springAlphaSwipeUp.addListener(springAlphaSwipeUpListener);
        springAlphaSwipeDown.addListener(springAlphaSwipeDownListener);

        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_tribe_pager, this);
        unbinder = ButterKnife.bind(this);

        ApplicationComponent applicationComponent = ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
        applicationComponent.inject(this);
        currentUser = applicationComponent.currentUser();
        screenUtils = applicationComponent.screenUtils();

        initViewPager();
        initDimen();
        initCamera();
        initUI();
    }

    public void onResume() {
        cameraWrapper.onResume();
    }

    public void onPause() {
        cameraWrapper.onPause();
    }

    private void initViewPager() {
        viewPager.setAdapter(tribePagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setScrollDurationFactor(1.0f);
        viewPager.setCurrentItem(0);
        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_ALL);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void onPageScrollStateChanged(int state) {}

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int currentPosition) {
                tribePagerAdapter.setCurrentPosition(currentPosition);
                currentView = (TribeComponentView) viewPager.findViewWithTag(currentPosition);
                tribePagerAdapter.releaseTribe(currentPosition, (TribeComponentView) viewPager.findViewWithTag(previousPosition));
                tribePagerAdapter.startTribe(currentPosition, currentView);
                previousPosition = currentPosition;
            }
        });
    }

    private void initUI() {
        springSystem = SpringSystem.create();

        springLeft = springSystem.createSpring();
        springRight = springSystem.createSpring();
        springTop = springSystem.createSpring();
        springBottom = springSystem.createSpring();
        springAlpha = springSystem.createSpring();
        springAlphaSwipeUp = springSystem.createSpring();
        springAlphaSwipeDown = springSystem.createSpring();

        springLeft.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springRight.setSpringConfig(ORIGAMI_SPRING_BOUNCE_CONFIG);
        springTop.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springBottom.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springAlpha.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springAlphaSwipeUp.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springAlphaSwipeDown.setSpringConfig(ORIGAMI_SPRING_CONFIG);

        springLeftListener = new LeftSpringListener();
        springRightListener = new RightSpringListener();
        springTopListener = new TopSpringListener();
        springBottomListener = new BottomSpringListener();
        springAlphaListener = new AlphaSpringListener();
        springAlphaSwipeUpListener = new AlphaSwipeUpSpringListener();
        springAlphaSwipeDownListener = new AlphaSwipeDownSpringListener();

        txtName.setText(currentUser.getDisplayName());
        avatarView.load(currentUser.getProfilePicture());

        ViewGroup.LayoutParams paramsLayoutTile = layoutTile.getLayoutParams();
        paramsLayoutTile.width = screenUtils.getWidth() >> 1;
        paramsLayoutTile.height = paramsLayoutTile.width;
        layoutTile.setLayoutParams(paramsLayoutTile);
        layoutTile.invalidate();

        FrameLayout.LayoutParams paramsCamera = (FrameLayout.LayoutParams) cameraWrapper.getLayoutParams();
        paramsCamera.topMargin = screenUtils.getHeight() - cameraWrapper.getHeightFromRatio() - marginCameraBottomInit;
        paramsCamera.leftMargin = marginCameraLeftInit;
        cameraWrapper.setLayoutParams(paramsCamera);
        cameraWrapper.invalidate();
    }

    private void initDimen() {
        thresholdEnd = getContext().getResources().getDimensionPixelSize(R.dimen.threshold_end_tribe);
        thresholdAlphaEnd = thresholdEnd >> 1;
        backToMessageHeight = getContext().getResources().getDimensionPixelSize(R.dimen.back_to_message_height);
        snoozeModeWidth = getContext().getResources().getDimensionPixelSize(R.dimen.snooze_mode_width);
        iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.tribe_icon_size);
        iconSizeMax = getContext().getResources().getDimensionPixelSize(R.dimen.tribe_icon_size_max);
        marginBottomReplyTile = getResources().getDimensionPixelOffset(R.dimen.vertical_margin_reply_tile);
        marginCameraLeftInit = getContext().getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
        marginCameraBottomInit = getContext().getResources().getDimensionPixelOffset(R.dimen.vertical_margin_small);
    }

    private void initCamera() {
        cameraWrapper.initDimens(
                marginCameraLeftInit + backToMessageHeight,
                marginCameraLeftInit,
                marginCameraBottomInit
        );
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        viewPager.computeSwipeDirection(event);
        currentSwipeDirection = viewPager.getCurrentSwipeDirection();

        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (!inReplyMode && !inSnoozeMode) {
                    initSnoozeItems();
                    initReplyItems();
                    springLeft.setCurrentValue(0).setAtRest();
                    springBottom.setCurrentValue(0).setAtRest();
                    springRight.setCurrentValue(0).setAtRest();
                    springTop.setCurrentValue(0).setAtRest();
                    springAlpha.setCurrentValue(1).setAtRest();
                    springAlphaSwipeUp.setCurrentValue(0).setAtRest();
                    springAlphaSwipeDown.setCurrentValue(0).setAtRest();
                }

                activePointerId = event.getPointerId(0);
                lastDownXTr = getTranslationX();
                lastDownX = event.getRawX();

                lastDownYTr = getTranslationY();
                lastDownY = event.getRawY();

                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);

                if ((inReplyMode && event.getY() < backToMessageHeight)
                    || (inSnoozeMode &&  (event.getX() > getWidth() - snoozeModeWidth))) {
                    return true;
                } else {
                    return false;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                if (currentSwipeDirection == -1) return false;

                final int pointerIndex = event.findPointerIndex(activePointerId);

                if (pointerIndex != -1) {
                    final int location[] = {0, 0};
                    getLocationOnScreen(location);

                    final int locationCamera[] = {0, 0};
                    cameraWrapper.getLocationOnScreen(locationCamera);

                    if (event.getX() <= locationCamera[0] + cameraWrapper.getWidth()
                            && event.getX() > locationCamera[0]
                            && event.getY() <= locationCamera[1] + cameraWrapper.getHeight()
                            && event.getY() > locationCamera[1]) {
                        return false;
                    }

                    if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_RIGHT || currentSwipeDirection == CustomViewPager.SWIPE_MODE_LEFT) {
                        float x = event.getX(pointerIndex) + location[0];
                        float offsetX = x - lastDownX + lastDownXTr;

                        if (!inSnoozeMode && viewPager.getCurrentItem() != tribePagerAdapter.getCount() - 1) {
                            if (offsetX <= 0) {
                                return false;
                            }
                        }
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (currentSwipeDirection == -1) return false;

                final int pointerIndex = event.findPointerIndex(activePointerId);

                if (pointerIndex != -1) {
                    final int location[] = {0, 0};
                    getLocationOnScreen(location);

                    final int locationCamera[] = {0, 0};
                    cameraWrapper.getLocationOnScreen(locationCamera);

                    if (event.getX() <= locationCamera[0] + cameraWrapper.getWidth()
                            && event.getX() > locationCamera[0]
                            && event.getY() <= locationCamera[1] + cameraWrapper.getHeight()
                            && event.getY() > locationCamera[1]) {
                        return false;
                    }

                    if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_RIGHT || currentSwipeDirection == CustomViewPager.SWIPE_MODE_LEFT) {
                        float x = event.getX(pointerIndex) + location[0];
                        float offsetX = x - lastDownX + lastDownXTr;

                        if (!inSnoozeMode && viewPager.getCurrentItem() != tribePagerAdapter.getCount() - 1) {
                            if (offsetX >= 0) {
                                return false;
                            }
                        }
                    }
                }

                break;
            }
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewPager.computeSwipeDirection(event);
        currentSwipeDirection = viewPager.getCurrentSwipeDirection();

        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(activePointerId);

                if (pointerIndex != -1) {
                    final int location[] = {0, 0};
                    getLocationOnScreen(location);

                    if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_RIGHT || currentSwipeDirection == CustomViewPager.SWIPE_MODE_LEFT) {
                        float x = event.getX(pointerIndex) + location[0];
                        float offsetX = x - lastDownX;

                        if (!inSnoozeMode && !inReplyMode) {
                            if (viewPager.getCurrentItem() == tribePagerAdapter.getCount() - 1) {
                                if (offsetX <= 0) {
                                    return applyOffsetLeftWithTension(offsetX);
                                } else {
                                    return applyOffsetRightWithTension(offsetX);
                                }
                            } else {
                                if (offsetX <= 0) {
                                    return viewPager.onTouchEvent(event);
                                } else {
                                    return applyOffsetRightWithTension(offsetX);
                                }
                            }
                        }
                    } else if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_DOWN || currentSwipeDirection == CustomViewPager.SWIPE_MODE_UP) {
                        float y = event.getY(pointerIndex) + location[1];
                        float offsetY = y - lastDownY + lastDownYTr;

                        if (!inReplyMode && !inSnoozeMode) {
                            if (offsetY >= 0) {
                                return applyOffsetBottomWithTension(offsetY);
                            } else {
                                return applyOffsetTopWithTension(offsetY);
                            }
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

                    if (inReplyMode) {
                        if (event.getY() < backToMessageHeight) closeReplyMode();
                    } else if (inSnoozeMode) {
                        if (event.getX() > snoozeModeWidth) closeSnoozeMenu(false);
                    }

                    if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_RIGHT || currentSwipeDirection == CustomViewPager.SWIPE_MODE_LEFT) {
                        float x = event.getX(pointerIndex) + location[0];
                        float offsetX = x - lastDownX;

                        if (!inSnoozeMode && !inReplyMode) {
                            if (viewPager.getCurrentItem() == tribePagerAdapter.getCount() - 1) {
                                if (offsetX <= 0) {
                                    springLeft.setCurrentValue(currentOffsetLeft).setAtRest();

                                    if (offsetX < -thresholdEnd) {
                                        dismissScreenToLeft();
                                    } else {
                                        springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
                                    }
                                } else {
                                    computeUpToRight(offsetX);
                                }
                            } else {
                                if (offsetX <= 0) {
                                    return viewPager.onTouchEvent(event);
                                } else {
                                    computeUpToRight(offsetX);
                                }
                            }
                        } else if (inSnoozeMode) {
                            closeSnoozeMenu(false);
                        }
                    } else if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_DOWN || currentSwipeDirection == CustomViewPager.SWIPE_MODE_UP) {
                        float y = event.getY(pointerIndex) - location[1];
                        float offsetY = y - lastDownY + lastDownYTr;

                        if (!inReplyMode && !inSnoozeMode) {
                            if (offsetY >= 0) {
                                springBottom.setCurrentValue(currentOffsetBottom);

                                if (offsetY > thresholdEnd) {
                                    dismissScreenToBottom();
                                } else {
                                    springAlphaSwipeDown.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                                    springAlpha.setVelocity(velocityTracker.getYVelocity()).setEndValue(1);
                                    springBottom.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                                }
                            } else {
                                springTop.setCurrentValue(currentOffsetTop).setAtRest();

                                if (offsetY < -thresholdEnd) {
                                    openReplyMode();
                                } else {
                                    springAlpha.setVelocity(velocityTracker.getYVelocity()).setEndValue(1);
                                    springAlphaSwipeUp.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                                    springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                                }
                            }
                        } else if (inReplyMode) {
                            closeReplyMode();
                        }
                    }
                }

                break;
        }

        return true;
    }

    public void setItems(List<Tribe> items) {
        this.tribeList = items;
        this.tribePagerAdapter.setItems(items);
    }

    @Override
    public void setBackgroundColor(int color) {
        ColorDrawable[] colorList = {
                new ColorDrawable(getResources().getColor(R.color.black_tribe)),
                new ColorDrawable(color),
        };
        TransitionDrawable trd = new TransitionDrawable(colorList);
        this.layoutReply.setBackground(trd);

    }

    private class LeftSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();
                scrollLeft(value);
            }
        }
    }

    private class RightSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();
                scrollRight(value);
            }
        }
    }

    private class TopSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();
                scrollTop(value);
            }
        }
    }

    private void scrollTop(float value) {
        viewPager.setTranslationY(value);
        layoutSnooze.setTranslationY(value);
        viewShadowBottom.setTranslationY(value);
    }

    private void scrollBottom(float value) {
        viewPager.setTranslationY(value);
        layoutReply.setTranslationY(value);
        layoutSnooze.setTranslationY(value);
        viewShadowTop.setTranslationY(value);
    }

    private void scrollRight(float value) {
        viewPager.setTranslationX(value);
        layoutReply.setTranslationX(value);
        viewShadowLeft.setTranslationX(value);
    }

    private void scrollLeft(float value) {
        viewPager.setTranslationX(value);
        layoutReply.setTranslationX(value);
        layoutSnooze.setTranslationX(value);
    }

    private class BottomSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();
                scrollBottom(value);
            }
        }
    }

    private class AlphaSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();

                computeCurrentView();
                currentView.setIconsAlpha(value);
            }
        }
    }

    private class AlphaSwipeUpSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();

                computeCurrentView();
                currentView.setSwipeUpAlpha(value);
            }
        }
    }

    private class AlphaSwipeDownSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();

                if (currentView == null) currentView = (TribeComponentView) viewPager.findViewWithTag(viewPager.getCurrentItem());
                currentView.setSwipeDownAlpha(value);
            }
        }
    }

    public Observable<Void> onDismissHorizontal() {
        return onDismissHorizontal;
    }

    public Observable<Void> onDismissVertical() {
        return onDismissVertical;
    }

    private void initSnoozeItems() {
        txtSnooze.setTranslationX(thresholdEnd);
        txtSnooze.setAlpha(0);
        imgSnooze.setAlpha(1f);
        layoutLater.setAlpha(1);
        layoutTomorrow.setAlpha(1);
        layoutWeekend.setAlpha(1);
        layoutWeek.setAlpha(1);
        layoutLater.setTranslationY(0);
        layoutTomorrow.setTranslationY(0);
        layoutWeekend.setTranslationY(0);
        layoutWeek.setTranslationY(0);
        layoutLater.setTranslationX(getWidth() >> 1);
        layoutTomorrow.setTranslationX(getWidth() >> 1);
        layoutWeekend.setTranslationX(getWidth() >> 1);
        layoutWeek.setTranslationX(getWidth() >> 1);
    }

    private void initReplyItems() {
        imgReply.setAlpha(1f);
        TransitionDrawable trd = ((TransitionDrawable) layoutReply.getBackground());
        trd.resetTransition();
        layoutTile.setRotation(-10f);
        layoutTile.setTranslationY(0);
        avatarView.setScaleX(0.8f);
        avatarView.setScaleY(0.8f);
        cameraWrapper.setTranslationY(getHeight());
    }

    private void updateIconSize(View view, int size) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = size;
        params.width = size;
        view.setLayoutParams(params);
        view.invalidate();
    }

    private void dismissScreenToLeft() {
        springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(-getWidth());
        onDismissHorizontal.onNext(null);
    }

    private void dismissScreenToBottom() {
        springAlpha.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
        springAlphaSwipeDown.setCurrentValue(1).setAtRest();
        springBottom.setVelocity(velocityTracker.getYVelocity()).setEndValue(getHeight());
        onDismissVertical.onNext(null);
    }

    private void openSnoozeMenu() {
        inSnoozeMode = true;
        springRight.setSpringConfig(ORIGAMI_SPRING_BOUNCE_CONFIG);
        springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(getWidth() - snoozeModeWidth);

        updateIconSize(imgSnooze, iconSize);
        imgSnooze.animate().translationX(0).translationY(0).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION).start();
        txtSnooze.animate().translationX(0).translationY(0).alpha(1).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION).start();
        layoutLater.animate().translationX(0).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION).setStartDelay(DELAY_INIT).start();
        layoutTomorrow.animate().translationX(0).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION).setStartDelay(DELAY_INIT + DELAY).start();
        layoutWeekend.animate().translationX(0).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION).setStartDelay(DELAY_INIT + DELAY * 2).start();
        layoutWeek.animate().translationX(0).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION).setStartDelay(DELAY_INIT + DELAY * 3).start();
    }

    private void closeSnoozeMenu(boolean hasSelectedSnooze) {
        inSnoozeMode = false;
        springRight.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);

        updateIconSize(imgSnooze, iconSize);
        imgSnooze.animate().translationY(0).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setStartDelay(DELAY).setDuration(DURATION).start();
        txtSnooze.animate().translationX(thresholdEnd).translationY(0).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION).start();
        layoutLater.animate().translationX(getWidth() >> 1).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION).start();
        layoutTomorrow.animate().translationX(getWidth() >> 1).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION).start();
        layoutWeekend.animate().translationX(getWidth() >> 1).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION).start();
        layoutWeek.animate().translationX(getWidth() >> 1).setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION).start();

        if (hasSelectedSnooze) {
            subscriptions.add(Observable.timer(DURATION, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    TransitionDrawable trd = ((TransitionDrawable) layoutSnooze.getBackground());
                    trd.resetTransition();

                    showBigSnoozeIcon();
                }));
        }
    }

    @OnClick({R.id.layoutLater, R.id.layoutTomorrow, R.id.layoutWeekend, R.id.layoutWeek})
    public void onClickSnooze(View v) {
        chooseSnooze(v);
    }

    private void chooseSnooze(View btnSnooze) {
        for (View layout : layoutSnoozeList) {
            if (btnSnooze.getId() != layout.getId()) {
                AnimationUtils.fadeOut(layout, DURATION);
            } else {
                layout.animate().translationY((getHeight() >> 1) - ((LayoutParams) layout.getLayoutParams()).topMargin - (layout.getHeight() >> 1))
                        .setInterpolator(new OvershootInterpolator(OVERSHOOT_BIG)).setDuration(DURATION_SLOW).start();
            }
        }

        AnimationUtils.fadeOut(txtSnooze, DURATION);
        AnimationUtils.fadeOut(imgSnooze, DURATION);

        TransitionDrawable trd = ((TransitionDrawable) layoutSnooze.getBackground());
        trd.setCrossFadeEnabled(false);
        trd.startTransition(DURATION);

        subscriptions.add(Observable.timer(DURATION + DELAY_BEFORE_CLOSE, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    closeSnoozeMenu(true);
                }));
    }

    private void showBigSnoozeIcon() {
        imgBigSnooze.animate().alpha(1).scaleY(1).scaleX(1).rotation(0)
                .setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION_SLOW)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        imgBigSnooze.animate().alpha(0).scaleY(0).scaleX(0).rotation(90)
                                .setInterpolator(new OvershootInterpolator(OVERSHOOT)).setDuration(DURATION_SLOW)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        imgBigSnooze.setRotation(-90);
                                    }
                                })
                                .setStartDelay(DELAY_BEFORE_CLOSE)
                                .start();
                    }
                })
                .start();
    }

    private void openReplyMode() {
        inReplyMode = true;
        springAlpha.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
        springAlphaSwipeUp.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
        springTop.setSpringConfig(ORIGAMI_SPRING_BOUNCE_LIGHT_CONFIG);
        springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(-getHeight() + backToMessageHeight);

        AnimationUtils.fadeOut(imgReply, DURATION);

        TransitionDrawable trd = ((TransitionDrawable) layoutReply.getBackground());
        trd.setCrossFadeEnabled(false);
        trd.startTransition(DURATION);

        avatarView.animate().scaleX(1).scaleY(1).setInterpolator(new OvershootInterpolator(OVERSHOOT_MEDIUM))
                .setDuration(DURATION)
                .setStartDelay(DURATION >> 1)
                .start();

        layoutTile.animate().translationY(-((getHeight() >> 1) - (layoutTile.getHeight() >> 1) - marginBottomReplyTile))
                .rotation(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(DURATION)
                .start();

        cameraWrapper.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION).start();

        computeCurrentView();
        currentView.showBackToTribe(DURATION);
    }

    private void closeReplyMode() {
        inReplyMode = false;
        springAlpha.setVelocity(velocityTracker.getYVelocity()).setEndValue(1);
        springAlphaSwipeUp.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
        springTop.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springTop.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);

        TransitionDrawable trd = ((TransitionDrawable) layoutReply.getBackground());
        trd.setCrossFadeEnabled(false);
        trd.reverseTransition(DURATION);

        avatarView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(DURATION).start();
        layoutTile.animate().translationY(marginBottomReplyTile).rotation(-10f).setDuration(DURATION_SLOW).start();
        cameraWrapper.animate().translationY(getHeight()).setDuration(DURATION_SLOW).start();

        currentView.hideBackToTribe(DURATION);
    }

    private boolean applyOffsetLeftWithTension(float offsetX) {
        // OPENING SNOOZE MENU
        float totalDragDistance = getWidth() - snoozeModeWidth;
        final float scrollRight = -offsetX * DRAG_RATE;
        currentDragPercent = scrollRight / totalDragDistance;

        if (currentDragPercent < 0) {
            return false;
        }

        currentOffsetLeft = -computeOffsetWithTension(scrollRight, totalDragDistance);
        scrollLeft(currentOffsetLeft);
        return true;
    }

    private boolean applyOffsetRightWithTension(float offsetX) {
        // OPENING SNOOZE MENU
        float totalDragDistance = getWidth() - snoozeModeWidth;
        final float scrollRight = offsetX * DRAG_RATE;
        currentDragPercent = scrollRight / totalDragDistance;

        if (currentDragPercent < 0) {
            return false;
        }

        currentOffsetRight = computeOffsetWithTension(scrollRight, totalDragDistance);
        viewPager.setTranslationX(currentOffsetRight);
        updateIconSize(imgSnooze, (int) (currentDragPercent * iconSizeMax));
        imgSnooze.setTranslationY((getHeight() >> 1) - ((LayoutParams) imgSnooze.getLayoutParams()).topMargin - (imgSnooze.getHeight() >> 1));
        txtSnooze.setTranslationY((getHeight() >> 1) - ((LayoutParams) txtSnooze.getLayoutParams()).topMargin - (txtSnooze.getHeight() >> 1));
        return true;
    }

    private boolean applyOffsetBottomWithTension(float offsetY) {
        float totalDragDistance = getHeight() / 3;
        final float scrollTop = offsetY * DRAG_RATE;
        currentDragPercent = scrollTop / totalDragDistance;

        if (currentDragPercent < 0) {
            return false;
        }

        currentOffsetBottom = computeOffsetWithTension(scrollTop, totalDragDistance);
        scrollBottom(currentOffsetBottom);

        springAlpha.setCurrentValue(1 - (offsetY / thresholdAlphaEnd));
        springAlphaSwipeDown.setCurrentValue(offsetY / (thresholdAlphaEnd * 2));
        return true;
    }

    private boolean applyOffsetTopWithTension(float offsetY) {
        // OPENING REPLY MENU
        float totalDragDistance = getHeight() / 3;
        final float scrollTop = -offsetY * DRAG_RATE;
        currentDragPercent = scrollTop / totalDragDistance;

        if (currentDragPercent < 0) {
            return false;
        }

        currentOffsetTop = -computeOffsetWithTension(scrollTop, totalDragDistance);
        scrollTop(currentOffsetTop);
        updateIconSize(imgReply, (int) (currentDragPercent * iconSizeMax));
        layoutTile.setTranslationY(currentOffsetTop);

        springAlpha.setCurrentValue(1 - (offsetY / -thresholdAlphaEnd));
        springAlphaSwipeUp.setCurrentValue(offsetY / (-thresholdAlphaEnd * 2));
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

    private void computeCurrentView() {
        if (currentView == null) currentView = (TribeComponentView) viewPager.findViewWithTag(viewPager.getCurrentItem());
    }

    private void computeUpToRight(float offsetX) {
        springRight.setCurrentValue(currentOffsetRight).setAtRest();

        if (offsetX > thresholdEnd) {
            openSnoozeMenu();
        } else {
            springRight.setVelocity(velocityTracker.getXVelocity()).setEndValue(0);
        }
    }
}