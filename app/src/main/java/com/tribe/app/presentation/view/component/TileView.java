package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.SquareCardView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.Avatar;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 10/06/2016.
 */
public class TileView extends SquareCardView {

    private final float DIFF_DOWN = 20f;
    private final int LONG_PRESS = 100;
    private final float SCALE_FACTOR = 1.75f;
    private final int RADIUS_MIN = 0;
    private final int RADIUS_MAX = 5;
    private final int ELEVATION_MIN = 0;
    private final int ELEVATION_MAX = 5;

    public final static int TYPE_GRID_LIVE_CO = 0;
    public final static int TYPE_INVITE_LIVE_CO = 1;
    public final static int TYPE_NORMAL = 2;
    public final static int TYPE_INVITE = 3;

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
    @BindView(R.id.viewShadowLeft)
    View viewShadowLeft;

    // OBSERVABLES
    private CompositeSubscription subscriptions;
    private Subscription timer;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
    private final PublishSubject<View> click = PublishSubject.create();

    // RESOURCES
    private int diffDown, cardRadiusMin, cardRadiusMax, diffCardRadius,
            cardElevationMin, cardElevationMax, diffCardElevation;

    // VARIABLES
    private Unbinder unbinder;
    private Recipient recipient;
    private int type;
    private boolean isDown = false;
    private long longDown = 0L;
    private float downX, downY, currentX, currentY;
    private int sizeAvatar, sizeAvatarScaled, diffSizeAvatar;

    // SPRINGS
    private SpringSystem springSystem = null;
    private Spring springInside;

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
        type = a.getInt(R.styleable.TileView_tileType, TYPE_NORMAL);
        a.recycle();

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

        if (type == TYPE_GRID_LIVE_CO)
            layoutPulse.start();

        setCardElevation(0);
        ViewCompat.setElevation(this, 0);
        setUseCompatPadding(false);
        setPreventCornerOverlap(true);
        setRadius(0);

        if (isGrid()) {
            setBackground(null);
            setCardBackgroundColor(Color.TRANSPARENT);
        }

        initDependencyInjector();
        initResources();
        initSprings();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void initResources() {
        diffDown = screenUtils.dpToPx(DIFF_DOWN);
        cardRadiusMax = screenUtils.dpToPx(RADIUS_MAX);
        cardRadiusMin = screenUtils.dpToPx(RADIUS_MIN);
        diffCardRadius = cardRadiusMax - cardRadiusMin;
        cardElevationMax = screenUtils.dpToPx(ELEVATION_MAX);
        cardElevationMin = screenUtils.dpToPx(ELEVATION_MIN);
        diffCardElevation = cardElevationMax - cardElevationMin;
    }

    private void initSprings() {
        springSystem = SpringSystem.create();
        springInside = springSystem.createSpring();
        springInside.setSpringConfig(SPRING_BOUNCE);
        springInside.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                float alpha = 1 - value;
                txtName.setAlpha(alpha);

                if (viewShadowLeft != null) viewShadowLeft.setAlpha(alpha);

                int scaleUp = Math.max((int) (sizeAvatar + (diffSizeAvatar * value)), sizeAvatar);
                ViewGroup.LayoutParams paramsAvatar = avatar.getLayoutParams();

                paramsAvatar.height = scaleUp;
                paramsAvatar.width = scaleUp;
                avatar.setLayoutParams(paramsAvatar);

                float scale = 1f + (value * (((float) sizeAvatarScaled / sizeAvatar) - 1));

                if (viewShadowAvatar != null) {
                    viewShadowAvatar.setScaleX(scale);
                    viewShadowAvatar.setScaleY(scale);
                }

                int cardRadius = Math.max((int) (cardRadiusMin + (diffCardRadius * value)), cardRadiusMin);
                setRadius(cardRadius);

                int cardElevation = Math.max((int) (cardElevationMin + (diffCardElevation * value)), cardElevationMin);
                setCardElevation(cardElevation);
            }
        });

        springInside.setEndValue(0f);
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

        avatar.invalidate();
        avatar.requestLayout();
    }

    public void initClicks() {
        prepareTouchesMore();
        prepareClickOnView();

        if (!isGrid()) {
            prepareTouches();
        }
    }

    private void prepareTouchesMore() {
        txtName.setOnClickListener(v -> clickMoreView.onNext(this));
    }

    private void prepareClickOnView() {
        setOnClickListener(v -> click.onNext(v));
    }

    private void prepareTouches() {
        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isDown) return false;

                Timber.d("DOWN");

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
                                Timber.d("LONG PRESS");
                                springInside.setSpringConfig(SPRING_BOUNCE);
                                springInside.setEndValue(1f);
                            }
                        });
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (isDown) {
                    currentX = event.getRawX();
                    currentY = event.getRawY();
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (isDown) {
                    if (timer != null) timer.unsubscribe();
                }

                springInside.setSpringConfig(SPRING_NO_BOUNCE);
                springInside.setEndValue(0f);

                isDown = false;
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (isDown) {
                    if (timer != null) timer.unsubscribe();
                }

                springInside.setSpringConfig(SPRING_NO_BOUNCE);
                springInside.setEndValue(0f);

                isDown = false;
            }

            return false;
        });
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

        if (recipient.isLive()) {
            ((AvatarLiveView) avatar).setType(AvatarLiveView.LIVE);
            if (isGrid()) txtStatus.setText(R.string.grid_status_live);
        } else if (recipient.isConnected()) {
            ((AvatarLiveView) avatar).setType(AvatarLiveView.CONNECTED);
            if (isGrid()) txtStatus.setText(R.string.grid_status_connected);
        } else if (isGrid()) {
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
    }

    private boolean isGrid() {
        return type == TYPE_GRID_LIVE_CO || type == TYPE_NORMAL;
    }

    public void setBackground(int position) {
        if (isGrid())
            UIUtils.setBackgroundGrid(screenUtils, viewBG, position, true);
        else
            UIUtils.setBackgroundCard(this, position);
    }

    public Observable<View> onClickMore() {
        return clickMoreView;
    }

    public Observable<View> onClick() { return click; }

    private void initDependencyInjector() {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
    }
}
