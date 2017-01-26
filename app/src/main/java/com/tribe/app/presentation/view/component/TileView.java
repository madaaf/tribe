package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.component.live.LiveRowView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.SquareCardView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.Avatar;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;

import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class TileView extends SquareCardView {

    public final static int TYPE_GRID_LIVE_CO = 0;
    public final static int TYPE_INVITE_LIVE_CO = 1;
    public final static int TYPE_NORMAL = 2;
    public final static int TYPE_INVITE = 3;

    private final float SCALE_FACTOR = 1.75f;
    private final float SCALE_TILE_FACTOR = 1.3f;
    private final float SCALE_DOWN_BG_FACTOR = 1.75f;
    private final float SCALE_DOWN_MASTER_BG_FACTOR_LOW = 0.9f;
    private final float SCALE_DOWN_BG_FACTOR_LOW = 0.8f;
    private final int RADIUS_MIN = 0;
    private final int RADIUS_MAX = 5;
    private final int ELEVATION_MIN = 0;
    private final int ELEVATION_MAX = 5;
    private final int ROTATION_MIN = 0;
    private final int ROTATION_MAX = 6;
    private final float ALPHA_TILES_MAX = 0.4f;
    private final float ALPHA_TILES_MIN = 0f;
    private final int ROTATION_BG_1_MIN = ROTATION_MAX;
    private final int ROTATION_BG_1_MAX = ROTATION_MIN;
    private final int ROTATION_BG_2_MIN = 0;
    private final int ROTATION_BG_2_MAX = 12;

    private static final float BOUNCINESS_DOWN = 10f;
    private static final float SPEED_DOWN = 5f;
    private static final float BOUNCINESS_UP = 1f;
    private static final float SPEED_UP = 20f;
    private static final SpringConfig SPRING_BOUNCE = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_DOWN, SPEED_DOWN);
    private static final SpringConfig SPRING_NO_BOUNCE = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_UP, SPEED_UP);

    @Inject
    ScreenUtils screenUtils;

    @Nullable
    @BindView(R.id.txtName)
    public TextViewFont txtName;

    @Nullable
    @BindView(R.id.viewShadowAvatar)
    public View viewShadowAvatar;

    @BindView(R.id.avatar)
    public View avatar;

    @Nullable
    @BindView(R.id.layoutPulse)
    public PulseLayout layoutPulse;

    @Nullable
    @BindView(R.id.txtStatus)
    public TextViewFont txtStatus;

    @BindView(R.id.viewBG)
    View viewBG;

    @Nullable
    @BindView(R.id.viewBGTransparent1)
    View viewBGTransparent1;

    @Nullable
    @BindView(R.id.viewBGTransparent2)
    View viewBGTransparent2;

    @Nullable
    @BindView(R.id.imgInd)
    ImageView imgInd;

    // OBSERVABLES
    private CompositeSubscription subscriptions;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
    private final PublishSubject<View> click = PublishSubject.create();
    private final PublishSubject<Void> onEndDrop = PublishSubject.create();

    // RESOURCES
    private int cardRadiusMin, cardRadiusMax, diffCardRadius,
            cardElevationMin, cardElevationMax, diffCardElevation,
            rotationMin, rotationMax, diffRotation,
            minSize, maxSize, sizeDiff,
            rotationBG1Min, rotationBG1Max, diffRotationBG1,
            rotationBG2Min, rotationBG2Max, diffRotationBG2, smallAvatarSize;
    private float alphaTilesMin, alphaTilesMax, alphaTilesDiff;

    // VARIABLES
    private Unbinder unbinder;
    private Recipient recipient;
    private int type, position;
    private int sizeAvatar, sizeAvatarScaled, diffSizeAvatar;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springInside;

    public TileView(Context context, int type) {
        super(context);
        this.type = type;
        init(true);
    }

    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileView);
        type = a.getInt(R.styleable.TileView_tileType, TYPE_NORMAL);
        a.recycle();

        init(false);
    }

    public void init(boolean isDragging) {
        subscriptions = new CompositeSubscription();

        int resLayout = 0;

        switch (type) {
            case TYPE_GRID_LIVE_CO:
                resLayout = R.layout.view_tile_grid_live_co;
                break;

            case TYPE_INVITE_LIVE_CO:
                resLayout = R.layout.view_tile_invite_live_co;
                break;

            case TYPE_NORMAL:
                resLayout = R.layout.view_tile_grid;
                break;

            case TYPE_INVITE:
                resLayout = R.layout.view_tile_invite;
                break;
        }

        LayoutInflater.from(getContext()).inflate(resLayout, this);
        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        initResources();
        initSprings();
        initSize();

        setCardElevation(0);
        ViewCompat.setElevation(this, 0);
        setRadius(0);
        setBackground(null);
        setCardBackgroundColor(Color.TRANSPARENT);

        if (!isDragging) {
            setUseCompatPadding(false);
            setPreventCornerOverlap(true);

            if (type == TYPE_GRID_LIVE_CO)
                layoutPulse.start();
        }

        if (!isGrid()) {
            minSize = screenUtils.getWidthPx() / 3;
            maxSize = (int) (minSize * SCALE_TILE_FACTOR);
            sizeDiff = maxSize - minSize;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void initResources() {
        cardRadiusMax = screenUtils.dpToPx(RADIUS_MAX);
        cardRadiusMin = screenUtils.dpToPx(RADIUS_MIN);
        diffCardRadius = cardRadiusMax - cardRadiusMin;
        cardElevationMax = screenUtils.dpToPx(ELEVATION_MAX);
        cardElevationMin = screenUtils.dpToPx(ELEVATION_MIN);
        diffCardElevation = cardElevationMax - cardElevationMin;
        rotationMax = ROTATION_MAX;
        rotationMin = ROTATION_MIN;
        diffRotation = rotationMax - rotationMin;
        alphaTilesMin = ALPHA_TILES_MIN;
        alphaTilesMax = ALPHA_TILES_MAX;
        alphaTilesDiff = alphaTilesMax - alphaTilesMin;
        rotationBG1Max = ROTATION_BG_1_MAX;
        rotationBG1Min = ROTATION_BG_1_MIN;
        diffRotationBG1 = rotationBG1Max - rotationBG1Min;
        rotationBG2Min = ROTATION_BG_2_MIN;
        rotationBG2Max = ROTATION_BG_2_MAX;
        diffRotationBG2 = rotationBG2Max - rotationBG2Min;
        smallAvatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_medium);
    }

    private void initSprings() {
        springSystem = SpringSystem.create();
        springInside = springSystem.createSpring();
        springInside.setSpringConfig(SPRING_NO_BOUNCE);
        springInside.addListener(new SimpleSpringListener() {

            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                if (Math.abs(value - spring.getEndValue()) < 0.05) value = (float) spring.getEndValue();

                float alpha = 1 - value;
                txtName.setAlpha(alpha);
                if (imgInd != null) imgInd.setAlpha((float) SpringUtil.mapValueFromRangeToRange(alpha, 1, 0, 1, -10)); // Should disappear faster ^^

                float scale = 1f + (value * (((float) sizeAvatarScaled / sizeAvatar) - 1));

                avatar.setScaleX(scale);
                avatar.setScaleY(scale);

                if (viewShadowAvatar != null) {
                    viewShadowAvatar.setScaleX(scale);
                    viewShadowAvatar.setScaleY(scale);
                }

                int cardRadius = Math.max((int) (cardRadiusMin + (diffCardRadius * value)), cardRadiusMin);
                setRadius(cardRadius);

                int cardElevation = Math.max((int) (cardElevationMin + (diffCardElevation * value)), cardElevationMin);
                setCardElevation(cardElevation);

                int rotation = Math.max((int) (rotationMin + (diffRotation * value)), rotationMin);
                viewBG.setRotation(rotation);
                avatar.setRotation(rotation);

                int sizeOfTile = Math.max((int) (minSize + (sizeDiff * value)), minSize);
                UIUtils.changeSizeOfView(TileView.this, sizeOfTile);

                int size = Math.max((int) (minSize + ((sizeDiff / SCALE_DOWN_BG_FACTOR) * value)), minSize);
                UIUtils.changeSizeOfView(viewBG, size);
            }
        });

        springInside.setEndValue(0f).setAtRest();
    }

    public void initSize() {
        sizeAvatar = isGrid() ? (int) ((screenUtils.getWidthPx() >> 1) * 0.4f) : screenUtils.getWidthPx() / 8;
        sizeAvatarScaled = (int) (sizeAvatar * SCALE_FACTOR);
        diffSizeAvatar = sizeAvatarScaled - sizeAvatar;

        ViewGroup.LayoutParams params = avatar.getLayoutParams();
        params.width = sizeAvatar;
        params.height = sizeAvatar;
        avatar.setLayoutParams(params);

        if (type == TYPE_GRID_LIVE_CO) {
            params = layoutPulse.getLayoutParams();
            params.width = sizeAvatar + screenUtils.dpToPx(90);
            params.height = sizeAvatar + screenUtils.dpToPx(90);
            layoutPulse.setLayoutParams(params);
        } else if (viewShadowAvatar != null) {
            params = viewShadowAvatar.getLayoutParams();
            params.width = sizeAvatar + (isGrid() ? screenUtils.dpToPx(25) : screenUtils.dpToPx(15));
            params.height = sizeAvatar + (isGrid() ? screenUtils.dpToPx(25) : screenUtils.dpToPx(15));
            viewShadowAvatar.setLayoutParams(params);
        }

        if (type == TYPE_INVITE_LIVE_CO) {
            FrameLayout.LayoutParams imgIndParams = (FrameLayout.LayoutParams) imgInd.getLayoutParams();
            imgIndParams.leftMargin = sizeAvatar / 3;
            imgIndParams.topMargin = imgIndParams.leftMargin;
            imgIndParams.height = sizeAvatar / 2;
            imgIndParams.width = imgIndParams.height;
            int padding = screenUtils.dpToPx(1);
            imgInd.setPadding(padding, padding, padding, padding);
            imgInd.setLayoutParams(imgIndParams);
            imgInd.requestLayout();
        }

        avatar.invalidate();
        avatar.requestLayout();
    }

    public void initClicks() {
        prepareTouchesMore();
        prepareClickOnView();
    }

    private void prepareTouchesMore() {
        txtName.setOnClickListener(v -> clickMoreView.onNext(this));
    }

    private void prepareClickOnView() {
        setOnClickListener(v -> click.onNext(v));
    }

    private void reset() {
        springInside.setSpringConfig(SPRING_NO_BOUNCE);
        springInside.setEndValue(0f);
    }

    private boolean isGrid() {
        return type == TYPE_GRID_LIVE_CO || type == TYPE_NORMAL;
    }

    public void setInfo(Recipient recipient) {
        this.recipient = recipient;
        ((Avatar) avatar).load(recipient);

        if (recipient instanceof Membership) {
            txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small, 0, 0, 0);
        } else {
            txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        txtName.setText(recipient.getDisplayName());

        if (isGrid()) {
            if (recipient.isLive()) {
                ((AvatarLiveView) avatar).setType(AvatarLiveView.LIVE);
                txtStatus.setText(R.string.grid_status_live);
            } else if (recipient.isConnected()) {
                ((AvatarLiveView) avatar).setType(AvatarLiveView.CONNECTED);
                txtStatus.setText(R.string.grid_status_connected);
            } else {
                if (recipient.getLastOnline() != null) {
                    txtStatus.setText(
                            getContext().getString(
                                    R.string.grid_status_last_seen,
                                    DateUtils.getRelativeTimeSpanString(
                                            recipient.getLastOnline().getTime(),
                                            new Date().getTime(),
                                            DateUtils.MINUTE_IN_MILLIS
                                    ).toString().toLowerCase()
                            )
                    );
                }
            }
        } else {
            if (recipient.isLive()) {
                imgInd.setVisibility(View.VISIBLE);
                imgInd.setImageResource(R.drawable.picto_live);
            } else if (recipient.isConnected()) {
                imgInd.setVisibility(View.VISIBLE);
                imgInd.setImageResource(R.drawable.picto_online);
            } else {
                imgInd.setVisibility(View.GONE);
            }
        }
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setBackground(int position) {
        this.position = position;

        UIUtils.setBackgroundGrid(screenUtils, viewBG, position, isGrid());

        if (!isGrid()) {
            UIUtils.setBackgroundMultiple(screenUtils, viewBGTransparent1, position);
            UIUtils.setBackgroundMultiple(screenUtils, viewBGTransparent2, position);
            UIUtils.setBackgroundInd(imgInd, position);
        }
    }

    public int getBackgroundColor() {
        return PaletteGrid.get(position);
    }

    public void startDrag() {
        UIUtils.setBackgroundMultiple(screenUtils, viewBG, position);
        springInside.setEndValue(1);
    }

    public void endDrag() {
        UIUtils.setBackgroundGrid(screenUtils, viewBG, position, isGrid());
        springInside.setEndValue(0);
    }

    public void startDrop() {
        getDropAnimator(true).start();
        ViewCompat.setElevation(avatar, 10);
        ViewCompat.setElevation(viewBG, 10);
        ViewCompat.setElevation(viewBGTransparent1, 10);
        ViewCompat.setElevation(viewBGTransparent2, 10);
    }

    public void endDrop() {
        getDropAnimator(false).start();
    }

    private AnimatorSet getDropAnimator(boolean start) {
        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator animatorAlpha = ValueAnimator.ofFloat(start ? alphaTilesMin : alphaTilesMax, start ? alphaTilesMax : alphaTilesMin);
        animatorAlpha.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            viewBGTransparent1.setAlpha(value);
            viewBGTransparent2.setAlpha(value);
        });

        ValueAnimator animatorRotationBG1 = ValueAnimator.ofInt(start ? rotationBG1Min : rotationBG1Max, start ? rotationBG1Max : rotationBG1Min);
        animatorRotationBG1.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            viewBGTransparent1.setRotation(value);
        });

        ValueAnimator animatorRotationBG2 = ValueAnimator.ofInt(start ? rotationBG2Min : rotationBG2Max, start ? rotationBG2Max : rotationBG2Min);
        animatorRotationBG2.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            viewBGTransparent2.setRotation(value);
        });

        ValueAnimator animatorSizeBG = ValueAnimator.ofFloat(start ? 1 : SCALE_DOWN_BG_FACTOR_LOW, start ? SCALE_DOWN_BG_FACTOR_LOW : 1);
        animatorSizeBG.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            viewBGTransparent1.setScaleX(value);
            viewBGTransparent1.setScaleY(value);
            viewBGTransparent2.setScaleX(value);
            viewBGTransparent2.setScaleY(value);
        });

        ValueAnimator animatorSizeMasterBG = ValueAnimator.ofFloat(start ? 1 : SCALE_DOWN_MASTER_BG_FACTOR_LOW, start ? SCALE_DOWN_MASTER_BG_FACTOR_LOW : 1);
        animatorSizeMasterBG.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            viewBG.setScaleX(value);
            viewBG.setScaleY(value);
        });

        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(300);
        animatorSet.setStartDelay(0);
        animatorSet.playTogether(animatorAlpha, animatorRotationBG1, animatorRotationBG2, animatorSizeBG, animatorSizeMasterBG);

        return animatorSet;
    }

    public void onDrop(LiveRowView viewLiveRow) {
        UIUtils.setBackgroundGrid(screenUtils, viewBG, position, isGrid());
        setShouldSquare(false);

        AnimatorSet animatorFinal = new AnimatorSet();
        animatorFinal.setDuration(300);
        animatorFinal.setInterpolator(new DecelerateInterpolator());
        AnimatorSet animatorFirst = getDropAnimator(false);

        AnimatorSet animatorSecond = new AnimatorSet();
        animatorSecond.playTogether(
                AnimationUtils.getHeightAnimator(this, getHeight(), viewLiveRow.getHeight()),
                AnimationUtils.getWidthAnimator(this, getWidth(), viewLiveRow.getWidth()),
                AnimationUtils.getHeightAnimator(viewBG, viewBG.getHeight(), viewLiveRow.getHeight()),
                AnimationUtils.getWidthAnimator(viewBG, viewBG.getWidth(), viewLiveRow.getWidth()),
                AnimationUtils.getLeftMarginAnimator(this, viewLiveRow.getLeft()),
                AnimationUtils.getTopMarginAnimator(this, viewLiveRow.getTop()),
                AnimationUtils.getRotationAnimator(viewBG, 0),
                AnimationUtils.getRotationAnimator(avatar, 0),
                AnimationUtils.getRadiusAnimator(this, 0),
                AnimationUtils.getSizeAnimator(avatar, smallAvatarSize),
                AnimationUtils.getScaleAnimator(avatar, 1)
        );

        if (viewShadowAvatar != null) {
            animatorSecond.playTogether(
                    AnimationUtils.getScaleAnimator(viewShadowAvatar, 1.2f)
            );
        }

        animatorFinal.playTogether(animatorFirst, animatorSecond);
        animatorFinal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onEndDrop.onNext(null);
            }
        });
        animatorFinal.start();
    }

    public Observable<View> onClickMore() {
        return clickMoreView;
    }

    public Observable<View> onClick() {
        return click;
    }

    public Observable<Void> onEndDrop() {
        return onEndDrop;
    }

    private void initDependencyInjector() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
    }
}
