package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.PlayerView;
import com.tribe.app.presentation.view.widget.SquareFrameLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class TileView extends SquareFrameLayout {

    private final static int TYPE_GRID = 0;
    private final static int TYPE_TILE = 1;

    private final float BOUNCINESS_INSIDE = 15f;
    private final float SPEED_INSIDE = 12.5f;
    private final float BOUNCINESS_OUTSIDE = 1f;
    private final float SPEED_OUTSIDE = 20f;
    private final int LONG_PRESS = 200;
    private final int FADE_DURATION = 200;
    private final int SCALE_DURATION = 200;
    private final int END_RECORD_DELAY = 1000;
    private final float OVERSHOOT = 3f;
    private final float TAP_TO_CANCEL_SPRING_VALUE = 0.60f;

    @BindView(R.id.txtName) public TextViewFont txtName;
    @Nullable @BindView(R.id.btnText) public ImageView btnText;
    @Nullable @BindView(R.id.btnMore) public ImageView btnMore;
    @BindView(R.id.txtStatus) public TextViewFont txtStatus;
    @BindView(R.id.txtSending) public TextViewFont txtSending;
    @BindView(R.id.viewShadow) public View viewShadow;
    @BindView(R.id.avatar) public AvatarView avatar;
    @BindView(R.id.progressBar) public ProgressBar progressBar;
    @BindView(R.id.viewForeground) public View viewForeground;
    @BindView(R.id.imgCancel) public ImageView imgCancel;
    @BindView(R.id.imgDone) public ImageView imgDone;
    @Nullable @BindView(R.id.viewPressedForeground) public View viewPressedForeground;
    @BindView(R.id.viewPlayer) public PlayerView playerView;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private Unbinder unbinder;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickChatView = PublishSubject.create();
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
    private final PublishSubject<View> recordStarted = PublishSubject.create();
    private final PublishSubject<View> recordEnded = PublishSubject.create();

    // RESOURCES
    private int transitionGridPressed;
    private int timeTapToCancel;
    private int sizePressedBorder;
    private int sizeAvatar;
    private int sizeAvatarIntermediate;
    private int sizeAvatarScaled;
    private int sizeAvatarInner;
    private int sizeAvatarInnerIntermediate;
    private int sizeAvatarInnerScaled;
    private int colorBlackOpacity20;
    private int diffSizeForScale;

    // VARIABLES
    private int type;
    private long longDown = 0L;
    private boolean isDown = false;
    private Tribe currentTribe;

    public TileView(Context context) {
        super(context);
        init(context, null);
    }

    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileView);
        type = a.getInt(R.styleable.TileView_tileType, TYPE_GRID);
        a.recycle();

        subscriptions = new CompositeSubscription();

        transitionGridPressed = context.getResources().getDimensionPixelSize(R.dimen.transition_grid_pressed);
        sizePressedBorder = context.getResources().getDimensionPixelSize(R.dimen.inside_cell_border);
        sizeAvatarScaled = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_scaled);
        sizeAvatar = context.getResources().getDimensionPixelSize(R.dimen.avatar_size);
        sizeAvatarIntermediate = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_intermediate);
        sizeAvatarInner = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner);
        sizeAvatarInnerIntermediate = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner_intermediate);
        sizeAvatarInnerScaled = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner_scaled);
        timeTapToCancel = context.getResources().getInteger(R.integer.time_tap_to_cancel);
        colorBlackOpacity20 = context.getResources().getColor(R.color.black_opacity_20);
        diffSizeForScale = (int) (context.getResources().getDisplayMetrics().density * 0.5);
    }

    @Override
    protected void onDetachedFromWindow() {
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
        LayoutInflater.from(getContext()).inflate((type == TYPE_GRID ? R.layout.view_tile_grid : R.layout.view_tile_reply), this);
        unbinder = ButterKnife.bind(this);
    }

    public void initWithParent(ViewGroup parent) {
        if (type == TYPE_GRID) {
            prepareTouchesChat(parent);
            prepareTouchesMore(parent);
        }

        prepareTouchesTile();
    }

    private void prepareTouchesChat(ViewGroup parent) {
        subscriptions.add(RxView.clicks(btnText)
                .takeUntil(RxView.detaches(parent))
                .map(aVoid -> this)
                .subscribe(clickChatView));
    }

    private void prepareTouchesMore(ViewGroup parent) {
        subscriptions.add(RxView.clicks(btnMore)
                .takeUntil(RxView.detaches(parent))
                .map(aVoid -> this)
                .subscribe(clickMoreView));
    }

    private void prepareTouchesTile() {
        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isDown) return false;

                boolean isTapToCancel = false;

                if (getTag(R.id.is_tap_to_cancel) != null)
                    isTapToCancel = (Boolean) getTag(R.id.is_tap_to_cancel);

                if (isTapToCancel) {
                    resetViewAfterTapToCancel(false);
                    return false;
                } else {
                    longDown = System.currentTimeMillis();
                    isDown = true;
                    Observable.timer(LONG_PRESS, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time -> {
                                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown) {
                                    recordStarted.onNext(this);

                                    Spring springInside = (Spring) v.getTag(R.id.spring_inside);
                                    springInside.setEndValue(1f);

                                    Spring springAvatar = (Spring) v.getTag(R.id.spring_avatar);
                                    springAvatar.setEndValue(1f);

                                    if (type == TYPE_GRID) {
                                        Spring springOutside = (Spring) v.getTag(R.id.spring_outside);
                                        springOutside.setEndValue(1f);
                                    }
                                }
                            });
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown) {
                    recordEnded.onNext(this);
                }

                isDown = false;
            }

            return true;
        });

        SpringSystem springSystem = SpringSystem.create();

        // SPRING INSIDE CONFIGURATION
        Spring springInside = springSystem.createSpring();
        SpringConfig configInside = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_INSIDE, SPEED_INSIDE);
        springInside.setSpringConfig(configInside);
        springInside.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                float alpha = 1 - value;
                txtName.setAlpha(alpha);
                txtStatus.setAlpha(alpha);

                if (type == TYPE_GRID) {
                    btnText.setAlpha(alpha);
                    btnMore.setAlpha(alpha);
                }
            }
        });

        springInside.setEndValue(0f);
        setTag(R.id.spring_inside, springInside);

        // SPRING INSIDE CONFIGURATION
        Spring springAvatar = springSystem.createSpring();
        SpringConfig configAvatar = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_INSIDE, SPEED_INSIDE);
        springAvatar.setSpringConfig(configAvatar);
        springAvatar.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                int scaleUp = (int) (sizeAvatar + ((sizeAvatarScaled - sizeAvatar) * value));
                ViewGroup.LayoutParams paramsAvatar = avatar.getLayoutParams();

                if (Math.abs(scaleUp - paramsAvatar.height) > diffSizeForScale) {
                    paramsAvatar.height = scaleUp;
                    paramsAvatar.width = scaleUp;
                    avatar.setLayoutParams(paramsAvatar);

                    float scale = 1f + (value * (((float) sizeAvatarInnerScaled / sizeAvatarInner) - 1));
                    progressBar.setScaleX(scale);
                    progressBar.setScaleY(scale);

                    viewForeground.setScaleX(scale);
                    viewForeground.setScaleY(scale);

                    viewShadow.setScaleX(scale);
                    viewShadow.setScaleY(scale);
                }
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);

                if (currentTribe != null && spring.getCurrentValue() == TAP_TO_CANCEL_SPRING_VALUE) {
                    playerView.showPlayer();
                }

                if (spring.getCurrentValue() == 0f) playerView.releasePlayer();
            }
        });

        springAvatar.setEndValue(0f);
        setTag(R.id.spring_avatar, springAvatar);

        if (type == TYPE_GRID) {
            final GradientDrawable drawable = (GradientDrawable) viewPressedForeground.getBackground();

            // SPRING OUTSIDE CONFIGURATION
            Spring springOutside = springSystem.createSpring();
            SpringConfig configOutside = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_OUTSIDE, SPEED_OUTSIDE);
            springOutside.setSpringConfig(configOutside);
            springOutside.addListener(new SimpleSpringListener() {
                @Override
                public void onSpringUpdate(Spring spring) {
                    float value = (float) spring.getCurrentValue();

                    int borderSize = (int) (sizePressedBorder * value);
                    drawable.setStroke(borderSize, colorBlackOpacity20);
                }
            });

            springOutside.setEndValue(0f);
            setTag(R.id.spring_outside, springOutside);
        }
    }

    private void resetViewAfterTapToCancel(boolean hasFinished) {
        if (hasFinished) AnimationUtils.scaleDown(imgDone, SCALE_DURATION);
        else AnimationUtils.scaleDown(imgCancel, SCALE_DURATION);

        AnimationUtils.fadeOut(viewForeground, 0);
        AnimationUtils.fadeOut(txtSending, 0);

        if (getTag(R.id.progress_bar_animation) != null) {
            ObjectAnimator animator = (ObjectAnimator) getTag(R.id.progress_bar_animation);
            animator.cancel();
            setTag(R.id.progress_bar_animation, null);
        }

        progressBar.clearAnimation();
        progressBar.setProgress(0);
        currentTribe = null;
        playerView.hideVideo();

        Spring springInside = (Spring) getTag(R.id.spring_inside);
        springInside.setEndValue(0f);

        Spring springAvatar = (Spring) getTag(R.id.spring_avatar);
        springAvatar.setEndValue(0f);

        setTag(R.id.is_tap_to_cancel, false);

        ((TransitionDrawable) ((LayerDrawable) viewForeground.getBackground()).getDrawable(0)).reverseTransition(FADE_DURATION);
    }

    private void showSending() {
        txtSending.setText(R.string.Grid_User_Sending);

        AnimationUtils.fadeIn(txtSending, 0);

        Spring springAvatar = (Spring) getTag(R.id.spring_avatar);
        springAvatar.setEndValue(TAP_TO_CANCEL_SPRING_VALUE);
    }

    public void setInfo(User user) {
        txtName.setText(user.getDisplayName());
        avatar.load(user.getProfilePicture());
    }

    public void setBackground(int position) {
        setBackgroundColor(PaletteGrid.get(position - 1));
    }

    public void setAvatarScale(float scale, int duration, int delay, Interpolator interpolator) {
        avatar.animate().scaleX(scale).scaleY(scale).setDuration(duration).setStartDelay(delay).setInterpolator(interpolator).start();
    }

    public void showTapToCancel(Tribe tribe) {
        currentTribe = tribe;

        if (currentTribe != null) {
            playerView.createPlayer(FileUtils.getPathForId(currentTribe.getId()));
        }

        AnimationUtils.fadeIn(viewForeground, 0);
        ((TransitionDrawable) ((LayerDrawable) viewForeground.getBackground()).getDrawable(0)).startTransition(FADE_DURATION);
        AnimationUtils.scaleUp(imgCancel, SCALE_DURATION, new OvershootInterpolator(OVERSHOOT));

        if (type == TYPE_GRID) {
            Spring springOutside = (Spring) getTag(R.id.spring_outside);
            springOutside.setEndValue(0f);
        }

        showSending();

        setTag(R.id.is_tap_to_cancel, true);

        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, timeTapToCancel);
        animation.setDuration(timeTapToCancel);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                AnimationUtils.scaleDown(imgCancel, SCALE_DURATION);
                AnimationUtils.scaleUp(imgDone, SCALE_DURATION, SCALE_DURATION, new OvershootInterpolator(OVERSHOOT));

                txtSending.setText(R.string.Grid_User_Sent);

                Observable.timer(END_RECORD_DELAY, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(time -> {
                            resetViewAfterTapToCancel(true);
                        });
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeAllListeners();
            }
        });
        animation.start();

        setTag(R.id.progress_bar_animation, animation);
    }

    public Observable<View> onClickChat() {
        return clickChatView;
    }

    public Observable<View> onClickMore() {
        return clickMoreView;
    }

    public Observable<View> onRecordStart() {
        return recordStarted;
    }

    public Observable<View> onRecordEnd() {
        return recordEnded;
    }
}
