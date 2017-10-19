package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.SquareFrameLayout;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class TileInviteView extends SquareFrameLayout {

  public static final float SCALE_MAX = 1.8f;
  public static final float SCALE_MIN = 1.4f;
  public static final float ROTATION = 10;

  private static final int DURATION = 450;
  private static final float BOUNCINESS_UP = 1f;
  private static final float SPEED_UP = 20f;
  private static final SpringConfig SPRING_NO_BOUNCE =
      SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_UP, SPEED_UP);

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewNewAvatar) NewAvatarView viewNewAvatar;
  @BindView(R.id.viewBG) View viewBG;
  @BindView(R.id.viewShadow) View viewShadow;

  // RESOURCES
  private int marginBG;
  private int marginAvatar;

  // VARIABLES
  private Unbinder unbinder;
  private User user;
  private int realWidth;
  private int position;

  // SPRINGS
  private SpringSystem springSystem = null;
  private Spring springTile;
  private SpringTileListener springTileListener;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<View> onClick = PublishSubject.create();

  public TileInviteView(Context context) {
    super(context);
    init(context, null);
  }

  public TileInviteView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public TileInviteView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int boundedWidth = screenUtils.dpToPx(LiveInviteView.WIDTH_PARTIAL);
    int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);

    if (boundedWidth > 0 && boundedWidth < measuredWidth) {
      int measureMode = MeasureSpec.getMode(widthMeasureSpec);
      widthMeasureSpec = MeasureSpec.makeMeasureSpec(boundedWidth, measureMode);
    }

    // Adjust height as necessary
    int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
    if (boundedWidth > 0 && boundedWidth < measuredHeight) {
      int measureMode = MeasureSpec.getMode(heightMeasureSpec);
      heightMeasureSpec = MeasureSpec.makeMeasureSpec(boundedWidth, measureMode);
    }

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  public void updateWidth(int width) {
    this.realWidth = width;
    int tempMarginBG = (int) (width * 0.15f);
    int tempMarginAvatar = (int) (width * 0.25f);
    updateGraphicConstraints(width, tempMarginBG, tempMarginAvatar);
  }

  private void updateGraphicConstraints(int width, int marginBG, int marginAvatar) {
    if (this.marginBG == marginBG || this.marginAvatar == marginAvatar) return;
    this.marginBG = marginBG;
    this.marginAvatar = marginAvatar;

    viewNewAvatar.updateWidth(width - marginAvatar);
    UIUtils.changeMarginOfView(viewBG, marginBG);
    UIUtils.changeMarginOfView(viewNewAvatar, marginAvatar);
    post(() -> requestLayout());
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_tile_invite, this);
    unbinder = ButterKnife.bind(this);

    initSprings();

    setBackground(null);
    setWillNotDraw(false);
  }

  private void initResources() {

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initSprings() {
    springSystem = SpringSystem.create();
    springTileListener = new SpringTileListener();
    springTile = springSystem.createSpring();
    springTile.setSpringConfig(SPRING_NO_BOUNCE);
    springTile.addListener(springTileListener);
    springTile.setCurrentValue(0f, true);
  }

  private class SpringTileListener extends SimpleSpringListener {
    @Override public void onSpringUpdate(Spring spring) {
      updateSpringValue(spring);
    }

    @Override public void onSpringAtRest(Spring spring) {
      updateSpringValue(spring);
    }

    @Override public void onSpringEndStateChange(Spring spring) {
      super.onSpringEndStateChange(spring);
    }

    private void updateSpringValue(Spring spring) {
      float value = (float) spring.getCurrentValue();

      float alpha = 1 - value;
      viewBG.setAlpha(alpha);

      float scale = 1f + value * 0.4f;

      viewNewAvatar.setScaleX(scale);
      viewNewAvatar.setScaleY(scale);
      viewShadow.setScaleX(scale);
      viewShadow.setScaleY(scale);

      int rotation = Math.max((int) (0 + (ROTATION * value)), 0);
      setRotation(rotation);
    }
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void initClicks() {
    viewNewAvatar.setClickable(true);
    viewNewAvatar.setOnClickListener(view -> {
      springTile.setEndValue(user.isSelected() ? 0 : 1);
      user.setSelected(!user.isSelected());
      onClick.onNext(viewNewAvatar);
    });
  }

  public int getRealWidth() {
    return realWidth;
  }

  public User getUser() {
    return user;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getPosition() {
    return position;
  }

  public void setUser(User user) {
    this.user = user;

    if (user.isSelected()) {
      springTile.setEndValue(1);
    } else {
      springTile.setEndValue(0);
    }

    if (!StringUtils.isEmpty(user.getCurrentRoomId())) {
      viewNewAvatar.setType(NewAvatarView.LIVE);
    } else if (user.isOnline()) {
      viewNewAvatar.setType(NewAvatarView.ONLINE);
    } else {
      viewNewAvatar.setType(NewAvatarView.NORMAL);
    }

    viewNewAvatar.load(user.getProfilePicture());
  }

  public void scaleAvatar(float scale) {
    viewNewAvatar.setScaleY(scale);
    viewNewAvatar.setScaleX(scale);
    viewShadow.setScaleX(scale);
    viewShadow.setScaleY(scale);
  }

  public void startDrag() {
    if (user.isSelected()) {
      springTile.setCurrentValue(1, true);
    } else {
      springTile.setEndValue(1);
    }
  }

  public void endDrag() {
    if (!user.isSelected()) springTile.setEndValue(0);
  }

  public int animateOnDrop(int toX, int toY) {
    int durationTotal = DURATION * 2 + (DURATION >> 1);

    animate().translationX(toX)
        .translationY(toY)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            animate().scaleX(0)
                .scaleY(0)
                .translationX(getTranslationX() + screenUtils.dpToPx(50))
                .setDuration(DURATION >> 1)
                .setStartDelay(DURATION >> 1)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null)
                .start();
          }
        })
        .start();

    animate().rotation(-360)
        .scaleX(0.8f)
        .scaleY(0.8f)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator(0.75f))
        .start();

    return durationTotal;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<View> onClick() {
    return onClick;
  }
}
