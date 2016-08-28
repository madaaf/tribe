package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
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

import com.f2prateek.rx.preferences.Preference;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.scope.FloatDef;
import com.tribe.app.presentation.internal.di.scope.SpeedPlayback;
import com.tribe.app.presentation.view.adapter.pager.TribePagerAdapter;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.Date;
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

    public static final float SPEED_NORMAL = 1f;
    public static final float SPEED_LIGHTLY_FASTER = 1.25f;
    public static final float SPEED_FAST = 1.5f;

    @FloatDef({SPEED_NORMAL, SPEED_LIGHTLY_FASTER, SPEED_FAST})
    public @interface SpeedPlaybackValues{}

    private static final SpringConfig ORIGAMI_SPRING_BOUNCE_LIGHT_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(132, 11f);
    private static final SpringConfig ORIGAMI_SPRING_BOUNCE_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(132, 7f);
    private static final SpringConfig ORIGAMI_SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(200, 17);
    private static final SpringConfig SPRING_REPLY_CONFIG = SpringConfig.fromBouncinessAndSpeed(5f, 5f);
    private static final float DRAG_RATE = 0.5f;
    private static final float OVERSHOOT = 0.75f;
    private static final float OVERSHOOT_MEDIUM = 2.5f;
    private static final float OVERSHOOT_BIG = 3f;
    private static final int DURATION = 200;
    private static final int DURATION_SLOW = 400;
    private static final int DURATION_REPLY = 600;
    private static final int DELAY_BEFORE_CLOSE = 750;
    private static final int DELAY = 20;
    private static final int DELAY_INIT = 100;

    @Inject
    TribePagerAdapter tribePagerAdapter;

    @Inject @SpeedPlayback Preference<Float> speedPlayback;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @BindView(R.id.viewShadowTop)
    View viewShadowTop;

    @BindView(R.id.viewShadowLeft)
    View viewShadowLeft;

    @BindView(R.id.viewShadowBottom)
    View viewShadowBottom;

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

    @BindView(R.id.layoutTile)
    TileView viewTile;

    @BindView(R.id.cameraWrapper)
    CameraWrapper cameraWrapper;

    @BindView(R.id.imgSpeed)
    ImageView imgSpeed;

    @BindView(R.id.layoutBottom)
    ViewGroup layoutBottom;

    @BindView(R.id.viewCircleGradient)
    View viewCircleGradient;

    @BindView(R.id.txtNbTribes)
    TextViewFont txtNbTribes;

    @BindView(R.id.layoutNbTribes)
    ViewGroup layoutNbTribes;

    @BindView(R.id.imgCancelReply)
    ImageView imgCancelReply;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springLeft;
    private Spring springRight;
    private Spring springTop;
    private Spring springBottom;
    private Spring springAlpha;
    private Spring springAlphaSwipeDown;
    private Spring springReplyMode;
    private LeftSpringListener springLeftListener;
    private RightSpringListener springRightListener;
    private TopSpringListener springTopListener;
    private BottomSpringListener springBottomListener;
    private AlphaSpringListener springAlphaListener;
    private AlphaSwipeDownSpringListener springAlphaSwipeDownListener;

    // VARIABLES
    private ScreenUtils screenUtils;
    private int previousPosition;
    private int currentSwipeDirection;
    private float currentDragPercent;
    private int currentOffsetRight;
    private int currentOffsetLeft;
    private int currentOffsetBottom;
    private List<TribeMessage> tribeList;
    private List<TribeMessage> tribeListSeens;
    private TribeComponentView currentView;
    private boolean inSnoozeMode = false;
    private @CameraWrapper.TribeMode String tribeMode;
    private boolean inReplyMode = false;

    // DIMENS
    private int thresholdEnd;
    private int backToMessageHeight;
    private int snoozeModeWidth;
    private int thresholdAlphaEnd;
    private int iconSize;
    private int iconSizeMax;
    private int marginCameraTopInit;
    private int marginCameraBounds;

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
    private final PublishSubject<View> clickTapToCancel = PublishSubject.create();
    private final PublishSubject<View> onNotCancel = PublishSubject.create();
    private final PublishSubject<View> recordStarted = PublishSubject.create();
    private final PublishSubject<View> recordEnded = PublishSubject.create();
    private final PublishSubject<View> clickEnableLocation = PublishSubject.create();


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
        springAlphaSwipeDown.addListener(springAlphaSwipeDownListener);

        if (currentView == null)
            ((TribeComponentView) viewPager.findViewWithTag(viewPager.getCurrentItem())).releasePlayer();
        else
            currentView.releasePlayer();

        tribePagerAdapter.onDestroy();

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
        screenUtils = applicationComponent.screenUtils();

        tribeListSeens = new ArrayList<>();

        initViewPager();
        initDimen();
        initUI();
        initCamera();
    }

    public void onResume() {
        cameraWrapper.onResume(false);
    }

    public void onPause() {
        cameraWrapper.onPause();

        if (currentView == null) {
            computeCurrentView();
        }

        currentView.pausePlayer();
    }

    private void initViewPager() {
        viewPager.setAdapter(tribePagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setScrollDurationFactor(1.0f);
        viewPager.setCurrentItem(0);
        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_ALL);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void onPageScrollStateChanged(int state) {}

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            public void onPageSelected(int currentPosition) {
                tribeListSeens.add(tribeList.get(currentPosition));
                updateNbTribes();
                tribePagerAdapter.setCurrentPosition(currentPosition);
                currentView = (TribeComponentView) viewPager.findViewWithTag(currentPosition);
                tribePagerAdapter.releaseTribe((TribeComponentView) viewPager.findViewWithTag(previousPosition));
                tribePagerAdapter.startTribe(currentView);
                previousPosition = currentPosition;
            }
        });

        subscriptions.add(tribePagerAdapter.onClickEnableLocation().subscribe(clickEnableLocation));
    }

    private void initUI() {
        springSystem = SpringSystem.create();

        springLeft = springSystem.createSpring();
        springRight = springSystem.createSpring();
        springTop = springSystem.createSpring();
        springBottom = springSystem.createSpring();
        springAlpha = springSystem.createSpring();
        springAlphaSwipeDown = springSystem.createSpring();

        springLeft.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springRight.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springTop.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springBottom.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springAlpha.setSpringConfig(ORIGAMI_SPRING_CONFIG);
        springAlphaSwipeDown.setSpringConfig(ORIGAMI_SPRING_CONFIG);

        springLeftListener = new LeftSpringListener();
        springRightListener = new RightSpringListener();
        springTopListener = new TopSpringListener();
        springBottomListener = new BottomSpringListener();
        springAlphaListener = new AlphaSpringListener();
        springAlphaSwipeDownListener = new AlphaSwipeDownSpringListener();

        ViewGroup.LayoutParams paramsLayoutTile = viewTile.getLayoutParams();
        paramsLayoutTile.width = screenUtils.getWidthPx() >> 1;
        paramsLayoutTile.height = paramsLayoutTile.width;
        viewTile.setLayoutParams(paramsLayoutTile);
        viewTile.invalidate();
        viewTile.initWithParent(null);

        viewTile.onRecordStart()
                .doOnNext(view -> hideExitCamera())
                .subscribe(recordStarted);

        viewTile.onRecordEnd()
                .doOnNext(view -> closeReplyMode())
                .subscribe(recordEnded);

        viewTile.onTapToCancel()
                .doOnNext(view -> {
                    viewTile.cancelReplyMode();
                    inReplyMode = false;
                })
                .subscribe(clickTapToCancel);

        viewTile.onNotCancel()
                .doOnNext(view -> {
                    viewTile.cancelReplyMode();
                    inReplyMode = false;
                })
                .subscribe(onNotCancel);

        subscriptions.add(viewTile
                .onReplyModeStarted()
                .subscribe(bool -> {
                    cameraWrapper.setVisibility(View.VISIBLE);
                    cameraWrapper.onResume(false);

                    computeCurrentView();
                    currentView.pausePlayer();
                    inReplyMode = true;
                    openReplyMode(bool);
                })
        );

        LayoutParams paramsCamera = (LayoutParams) cameraWrapper.getLayoutParams();
        paramsCamera.topMargin = (screenUtils.getHeightPx() >> 1) - (cameraWrapper.getHeightFromRatio() >> 1);
        paramsCamera.leftMargin = (screenUtils.getWidthPx() >> 1) - (cameraWrapper.getWidthFromRatio() >> 1);
        cameraWrapper.setLayoutParams(paramsCamera);

        imgCancelReply.setTranslationX(screenUtils.getWidthPx());

        computeImgSpeed();
    }

    private void initDimen() {
        thresholdEnd = getContext().getResources().getDimensionPixelSize(R.dimen.threshold_dismiss);
        thresholdAlphaEnd = thresholdEnd >> 1;
        backToMessageHeight = getContext().getResources().getDimensionPixelSize(R.dimen.back_to_message_height);
        snoozeModeWidth = getContext().getResources().getDimensionPixelSize(R.dimen.snooze_mode_width);
        iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.tribe_icon_size);
        iconSizeMax = getContext().getResources().getDimensionPixelSize(R.dimen.tribe_icon_size_max);
        marginCameraBounds = getContext().getResources().getDimensionPixelOffset(R.dimen.vertical_margin_small);
    }

    private void initCamera() {
        marginCameraTopInit = (screenUtils.getHeightPx() >> 1) - (cameraWrapper.getHeightFromRatio() >> 1);

        cameraWrapper.initDimens(
                marginCameraTopInit,
                (screenUtils.getWidthPx() >> 1) - (cameraWrapper.getWidthFromRatio() >> 1),
                marginCameraBounds,
                marginCameraBounds,
                marginCameraBounds,
                marginCameraBounds,
                false
        );

        // SPRING INSIDE CONFIGURATION
        springReplyMode = springSystem.createSpring();
        springReplyMode.setSpringConfig(SPRING_REPLY_CONFIG);
        springReplyMode.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                int marginTop = marginCameraTopInit - (int) (value * screenUtils.getHeightPx());
                FrameLayout.LayoutParams paramsCamera = (FrameLayout.LayoutParams) cameraWrapper.getLayoutParams();
                paramsCamera.topMargin = marginTop;
                cameraWrapper.setLayoutParams(paramsCamera);

                viewCircleGradient.setAlpha(1 - value);
            }
        });

        springReplyMode.setEndValue(1f);

        subscriptions.add(cameraWrapper.tribeMode().subscribe(mode -> tribeMode = mode));
    }

    public void setItems(List<TribeMessage> items) {
        this.tribeList = items;
        this.tribeListSeens.add(tribeList.get(0));
        this.tribePagerAdapter.setItems(items);
        updateNbTribes();
    }

    public void addItems(List<TribeMessage> items) {
        List<TribeMessage> newTribeList = new ArrayList<>();
        Date newestDate = this.tribeList.get(this.tribeList.size() - 1).getRecordedAt();

        for (TribeMessage message : items) {
            if (message.getRecordedAt().after(newestDate)) {
                newTribeList.add(message);
            }
        }

        if (newTribeList.size() > 0) {
            this.tribeList.addAll(newTribeList);
            this.tribePagerAdapter.setItems(newTribeList);
            updateNbTribes();
        }
    }

    private void updateNbTribes() {
        this.txtNbTribes.setText("" + (tribeList.size() - tribeListSeens.size()));
    }

    public @CameraWrapper.TribeMode String getTribeMode() {
        return tribeMode;
    }

    public void initWithInfo(Recipient recipient) {
        viewTile.setInfo(recipient);
    }

    public void startRecording(String id) {
        cameraWrapper.onStartRecord(id);
    }

    public void stopRecording() {
        cameraWrapper.onEndRecord();
    }

    public void showTapToCancel(TribeMessage tribe) {
        viewTile.showTapToCancel(tribe, tribeMode);
    }

    /////////////////////
    //    ANIMATIONS   //
    /////////////////////
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        viewPager.computeSwipeDirection(event);
        currentSwipeDirection = viewPager.getCurrentSwipeDirection();

        if (inReplyMode) return false;

        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (!inSnoozeMode && !inReplyMode) {
                    initSnoozeItems();
                    springLeft.setCurrentValue(0).setAtRest();
                    springBottom.setCurrentValue(0).setAtRest();
                    springRight.setCurrentValue(0).setAtRest();
                    springTop.setCurrentValue(0).setAtRest();
                    springAlpha.setCurrentValue(1).setAtRest();
                    springAlphaSwipeDown.setCurrentValue(0).setAtRest();
                }

                activePointerId = event.getPointerId(0);
                lastDownXTr = getTranslationX();
                lastDownX = event.getRawX();

                lastDownYTr = getTranslationY();
                lastDownY = event.getRawY();

                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);

                if (inSnoozeMode && (event.getX() > getWidth() - snoozeModeWidth)) {
                    return true;
                } else {
                    return false;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                if (currentSwipeDirection == -1 || inReplyMode) return false;

                final int pointerIndex = event.findPointerIndex(activePointerId);

                if (pointerIndex != -1) {
                    final int location[] = {0, 0};
                    getLocationOnScreen(location);

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
                if (currentSwipeDirection == -1 || inReplyMode) return false;

                final int pointerIndex = event.findPointerIndex(activePointerId);

                if (pointerIndex != -1) {
                    final int location[] = {0, 0};
                    getLocationOnScreen(location);

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
                                    //return applyOffsetRightWithTension(offsetX);
                                }
                            } else {
                                if (offsetX <= 0) {
                                    return viewPager.onTouchEvent(event);
                                } else {
                                    //return applyOffsetRightWithTension(offsetX);
                                }
                            }
                        }
                    } else if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_DOWN || currentSwipeDirection == CustomViewPager.SWIPE_MODE_UP) {
                        float y = event.getY(pointerIndex) + location[1];
                        float offsetY = y - lastDownY + lastDownYTr;

                        if (!inReplyMode && !inSnoozeMode) {
                            if (offsetY >= 0) {
                                return applyOffsetBottomWithTension(offsetY);
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

                    if (inSnoozeMode) {
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
                                    //computeUpToRight(offsetX);
                                }
                            } else {
                                if (offsetX <= 0) {
                                    return viewPager.onTouchEvent(event);
                                } else {
                                    //computeUpToRight(offsetX);
                                }
                            }
                        } else if (inSnoozeMode) {
                            closeSnoozeMenu(false);
                        }
                    } else if (currentSwipeDirection == CustomViewPager.SWIPE_MODE_DOWN || currentSwipeDirection == CustomViewPager.SWIPE_MODE_UP) {
                        float y = event.getY(pointerIndex) - location[1];
                        float offsetY = y - lastDownY + lastDownYTr;

                        if (!inSnoozeMode) {
                            if (offsetY >= 0) {
                                springBottom.setCurrentValue(currentOffsetBottom);

                                if (offsetY > thresholdEnd) {
                                    dismissScreenToBottom();
                                } else {
                                    springAlphaSwipeDown.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                                    springAlpha.setVelocity(velocityTracker.getYVelocity()).setEndValue(1);
                                    springBottom.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
                                }
                            }
                        }
                    }
                }

                break;
        }

        return true;
    }

    @Override
    public void setBackgroundColor(int color) {
//        ColorDrawable[] colorList = {
//                new ColorDrawable(getResources().getColor(R.color.black_tribe)),
//                new ColorDrawable(color),
//        };
//        TransitionDrawable trd = new TransitionDrawable(colorList);
//        this.layoutReply.setBackground(trd);
    }

    private class LeftSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();
                scrollLeft(value);
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            if (currentView != null && spring.getEndValue() == -getWidth()) {
                currentView.releasePlayer();
            }

            super.onSpringAtRest(spring);
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
        layoutBottom.setTranslationY(value);
    }

    private void scrollBottom(float value) {
        viewPager.setTranslationY(value);
        layoutSnooze.setTranslationY(value);
        viewShadowTop.setTranslationY(value);
        layoutBottom.setTranslationY(value);
    }

    private void scrollRight(float value) {
        viewPager.setTranslationX(value);
        viewShadowLeft.setTranslationX(value);
        layoutBottom.setTranslationX(value);
    }

    private void scrollLeft(float value) {
        viewPager.setTranslationX(value);
        layoutSnooze.setTranslationX(value);
        layoutBottom.setTranslationX(value);
    }

    private class BottomSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();
                scrollBottom(value);
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            if (currentView != null && spring.getEndValue() == getHeight()) {
                currentView.releasePlayer();
            }

            super.onSpringAtRest(spring);
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

    private class AlphaSwipeDownSpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (isAttachedToWindow()) {
                float value = (float) spring.getCurrentValue();

                computeCurrentView();
                currentView.setSwipeDownAlpha(value);
            }
        }
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

    private void updateIconSize(View view, int size) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = size;
        params.width = size;
        view.setLayoutParams(params);
        view.invalidate();
    }

    private void dismissScreenToLeft() {
        if (currentView != null) {
            currentView.pausePlayer();
        }

        springLeft.setVelocity(velocityTracker.getXVelocity()).setEndValue(-getWidth());
        onDismissHorizontal.onNext(null);

        cameraWrapper.setTranslationX(screenUtils.getWidthPx());
    }

    private void dismissScreenToBottom() {
        if (currentView != null) {
            currentView.pausePlayer();
        }

        springAlpha.setVelocity(velocityTracker.getYVelocity()).setEndValue(0);
        springAlphaSwipeDown.setCurrentValue(1).setAtRest();
        springBottom.setVelocity(velocityTracker.getYVelocity()).setEndValue(getHeight());
        onDismissVertical.onNext(null);

        cameraWrapper.setTranslationX(screenUtils.getWidthPx());
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

        if (currentView != null) currentView.pausePlayer();
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

        if (currentView != null) currentView.resumePlayer();
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
        scrollRight(currentOffsetRight);

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

    public List<TribeMessage> getTribeListSeens() {
        return tribeListSeens;
    }

    private void hideSpeed() {
        imgSpeed.animate().translationX(-screenUtils.getWidthPx()).setDuration(DURATION_SLOW).setInterpolator(new DecelerateInterpolator()).start();
    }

    private void showSpeed() {
        imgSpeed.animate().translationX(0).setDuration(DURATION_SLOW).setInterpolator(new DecelerateInterpolator()).start();
    }

    private void hideNbTribes() {
        layoutNbTribes.animate().translationX(screenUtils.getWidthPx()).setDuration(DURATION_REPLY).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
    }

    private void showNbTribes() {
        layoutNbTribes.animate().translationX(0).setDuration(DURATION_REPLY).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
    }

    private void showExitCamera() {
        imgCancelReply.animate().translationX(0).setDuration(DURATION_REPLY).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
    }

    private void hideExitCamera() {
        imgCancelReply.animate().translationX(screenUtils.getWidthPx()).setDuration(DURATION_REPLY).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
    }

    private void openReplyMode(boolean showExitCamera) {
        springReplyMode.setEndValue(0f);
        hideSpeed();
        hideNbTribes();
        if (showExitCamera) showExitCamera();
    }

    private void closeReplyMode() {
        springReplyMode.setEndValue(1f);
        showSpeed();
        showNbTribes();
        hideExitCamera();
        currentView.resumePlayer();
    }

    @OnClick(R.id.imgCancelReply)
    public void stopReply() {
        closeReplyMode();
        viewTile.cancelReplyMode();
        inReplyMode = false;
    }

    @OnClick(R.id.layoutNbTribes)
    public void goToNext() {
        if (tribeList.size() - tribeListSeens.size() > 0)
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    //////////////////////
    //     PLAYER       //
    //////////////////////
    @OnClick(R.id.imgSpeed)
    public void changeSpeed() {
        Float newSpeed;
        if (speedPlayback.get().equals(SPEED_NORMAL)) newSpeed = SPEED_LIGHTLY_FASTER;
        else if (speedPlayback.get().equals(SPEED_LIGHTLY_FASTER)) newSpeed = SPEED_FAST;
        else newSpeed = SPEED_NORMAL;
        speedPlayback.set(newSpeed);

        computeCurrentView();
        currentView.changeSpeed();

        computeImgSpeed();
    }

    private void computeImgSpeed() {
        if (speedPlayback.get().equals(SPEED_NORMAL)) {
            imgSpeed.setImageResource(R.drawable.picto_speed_regular);
        } else if (speedPlayback.get().equals(SPEED_LIGHTLY_FASTER)) {
            imgSpeed.setImageResource(R.drawable.picto_speed_medium);
        } else if (speedPlayback.get().equals(SPEED_FAST)) {
            imgSpeed.setImageResource(R.drawable.picto_speed_fast);
        }
    }

    //////////////////////
    //   OBSERVABLES    //
    //////////////////////
    public Observable<View> onRecordStart() {
        return recordStarted;
    }

    public Observable<View> onRecordEnd() {
        return recordEnded;
    }

    public Observable<View> onClickTapToCancel() {
        return clickTapToCancel;
    }

    public Observable<View> onNotCancel() {
        return onNotCancel;
    }

    public Observable<Void> onDismissHorizontal() {
        return onDismissHorizontal;
    }

    public Observable<Void> onDismissVertical() {
        return onDismissVertical;
    }

    public Observable<View> onClickEnableLocation() {
        return clickEnableLocation;
    }
}