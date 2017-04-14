package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveRowView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.SquareCardView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.Date;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class TileView extends SquareCardView {

  public final static float RATIO_AVATAR_TILE = 0.5f;
  public final static int TYPE_GRID_LIVE = 0;
  public final static int TYPE_GRID_CONNECTED = 1;
  public final static int TYPE_INVITE_LIVE_CO = 2;
  public final static int TYPE_NORMAL = 3;
  public final static int TYPE_INVITE = 4;

  private final int MINUTES_LIMIT = 120 * 60 * 1000;
  private final int HOURS_LIMIT = 48 * 24 * 60 * 1000;
  private final float SCALE_FACTOR = 1.75f;
  private final float SCALE_TILE_FACTOR = 1.05f;
  private final float SCALE_DOWN_MASTER_BG_FACTOR_LOW = 0.85f;
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
  private static final SpringConfig SPRING_BOUNCE =
      SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_DOWN, SPEED_DOWN);
  private static final SpringConfig SPRING_NO_BOUNCE =
      SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_UP, SPEED_UP);

  @Inject ScreenUtils screenUtils;

  @Inject PaletteGrid paletteGrid;

  @Nullable @BindView(R.id.txtName) public TextViewFont txtName;

  @Nullable @BindView(R.id.txtWithGuests) TextViewFont txtWithGuests;

  @Nullable @BindView(R.id.layoutName) public ViewGroup layoutName;

  @BindView(R.id.avatar) public AvatarView avatar;

  @Nullable @BindView(R.id.layoutPulse) public PulseLayout layoutPulse;

  @Nullable @BindView(R.id.layoutStatus) public ViewGroup layoutStatus;

  @Nullable @BindView(R.id.txtStatus) public TextViewFont txtStatus;

  @BindView(R.id.viewBG) View viewBG;

  @Nullable @BindView(R.id.imgIndInvite) ImageView imgIndInvite;

  // OBSERVABLES
  private CompositeSubscription subscriptions;

  // RX SUBSCRIPTIONS / SUBJECTS
  private final PublishSubject<View> clickMoreView = PublishSubject.create();
  private final PublishSubject<View> click = PublishSubject.create();
  private final PublishSubject<Void> onEndDrop = PublishSubject.create();
  private final PublishSubject<View> longClick = PublishSubject.create();

  // RESOURCES
  private int cardRadiusMin, cardRadiusMax, diffCardRadius, cardElevationMin, cardElevationMax,
      diffCardElevation, rotationMin, rotationMax, diffRotation, minSize, maxSize, sizeDiff,
      rotationBG1Min, rotationBG1Max, diffRotationBG1, rotationBG2Min, rotationBG2Max,
      diffRotationBG2, sizeAvatarBig;
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
      case TYPE_GRID_LIVE:
        resLayout = R.layout.view_tile_grid_live;
        break;

      case TYPE_GRID_CONNECTED:
        resLayout = R.layout.view_tile_grid_connected;
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

    if (!isDragging) {
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
        setMaxCardElevation(0);
        setUseCompatPadding(true);
        setPreventCornerOverlap(false);
      } else {
        setUseCompatPadding(false);
        setPreventCornerOverlap(true);
      }

      if (type == TYPE_GRID_LIVE) layoutPulse.start();
    }

    if (!isGrid()) {
      minSize = screenUtils.dpToPx(LiveInviteView.WIDTH);
      maxSize = (int) (minSize * SCALE_TILE_FACTOR);
      sizeDiff = maxSize - minSize;
    } else {
      setBackground(null);
      setCardBackgroundColor(Color.TRANSPARENT);
    }
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (!isGrid() && getWidth() == 0 && getParent() != null) {
      UIUtils.changeSizeOfView(this, ((View) getParent()).getWidth());
    }
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (isGrid() && (getHeight() != 0 && viewBG.getHeight() != getHeight()) || (getWidth() != 0
        && viewBG.getWidth() != getWidth())) {
      UIUtils.changeWidthHeightOfView(viewBG, getWidth(), getHeight());
    }
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
    sizeAvatarBig = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_big);
  }

  private void initSprings() {
    springSystem = SpringSystem.create();
    springInside = springSystem.createSpring();
    springInside.setSpringConfig(SPRING_NO_BOUNCE);
    springInside.addListener(new SimpleSpringListener() {

      @Override public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();

        if (Math.abs(value - spring.getEndValue()) < 0.05) value = (float) spring.getEndValue();

        float alpha = 1 - value;
        if (layoutName != null) {
          layoutName.setAlpha(alpha);
        } else {
          txtName.setAlpha(alpha);
        }

        if (imgIndInvite != null) {
          imgIndInvite.setAlpha((float) SpringUtil.mapValueFromRangeToRange(alpha, 1, 0, 1,
              -10)); // Should disappear faster ^^
        }

        float scale = 1f + (value * (((float) sizeAvatarScaled / sizeAvatar) - 1));

        avatar.setScaleX(scale);
        avatar.setScaleY(scale);

        int cardRadius = Math.max((int) (cardRadiusMin + (diffCardRadius * value)), cardRadiusMin);
        setRadius(cardRadius);

        int cardElevation =
            Math.max((int) (cardElevationMin + (diffCardElevation * value)), cardElevationMin);
        setCardElevation(cardElevation);

        int rotation = Math.max((int) (rotationMin + (diffRotation * value)), rotationMin);
        setRotation(rotation);

        int sizeOfTile = Math.max((int) (minSize + (sizeDiff * value)), minSize);
        UIUtils.changeSizeOfView(TileView.this, sizeOfTile);
      }
    });

    springInside.setEndValue(0f).setAtRest();
  }

  public void initSize() {
    int screenSize =
        (screenUtils.getWidthPx() < screenUtils.getHeightPx()) ? screenUtils.getWidthPx()
            : screenUtils.getHeightPx();
    if (getResources().getBoolean(R.bool.isTablet)) {
      screenSize = screenUtils.getWidthPx();
    }

    sizeAvatar = isGrid() ? (int) ((screenSize / getResources().getInteger(R.integer.columnNumber))
        * RATIO_AVATAR_TILE) : (int) (screenUtils.dpToPx(LiveInviteView.WIDTH) * RATIO_AVATAR_TILE);
    sizeAvatarScaled = (int) (sizeAvatar * SCALE_FACTOR);
    diffSizeAvatar = sizeAvatarScaled - sizeAvatar;

    avatar.changeSize(sizeAvatar, true);

    if (isGrid()) {
      int sizeTile = screenSize / getResources().getInteger(R.integer.columnNumber);
      int sizeLayoutName =
          (int) ((sizeTile - (sizeAvatar - (int) (sizeAvatar * avatar.getShadowRatio())))
              * RATIO_AVATAR_TILE);
      int sizeStatus = sizeLayoutName;
      UIUtils.changeHeightOfView(layoutName, sizeLayoutName);
      UIUtils.changeHeightOfView(layoutStatus, sizeStatus);
    }

    ViewGroup.LayoutParams params;

    if (type == TYPE_GRID_LIVE) {
      params = layoutPulse.getLayoutParams();
      params.width = sizeAvatar + screenUtils.dpToPx(90);
      params.height = sizeAvatar + screenUtils.dpToPx(90);
      layoutPulse.setLayoutParams(params);
    }

    if (type == TYPE_INVITE_LIVE_CO) {
      FrameLayout.LayoutParams imgIndParams =
          (FrameLayout.LayoutParams) imgIndInvite.getLayoutParams();
      imgIndParams.leftMargin = sizeAvatar / 4;
      imgIndParams.topMargin = imgIndParams.leftMargin;
      imgIndParams.height = sizeAvatar / 3;
      imgIndParams.width = imgIndParams.height;
      imgIndInvite.setLayoutParams(imgIndParams);
    }
  }

  public void initClicks() {
    prepareTouchesMore();
    prepareClickOnView();
  }

  private void prepareTouchesMore() {
    if (layoutName != null) layoutName.setOnClickListener(v -> clickMoreView.onNext(this));
  }

  private void prepareClickOnView() {
    setOnLongClickListener(v -> {
      longClick.onNext(v);
      return true;
    });

    avatar.setOnClickListener(v -> {
      avatar.animate()
          .scaleX(1.05f)
          .scaleY(1.05f)
          .setInterpolator(new OvershootInterpolator(0.45f))
          .setDuration(200)
          .setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
              avatar.animate()
                  .scaleX(1f)
                  .scaleY(1f)
                  .setInterpolator(new DecelerateInterpolator())
                  .setDuration(200)
                  .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                      click.onNext(TileView.this);
                      avatar.animate().setListener(null).start();
                    }
                  })
                  .start();
            }
          })
          .start();
    });
  }

  private void reset() {
    springInside.setSpringConfig(SPRING_NO_BOUNCE);
    springInside.setEndValue(0f);
  }

  private boolean isGrid() {
    return type == TYPE_GRID_LIVE || type == TYPE_GRID_CONNECTED || type == TYPE_NORMAL;
  }

  public void setInfo(Recipient recipient) {
    setRecipient(recipient);
    setAvatar();
    setName();

    if (isGrid()) {
      setStatus();
    } else {
      if (recipient.isLive()) {
        imgIndInvite.setVisibility(View.VISIBLE);
        imgIndInvite.setImageResource(R.drawable.picto_live);
      } else if (recipient.isOnline()) {
        imgIndInvite.setVisibility(View.VISIBLE);
        imgIndInvite.setImageResource(R.drawable.picto_online);
      } else {
        imgIndInvite.setVisibility(View.GONE);
      }
    }
  }

  public void setRecipient(Recipient recipient) {
    this.recipient = recipient;
  }

  public void setAvatar() {
    avatar.setType(isGrid() ? AvatarView.PHONE : (recipient.isLive() ? AvatarView.LIVE
        : (recipient.isOnline() ? AvatarView.ONLINE : AvatarView.REGULAR)));
    avatar.load(recipient);
  }

  public void setName() {
    if (recipient instanceof Membership) {
      txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small, 0, 0, 0);
    } else {
      txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
      if (txtWithGuests != null) {
        txtWithGuests.setVisibility(recipient instanceof Invite ? View.VISIBLE : View.GONE);
      }
    }

    txtName.setText(recipient.getDisplayName());
  }

  public void setStatus() {
    if (!recipient.isLive() && !recipient.isOnline() && recipient.getLastSeenAt() != null) {
      long time = new Date().getTime() - recipient.getLastSeenAt().getTime();
      String txt = "";

      if (time < MINUTES_LIMIT) {
        txt = getContext().getResources().getString(R.string.grid_status_last_seen_minutes);
      } else if (time < HOURS_LIMIT) {
        txt = getContext().getResources().getString(R.string.grid_status_last_seen_hours);
      } else {
        txt = getContext().getResources().getString(R.string.grid_status_last_seen_days);
      }

      txtStatus.setText(txt);
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

    if (!isGrid()) {
      UIUtils.setBackgroundInd(imgIndInvite, position);
      UIUtils.setBackgroundCard(this, position);
    } else {
      UIUtils.setBackgroundGrid(screenUtils, viewBG, position, isGrid());
    }
  }

  public int getBackgroundColor() {
    return paletteGrid.get(position - 1);
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
    startAnimation(
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.jiggle));
  }

  public void endDrop() {
    clearAnimation();
    getDropAnimator(false).start();
  }

  private AnimatorSet getDropAnimator(boolean start) {
    AnimatorSet animatorSet = new AnimatorSet();

    ValueAnimator animatorSizeMasterBG =
        ValueAnimator.ofFloat(start ? 1 : SCALE_DOWN_MASTER_BG_FACTOR_LOW,
            start ? SCALE_DOWN_MASTER_BG_FACTOR_LOW : 1);
    animatorSizeMasterBG.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      setScaleX(value);
      setScaleY(value);
    });

    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.setDuration(300);
    animatorSet.setStartDelay(0);
    animatorSet.playTogether(animatorSizeMasterBG);

    return animatorSet;
  }

  public void onDrop(LiveRowView viewLiveRow) {
    clearAnimation();
    UIUtils.setBackgroundGrid(screenUtils, viewBG, position, isGrid());
    setShouldSquare(false);

    AnimatorSet animatorFinal = new AnimatorSet();
    animatorFinal.setDuration(300);
    animatorFinal.setInterpolator(new DecelerateInterpolator());
    AnimatorSet animatorFirst = getDropAnimator(false);

    Animator animatorBG = AnimationUtils.getColorAnimator(this, getBackgroundColor(),
        ColorUtils.setAlphaComponent(getBackgroundColor(), 0));
    animatorBG.setDuration(60);

    AnimatorSet animatorSecond = new AnimatorSet();
    animatorSecond.playTogether(
        AnimationUtils.getHeightAnimator(this, getHeight(), viewLiveRow.getHeight()),
        AnimationUtils.getWidthAnimator(this, getWidth(), viewLiveRow.getWidth()),
        AnimationUtils.getLeftMarginAnimator(this, viewLiveRow.getLeft()),
        AnimationUtils.getTopMarginAnimator(this, viewLiveRow.getTop()),
        AnimationUtils.getRotationAnimator(this, 0),
        AnimationUtils.getSizeAnimator(avatar, sizeAvatarBig),
        AnimationUtils.getScaleAnimator(avatar, 1), AnimationUtils.getElevationAnimator(this, 0),
        animatorBG);

    animatorFinal.playTogether(animatorFirst, animatorSecond);
    animatorFinal.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        onEndDrop.onNext(null);
      }

      @Override public void onAnimationCancel(Animator animation) {
        if (animatorFinal != null) animatorFinal.removeAllListeners();
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

  public Observable<View> onLongClick() {
    return longClick;
  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }
}
