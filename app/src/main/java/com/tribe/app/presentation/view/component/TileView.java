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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.MessageStatus;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.PlayerView;
import com.tribe.app.presentation.view.widget.SquareFrameLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
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
    private final float DIFF_DOWN = 20f;
    private final int LONG_PRESS = 150;
    private final int FADE_DURATION = 200;
    private final int SCALE_DURATION = 200;
    private final int END_RECORD_DELAY = 1000;
    private final float OVERSHOOT = 3f;
    private final float TAP_TO_CANCEL_SPRING_VALUE = 0.60f;
    private final int ANIMATION_DELAY = 500;

    @BindView(R.id.txtName) public TextViewFont txtName;
    @Nullable @BindView(R.id.btnText) public ImageView btnText;
    @Nullable @BindView(R.id.btnMore) public ImageView btnMore;
    @BindView(R.id.txtStatus) public TextViewFont txtStatus;
    @Nullable @BindView(R.id.txtStatusError) public TextViewFont txtStatusError;
    @BindView(R.id.txtSending) public TextViewFont txtSending;
    @BindView(R.id.viewShadow) public View viewShadow;
    @BindView(R.id.avatar) public AvatarView avatar;
    @BindView(R.id.progressBar) public ProgressBar progressBar;
    @BindView(R.id.viewForeground) public View viewForeground;
    @BindView(R.id.imgCancel) public ImageView imgCancel;
    @BindView(R.id.imgDone) public ImageView imgDone;
    @Nullable @BindView(R.id.viewPressedForeground) public View viewPressedForeground;
    @BindView(R.id.viewPlayer) public PlayerView playerView;
    @Nullable @BindView(R.id.txtNbTribes) public TextViewFont txtNbTribes;
    @Nullable @BindView(R.id.layoutNbTribes) public FrameLayout layoutNbTribes;
    @Nullable @BindView(R.id.circularProgressView) public CircularProgressView circularProgressView;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private Subscription subscriptionVideoStarted;
    private Unbinder unbinder;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickErrorTribes = PublishSubject.create();
    private final PublishSubject<View> clickOpenTribes = PublishSubject.create();
    private final PublishSubject<View> clickChatView = PublishSubject.create();
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
    private final PublishSubject<View> clickTapToCancel = PublishSubject.create();
    private final PublishSubject<View> onNotCancel = PublishSubject.create();
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
    private int diffDown;

    // VARIABLES
    private int type;
    private long longDown = 0L;
    private boolean isDown = false;
    private float downX, downY, currentX, currentY;
    private TribeMessage currentTribe;
    private @CameraWrapper.TribeMode String currentTribeMode;
    private boolean isRecording = false;
    private boolean isTapToCancel = false;

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
        diffDown = (int) (context.getResources().getDisplayMetrics().density * DIFF_DOWN);

        LayoutInflater.from(getContext()).inflate((type == TYPE_GRID ? R.layout.view_tile_grid : R.layout.view_tile_reply), this);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void initWithParent(ViewGroup parent) {
        if (type == TYPE_GRID) {
            prepareTouchesChat(parent);
            prepareTouchesMore(parent);
            prepareTouchesErrorTribe(parent);
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

    private void prepareTouchesErrorTribe(ViewGroup parent) {
        subscriptions.add(RxView.clicks(txtStatusError)
                .takeUntil(RxView.detaches(parent))
                .map(aVoid -> this)
                .subscribe(clickErrorTribes));
    }

    private void prepareTouchesTile() {
        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isDown) return false;

                if (getTag(R.id.is_tap_to_cancel) != null)
                    isTapToCancel = (Boolean) getTag(R.id.is_tap_to_cancel);

                if (isTapToCancel) {
                    clickTapToCancel.onNext(this);
                    resetViewAfterTapToCancel(false);
                    return false;
                } else {
                    longDown = System.currentTimeMillis();
                    downX = currentX = event.getRawX();
                    downY = currentY = event.getRawY();
                    isDown = true;
                    Observable.timer(LONG_PRESS, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time -> {
                                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown
                                        && Math.abs(currentX - downX) < diffDown
                                        && Math.abs(currentY - downY) < diffDown) {
                                    recordStarted.onNext(this);
                                    isRecording = true;
                                    isTapToCancel = false;

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
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                currentX = event.getRawX();
                currentY = event.getRawY();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown) {
                    recordEnded.onNext(this);
                    isRecording = false;
                } else if (isDown && (System.currentTimeMillis() - longDown) <= LONG_PRESS) {
                    clickOpenTribes.onNext(this);
                }

                isDown = false;
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
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
                    txtStatusError.setAlpha(alpha);
                    layoutNbTribes.setAlpha(alpha);
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

                if (currentTribe != null && spring.getCurrentValue() == TAP_TO_CANCEL_SPRING_VALUE && currentTribeMode.equals(CameraWrapper.VIDEO)) {
                    playerView.play();
                } else if (currentTribe != null && spring.getCurrentValue() == TAP_TO_CANCEL_SPRING_VALUE) {
                    animateTapToCancel();
                }
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

        if (currentTribeMode.equals(CameraWrapper.VIDEO))
            playerView.hideVideo();

        Spring springInside = (Spring) getTag(R.id.spring_inside);
        springInside.setEndValue(0f);

        Spring springAvatar = (Spring) getTag(R.id.spring_avatar);
        springAvatar.setEndValue(0f);

        setTag(R.id.is_tap_to_cancel, false);
        isTapToCancel = false;

        ((TransitionDrawable) ((LayerDrawable) viewForeground.getBackground()).getDrawable(0)).reverseTransition(FADE_DURATION);
    }

    private void showSending() {
        txtSending.setText(R.string.Grid_User_Sending);

        AnimationUtils.fadeIn(txtSending, 0);
    }

    public void setInfo(String name, String urlAvatar, List<TribeMessage> receivedTribes) {
        txtName.setText(name);

        // WE DON'T LOAD THE AVATAR AGAIN IF THE URL IS THE SAME
        String previousAvatar = (String) avatar.getTag(R.id.profile_picture);
        if (previousAvatar == null || !previousAvatar.equals(urlAvatar)) {
            avatar.setTag(R.id.profile_picture, urlAvatar);
            avatar.load(urlAvatar);
        }

        if (type == TYPE_GRID) {
            if (receivedTribes != null && receivedTribes.size() > 0) {
                txtNbTribes.setText("" + receivedTribes.size());
                if (layoutNbTribes.getScaleX() == 0) {
                    layoutNbTribes.animate().scaleX(1).scaleY(1).setDuration(0).setStartDelay(0).setInterpolator(new OvershootInterpolator(OVERSHOOT)).start();
                }
            } else {
                txtNbTribes.setText("");
                if (layoutNbTribes.getScaleX() == 1) {
                    layoutNbTribes.animate().scaleX(0).scaleY(0).setDuration(0).start();
                }
            }
        }
    }

    public void setStatus(List<TribeMessage> receivedTribes, List<TribeMessage> sentTribes, List<TribeMessage> errorTribes) {
        @MessageStatus.Status String ultimateMessageStatus = computeStatus(receivedTribes, sentTribes, errorTribes);

        if (ultimateMessageStatus != null && ultimateMessageStatus.equals(MessageStatus.STATUS_ERROR)) {
            txtStatus.setVisibility(View.GONE);
            txtStatusError.setVisibility(View.VISIBLE);
            txtStatusError.setText("" + errorTribes.size());
        } else {
            txtStatusError.setVisibility(View.GONE);
            txtStatus.setVisibility(View.VISIBLE);
            txtStatus.setText(computeStrStatus(ultimateMessageStatus));
            txtStatus.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(computeIconStatus(ultimateMessageStatus)), null, null, null);
        }

        if (ultimateMessageStatus.equals(MessageStatus.STATUS_LOADING) && circularProgressView.getVisibility() == View.GONE) {
            txtNbTribes.setVisibility(View.GONE);
            circularProgressView.setVisibility(View.VISIBLE);
        } else if (circularProgressView.getVisibility() == View.VISIBLE) {
            txtNbTribes.setVisibility(View.VISIBLE);
            circularProgressView.setVisibility(View.GONE);
        }
    }

    public void setBackground(int position) {
        setBackgroundColor(PaletteGrid.get(position - 1));
    }

    public void setAvatarScale(float scale, int duration, int delay, Interpolator interpolator) {
        avatar.animate().scaleX(scale).scaleY(scale).setDuration(duration).setStartDelay(delay).setInterpolator(interpolator).start();
    }

//    public void preparePlayer(String localId) {
//        if (localId != null) {
//            playerView.createPlayer(FileUtils.getPathForId(localId));
//        }
//    }

    public void showTapToCancel(TribeMessage tribe, @CameraWrapper.TribeMode String tribeMode) {
        currentTribe = tribe;
        currentTribeMode = tribeMode;

        if (type == TYPE_GRID) {
            Spring springOutside = (Spring) getTag(R.id.spring_outside);
            springOutside.setEndValue(0f);
        }

        Spring springAvatar = (Spring) getTag(R.id.spring_avatar);
        springAvatar.setEndValue(TAP_TO_CANCEL_SPRING_VALUE);

        setTag(R.id.is_tap_to_cancel, true);
        isTapToCancel = true;

        if (tribeMode.equals(CameraWrapper.VIDEO)) {
            if (currentTribe != null) {
                playerView.createPlayer(FileUtils.getPathForId(currentTribe.getLocalId()));
            }

            subscriptionVideoStarted = playerView.videoStarted()
                    .subscribe(view -> {
                        subscriptionVideoStarted.unsubscribe();
                        animateTapToCancel();
                    });
        }
    }

    private void animateTapToCancel() {
        AnimationUtils.fadeIn(viewForeground, 0);
        ((TransitionDrawable) ((LayerDrawable) viewForeground.getBackground()).getDrawable(0)).startTransition(FADE_DURATION);
        AnimationUtils.scaleUp(imgCancel, SCALE_DURATION, new OvershootInterpolator(OVERSHOOT));

        showSending();

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
                            onNotCancel.onNext(TileView.this);
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

    private String computeStatus(List<TribeMessage> received, List<TribeMessage> sent, List<TribeMessage> error) {
        TribeMessage endTribe = computeMostRecentTribe(received, sent, error);
        if (endTribe == null) return MessageStatus.STATUS_NONE;
        else if (endTribe != null && endTribe.getMessageStatus() == null) return MessageStatus.STATUS_RECEIVED;
        return endTribe.getMessageStatus();
    }

    private String computeStrStatus(@MessageStatus.Status String status) {
        return MessageStatus.getStrRes(getContext(), status);
    }

    private int computeIconStatus(@MessageStatus.Status String status) {
        return MessageStatus.getIconRes(status);
    }

    private TribeMessage computeMostRecentTribe(List<TribeMessage> received, List<TribeMessage> sent, List<TribeMessage> error) {
        TribeMessage recentReceived = received != null && received.size() > 0 ? received.get(received.size() - 1) : null;
        TribeMessage recentSent = sent != null && sent.size() > 0 ? sent.get(sent.size() - 1) : null;
        TribeMessage recentError = error != null && error.size() > 0 ? error.get(error.size() - 1) : null;
        return TribeMessage.getMostRecentTribe(recentReceived, recentSent, recentError);
    }

    public Observable<View> onClickErrorTribes() {
        return clickErrorTribes;
    }

    public Observable<View> onOpenTribes() {
        return clickOpenTribes;
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

    public Observable<View> onTapToCancel() {
        return clickTapToCancel;
    }

    public Observable<View> onNotCancel() {
        return onNotCancel;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isTapToCancel() {
        return isTapToCancel;
    }

    public String getCurrentTribeMode() {
        return currentTribeMode;
    }
}
