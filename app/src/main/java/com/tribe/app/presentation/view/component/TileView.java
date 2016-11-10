package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
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
import com.tribe.app.R;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.TextViewUtils;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.CameraWrapper;
import com.tribe.app.presentation.view.widget.PlayerView;
import com.tribe.app.presentation.view.widget.SquareFrameLayout;
import com.tribe.app.presentation.view.widget.TextViewAnimatedDots;
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

    public final static int TYPE_GRID = 0;
    public final static int TYPE_TILE = 1;
    public final static int TYPE_SUPPORT = 2;

    private static final float BOUNCINESS_INSIDE = 10f;
    private static final float SPEED_INSIDE = 5f;
    private static final float BOUNCINESS_OUTSIDE = 1f;
    private static final float SPEED_OUTSIDE = 20f;
    private static final SpringConfig SPRING_NO_BOUNCE = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_OUTSIDE, SPEED_OUTSIDE);
    private static final SpringConfig SPRING_BOUNCE = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_INSIDE, SPEED_INSIDE);

    private final float DIFF_DOWN = 20f;
    private final int LONG_PRESS = 200;
    private final int FADE_DURATION = 200;
    private final int SCALE_DURATION = 200;
    private final int END_RECORD_DELAY = 1000;
    private final float OVERSHOOT = 3f;
    private final float TAP_TO_CANCEL_SPRING_VALUE = 0.60f;
    private final float REPLY_OPEN_CAMERA = 0.25f;
    private final float REPLY_RECORD = 1f;
    private final float REPLY_TAP_TO_CANCEL = 0.7f;
    private final int ANIMATION_DELAY = 500;

    @Nullable @BindView(R.id.txtName) public TextViewFont txtName;
    @Nullable @BindView(R.id.btnText) public ImageView btnText;
    @Nullable @BindView(R.id.viewNewText) public View viewNewText;
    @Nullable @BindView(R.id.btnMore) public ImageView btnMore;
    @Nullable @BindView(R.id.txtStatus) public TextViewFont txtStatus;
    @Nullable @BindView(R.id.txtStatusError) public TextViewFont txtStatusError;
    @Nullable @BindView(R.id.txtSending) public TextViewAnimatedDots txtSending;
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
    @Nullable @BindView(R.id.layoutReply) public ViewGroup layoutReply;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private Subscription subscriptionVideoStarted;
    private Unbinder unbinder;
    private Subscription timer;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> onDown = PublishSubject.create();
    private final PublishSubject<View> clickErrorTribes = PublishSubject.create();
    private final PublishSubject<View> clickOpenTribes = PublishSubject.create();
    private final PublishSubject<View> clickChatView = PublishSubject.create();
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
    private final PublishSubject<View> clickTapToCancel = PublishSubject.create();
    private final PublishSubject<View> onNotCancel = PublishSubject.create();
    private final PublishSubject<View> recordStarted = PublishSubject.create();
    private final PublishSubject<View> recordEnded = PublishSubject.create();
    private final PublishSubject<Boolean> replyModeStarted = PublishSubject.create(); // OPEN THE CAMERA WHILE RECORDING
    private final PublishSubject<Boolean> replyModeClickStarted = PublishSubject.create(); // JUST TO OPEN THE CAMERA
    private final PublishSubject<Boolean> replyModeEndedAtRest = PublishSubject.create();

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
    private int replySize;
    private int colorBlackOpacity20;
    private int diffSizeForScale;
    private int diffDown;
    private int replySizeScaled;

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
        sizeAvatarScaled = (type == TYPE_GRID || type == TYPE_SUPPORT) ?
                context.getResources().getDimensionPixelSize(R.dimen.avatar_size_scaled) :
                context.getResources().getDimensionPixelSize(R.dimen.avatar_size_reply_full);
        sizeAvatar = (type == TYPE_GRID || type == TYPE_SUPPORT) ? context.getResources().getDimensionPixelSize(R.dimen.avatar_size) : context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
        sizeAvatarIntermediate = (type == TYPE_GRID || type == TYPE_SUPPORT) ?
                context.getResources().getDimensionPixelSize(R.dimen.avatar_size_intermediate) :
                context.getResources().getDimensionPixelSize(R.dimen.avatar_size_reply_intermediate);
        sizeAvatarInner = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner);
        sizeAvatarInnerIntermediate = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner_intermediate);
        sizeAvatarInnerScaled = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner_scaled);
        replySize = context.getResources().getDimensionPixelSize(R.dimen.reply_size);
        replySizeScaled = context.getResources().getDimensionPixelSize(R.dimen.reply_size_full);
        timeTapToCancel = context.getResources().getInteger(R.integer.time_tap_to_cancel);
        colorBlackOpacity20 = context.getResources().getColor(R.color.black_opacity_20);
        diffSizeForScale = (int) (context.getResources().getDisplayMetrics().density * 0.5);
        diffDown = (int) (context.getResources().getDisplayMetrics().density * DIFF_DOWN);

        int resLayout = 0;

        switch (type) {
            case TYPE_GRID:
                resLayout = R.layout.view_tile_grid;
                break;

            case TYPE_TILE:
                resLayout = R.layout.view_tile_reply;
                break;

            case TYPE_SUPPORT:
                resLayout = R.layout.view_tile_support;
                break;
        }

        LayoutInflater.from(getContext()).inflate(resLayout, this);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void initClicks() {
        if (type == TYPE_GRID || type == TYPE_SUPPORT) {
            prepareTouchesErrorTribe();

            if (type == TYPE_GRID) {
                prepareTouchesChat();
                prepareTouchesMore();
            }
        }

        prepareTouchesTile();
    }

    private void prepareTouchesChat() {
        btnText.setOnClickListener(v -> clickChatView.onNext(this));
    }

    private void prepareTouchesMore() {
        btnMore.setOnClickListener(v -> clickMoreView.onNext(this));
    }

    private void prepareTouchesErrorTribe() {
        txtStatusError.setOnClickListener(v -> clickErrorTribes.onNext(this));
    }

    private void prepareTouchesTile() {
        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isDown) return false;

                onDown.onNext(this);

                if (timer != null) timer.unsubscribe();

                if (getTag(R.id.is_tap_to_cancel) != null)
                    isTapToCancel = (Boolean) getTag(R.id.is_tap_to_cancel);

                if (isTapToCancel) {
                    clickTapToCancel.onNext(this);
                    if (type != TYPE_TILE) resetViewAfterTapToCancel(false);
                    return false;
                } else {
                    longDown = System.currentTimeMillis();
                    downX = currentX = event.getRawX();
                    downY = currentY = event.getRawY();
                    isDown = true;
                    timer = Observable.timer(LONG_PRESS, TimeUnit.MILLISECONDS)
                            .onBackpressureDrop()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time -> {
                                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown
                                        && Math.abs(currentX - downX) < diffDown
                                        && Math.abs(currentY - downY) < diffDown) {
                                    isRecording = true;
                                    isTapToCancel = false;

                                    Spring springInside = (Spring) v.getTag(R.id.spring_inside);
                                    springInside.setSpringConfig(SPRING_BOUNCE);
                                    springInside.setEndValue(1f);

                                    Spring springAvatar = (Spring) v.getTag(R.id.spring_avatar);
                                    springAvatar.setSpringConfig(SPRING_BOUNCE);
                                    springAvatar.setEndValue((type == TYPE_GRID || type == TYPE_SUPPORT) ? 1f : REPLY_RECORD);

                                    if ((type == TYPE_GRID || type == TYPE_SUPPORT)) {
                                        recordStarted.onNext(this);

                                        Spring springOutside = (Spring) v.getTag(R.id.spring_outside);
                                        springOutside.setEndValue(1f);
                                    } else {
                                        Spring springReplyBG = (Spring) v.getTag(R.id.spring_reply_bg);
                                        springReplyBG.setEndValue(REPLY_RECORD);

                                        replyModeStarted.onNext(false);
                                    }
                                }
                            });
                }

                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                currentX = event.getRawX();
                currentY = event.getRawY();
                //if (isRecording) return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (timer != null) timer.unsubscribe();

                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown && isRecording) {
                    recordEnded.onNext(this);
                    isRecording = false;
                } else if (isDown && (System.currentTimeMillis() - longDown) <= LONG_PRESS) {
                    if ((type == TYPE_GRID || type == TYPE_SUPPORT)) {
                        clickOpenTribes.onNext(this);
                    } else {
                        replyModeClickStarted.onNext(true);
                    }
                }

                isDown = false;
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                isDown = false;
            }

            return false;
        });

        SpringSystem springSystem = SpringSystem.create();

        // SPRING INSIDE CONFIGURATION
        Spring springInside = springSystem.createSpring();
        springInside.setSpringConfig(SPRING_BOUNCE);
        springInside.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                float alpha = 1 - value;

                if (type == TYPE_SUPPORT || type == TYPE_GRID) {
                    txtName.setAlpha(alpha);
                    txtStatus.setAlpha(alpha);
                    txtStatusError.setAlpha(alpha);

                    if (type == TYPE_GRID) {
                        btnText.setAlpha(alpha);
                        btnMore.setAlpha(alpha);
                        layoutNbTribes.setAlpha(alpha);
                        viewNewText.setAlpha(alpha);
                    }
                }
            }
        });

        springInside.setEndValue(0f);
        setTag(R.id.spring_inside, springInside);

        // SPRING INSIDE CONFIGURATION
        Spring springAvatar = springSystem.createSpring();
        springAvatar.setSpringConfig((type == TYPE_GRID || type == TYPE_SUPPORT) ? SPRING_BOUNCE : SPRING_NO_BOUNCE);
        springAvatar.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                if ((type == TYPE_GRID || type == TYPE_SUPPORT)) {
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
                } else {
                    int scaleUp = (int) (sizeAvatar + ((sizeAvatarScaled - sizeAvatar) * value));
                    ViewGroup.LayoutParams paramsAvatar = avatar.getLayoutParams();
                    paramsAvatar.height = scaleUp;
                    paramsAvatar.width = scaleUp;
                    avatar.setLayoutParams(paramsAvatar);
                }
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);

                if (currentTribe == null && spring.getEndValue() == REPLY_RECORD && type == TYPE_TILE) {
                    Observable.timer(100, TimeUnit.MILLISECONDS)
                            .onBackpressureDrop()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(aLong -> recordStarted.onNext(TileView.this));
                } else if (currentTribe != null
                        && (spring.getCurrentValue() == TAP_TO_CANCEL_SPRING_VALUE || spring.getCurrentValue() == REPLY_TAP_TO_CANCEL)
                        && currentTribeMode != null && currentTribeMode.equals(CameraWrapper.VIDEO)
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    playerView.createPlayer(FileUtils.getPathForId(getContext(), currentTribe.getLocalId(), FileUtils.VIDEO));

                    if (currentTribeMode.equals(CameraWrapper.VIDEO)) {
                        subscriptionVideoStarted = playerView.videoStarted()
                                .subscribe(view -> {
                                    subscriptionVideoStarted.unsubscribe();
                                    animateTapToCancel();
                                });
                    }
                } else if (currentTribe != null
                        && (spring.getCurrentValue() == TAP_TO_CANCEL_SPRING_VALUE || spring.getCurrentValue() == REPLY_TAP_TO_CANCEL)) {
                    animateTapToCancel();
                }
            }
        });

        springAvatar.setEndValue(0f);
        setTag(R.id.spring_avatar, springAvatar);

        if ((type == TYPE_GRID || type == TYPE_SUPPORT)) {
            final GradientDrawable drawable = (GradientDrawable) viewPressedForeground.getBackground();

            // SPRING OUTSIDE CONFIGURATION
            Spring springOutside = springSystem.createSpring();
            springOutside.setSpringConfig(SPRING_NO_BOUNCE);
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
        } else {
            // SPRING REPLY BACKGROUND CONFIGURATION
            Spring springReplyBG = springSystem.createSpring();
            springReplyBG.setSpringConfig(SPRING_NO_BOUNCE);
            springReplyBG.addListener(new SimpleSpringListener() {
                @Override
                public void onSpringUpdate(Spring spring) {
                    float value = (float) spring.getCurrentValue();

                    int scaleUpReply = (int) (replySize + ((replySizeScaled - replySize) * value));
                    ViewGroup.LayoutParams paramsReplySize = layoutReply.getLayoutParams();
                    paramsReplySize.height = scaleUpReply;
                    paramsReplySize.width = scaleUpReply;
                    layoutReply.setLayoutParams(paramsReplySize);

                    ViewGroup.LayoutParams paramsViewShadow = viewShadow.getLayoutParams();
                    paramsViewShadow.height = scaleUpReply;
                    paramsViewShadow.width = scaleUpReply;
                    viewShadow.setLayoutParams(paramsViewShadow);

                    ViewGroup.LayoutParams paramsTile = getLayoutParams();
                    paramsTile.height = scaleUpReply;
                    paramsTile.width = scaleUpReply;
                    setLayoutParams(paramsTile);
                }
            });

            springReplyBG.setEndValue(0f);
            setTag(R.id.spring_reply_bg, springReplyBG);
        }
    }

    public void resetViewAfterTapToCancel(boolean hasFinished) {
        if (isTapToCancel) {
            if (hasFinished) AnimationUtils.scaleDown(imgDone, SCALE_DURATION, new DecelerateInterpolator());
            else AnimationUtils.scaleDown(imgCancel, SCALE_DURATION, new DecelerateInterpolator());
        }

        AnimationUtils.fadeOut(viewForeground, 0);

        if (getTag(R.id.progress_bar_animation) != null) {
            ObjectAnimator animator = (ObjectAnimator) getTag(R.id.progress_bar_animation);
            animator.cancel();
            setTag(R.id.progress_bar_animation, null);
        }

        progressBar.clearAnimation();
        progressBar.setProgress(0);
        currentTribe = null;

        if (currentTribeMode != null && currentTribeMode.equals(CameraWrapper.VIDEO) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            playerView.hideVideo();

        Spring springInside = (Spring) getTag(R.id.spring_inside);
        springInside.setSpringConfig(SPRING_BOUNCE);
        springInside.setEndValue(0f);

        Spring springAvatar = (Spring) getTag(R.id.spring_avatar);
        springAvatar.setSpringConfig(type == TYPE_TILE ? SPRING_NO_BOUNCE : SPRING_BOUNCE);
        springAvatar.setEndValue(0f);

        if (type == TYPE_TILE) {
            Spring springReplyBG = (Spring) getTag(R.id.spring_reply_bg);
            springReplyBG.setEndValue(0f);
        } else {
            AnimationUtils.fadeOut(txtSending, 0);
            txtSending.stopDotsAnimation();
            Spring springOutside = (Spring) getTag(R.id.spring_outside);
            springOutside.setEndValue(0f);
        }

        setTag(R.id.is_tap_to_cancel, false);

        isTapToCancel = false;
        currentTribeMode = null;

        ((TransitionDrawable) ((LayerDrawable) viewForeground.getBackground()).getDrawable(0)).reverseTransition(FADE_DURATION);
    }

    private void showSending() {
        txtSending.setText(R.string.grid_friendship_recording_tap_to_cancel);
        txtSending.startDotsAnimation();

        AnimationUtils.fadeIn(txtSending, 0);
    }

    public void setInfo(Recipient recipient) {
        // WE DON'T LOAD THE AVATAR AGAIN IF THE URL IS THE SAME
        avatar.load(recipient);

        if ((type == TYPE_GRID || type == TYPE_SUPPORT)) {
            if (recipient instanceof Membership) {
                txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small, 0, 0, 0);
            } else {
                txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            if (type == TYPE_SUPPORT) txtName.setText(R.string.grid_support_title);
            else txtName.setText(recipient.getDisplayName());

            if (recipient.getReceivedTribes() != null && recipient.getReceivedTribes().size() > 0) {
                txtNbTribes.setText("" + recipient.getReceivedTribes().size());
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

    public void setStatus(List<TribeMessage> receivedTribes, List<TribeMessage> sentTribes,
                          List<TribeMessage> errorTribes, List<ChatMessage> receivedMessages) {
        TribeMessage lastTribe = computeMostRecentTribe(receivedTribes, sentTribes, errorTribes);

        boolean isFinalStatus = false, isLoading = false;
        int label = MessageSendingStatus.getStrRes(MessageSendingStatus.STATUS_NONE, type);
        int drawableRes = R.drawable.picto_tap_to_view;
        int textAppearence = R.style.Caption_Black_40;

        if (lastTribe != null && lastTribe.getMessageSendingStatus() != null) {
            switch (lastTribe.getMessageSendingStatus()) {
                case MessageSendingStatus.STATUS_SENDING: case MessageSendingStatus.STATUS_PENDING:
                    label = MessageSendingStatus.getStrRes(lastTribe.getMessageSendingStatus(), type);
                    drawableRes = R.drawable.picto_sending;
                    textAppearence = R.style.Caption_White_1;
                    isFinalStatus = true;
                    break;

                case MessageSendingStatus.STATUS_CONFIRMED:
                    label = MessageSendingStatus.getStrRes(lastTribe.getMessageSendingStatus(), type);
                    drawableRes = R.drawable.picto_delivered;
                    textAppearence = R.style.Caption_White_1;
                    isFinalStatus = true;
                    break;

                case MessageSendingStatus.STATUS_SENT:
                    label = MessageSendingStatus.getStrRes(lastTribe.getMessageSendingStatus(), type);
                    drawableRes = R.drawable.picto_sent;
                    textAppearence = R.style.Caption_White_1;
                    isFinalStatus = true;
                    break;

                case MessageSendingStatus.STATUS_OPENED: case MessageSendingStatus.STATUS_OPENED_PARTLY:
                    label = MessageSendingStatus.getStrRes(lastTribe.getMessageSendingStatus(), type);
                    drawableRes = R.drawable.picto_opened;
                    textAppearence = R.style.Caption_White_1;
                    isFinalStatus = true;
                    break;
            }
        }

        if (!isFinalStatus && receivedTribes != null && receivedTribes.size() > 0) {
            boolean firstLoaded = false;
            TribeMessage tribeMessage = receivedTribes.get(0);

            if (tribeMessage.getMessageDownloadingStatus() != null && tribeMessage.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_DOWNLOADED)) {
                firstLoaded = true;
            }

            if (firstLoaded) {
                label = (type == TYPE_SUPPORT ? R.string.grid_support_status_new_messages : R.string.grid_friendship_status_new_messages);
                drawableRes = R.drawable.picto_tap_to_view;
                textAppearence = R.style.Caption_Black_40;
            } else if (!firstLoaded && tribeMessage.isDownloadPending()) {
                isLoading = true;
                label = (type == TYPE_SUPPORT ? R.string.grid_support_status_loading : R.string.grid_friendship_status_loading);
                drawableRes = R.drawable.picto_loading;
                textAppearence = R.style.Caption_Black_40;
            }
        }

        if (errorTribes != null && errorTribes.size() > 0) {
            txtStatus.setVisibility(View.GONE);
            txtStatusError.setVisibility(View.VISIBLE);
            txtStatusError.setText("" + errorTribes.size());
        } else {
            txtStatusError.setVisibility(View.GONE);
            txtStatus.setVisibility(View.VISIBLE);

            TextViewUtils.setTextAppearence(getContext(), txtStatus, textAppearence);

            txtStatus.setText(label);
            txtStatus.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(drawableRes), null, null, null);

            if (isLoading) {
                if (circularProgressView.getVisibility() == View.GONE) {
                    txtNbTribes.setVisibility(View.GONE);
                    circularProgressView.setVisibility(View.VISIBLE);
                }
            } else if (circularProgressView.getVisibility() == View.VISIBLE) {
                txtNbTribes.setVisibility(View.VISIBLE);
                circularProgressView.setVisibility(View.GONE);
            }
        }

        if (viewNewText != null) viewNewText.setVisibility(receivedMessages != null && receivedMessages.size() > 0 ? View.VISIBLE : View.GONE);
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

        if ((type == TYPE_GRID || type == TYPE_SUPPORT)) {
            Spring springOutside = (Spring) getTag(R.id.spring_outside);
            springOutside.setEndValue(0f);
        } else {
            Spring springReplyBG = (Spring) getTag(R.id.spring_reply_bg);
            springReplyBG.setEndValue(TAP_TO_CANCEL_SPRING_VALUE);
        }

        Spring springAvatar = (Spring) getTag(R.id.spring_avatar);
        springAvatar.setSpringConfig(SPRING_NO_BOUNCE);
        springAvatar.setEndValue((type == TYPE_GRID || type == TYPE_SUPPORT) ? TAP_TO_CANCEL_SPRING_VALUE : REPLY_TAP_TO_CANCEL);

        setTag(R.id.is_tap_to_cancel, true);
        isTapToCancel = true;
    }

    private void animateTapToCancel() {
        AnimationUtils.fadeIn(viewForeground, 0);
        ((TransitionDrawable) ((LayerDrawable) viewForeground.getBackground()).getDrawable(0)).startTransition(FADE_DURATION);
        AnimationUtils.scaleUp(imgCancel, SCALE_DURATION, new OvershootInterpolator(OVERSHOOT));

        if (type != TYPE_TILE) showSending();

        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, timeTapToCancel);
        animation.setDuration(timeTapToCancel);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                AnimationUtils.scaleDown(imgCancel, SCALE_DURATION, new DecelerateInterpolator());
                AnimationUtils.scaleUp(imgDone, SCALE_DURATION, SCALE_DURATION, new OvershootInterpolator(OVERSHOOT));

                if (type != TYPE_TILE) {
                    txtSending.stopDotsAnimation();
                    txtSending.setText(R.string.grid_friendship_recording_validated);
                }

                Observable.timer(END_RECORD_DELAY, TimeUnit.MILLISECONDS)
                        .onBackpressureDrop()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(time -> {
                            onNotCancel.onNext(TileView.this);
                            if (type != TYPE_TILE) resetViewAfterTapToCancel(true);
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

    public void openCameraMode() {
        Spring springAvatar = (Spring) getTag(R.id.spring_avatar);
        springAvatar.setEndValue(REPLY_OPEN_CAMERA);
    }

    private TribeMessage computeMostRecentTribe(List<TribeMessage> received, List<TribeMessage> sent, List<TribeMessage> error) {
        TribeMessage recentReceived = received != null && received.size() > 0 ? received.get(received.size() - 1) : null;
        TribeMessage recentSent = sent != null && sent.size() > 0 ? sent.get(sent.size() - 1) : null;
        TribeMessage recentError = error != null && error.size() > 0 ? error.get(error.size() - 1) : null;
        return TribeMessage.getMostRecentTribe(recentReceived, recentSent, recentError);
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

    public int getType() {
        return type;
    }

    public void cancelReplyMode() {
        Spring springAvatar = (Spring) getTag(R.id.spring_avatar);
        springAvatar.setEndValue(0);

        if (type == TYPE_TILE) {
            Spring springReplyBG = (Spring) getTag(R.id.spring_reply_bg);
            springReplyBG.setEndValue(0);
        }
    }

    // OBSERVABLES
    public Observable<View> onDown() { return onDown; }

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

    public Observable<Boolean> onReplyModeStarted() {
        return replyModeStarted;
    }

    public Observable<Boolean> onReplyModeClickStarted() {
        return replyModeClickStarted;
    }
}
