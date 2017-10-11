package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
public class TileInviteView extends SquareFrameLayout implements View.OnClickListener {

  private static final float BOUNCINESS_UP = 1f;
  private static final float SPEED_UP = 20f;
  private static final SpringConfig SPRING_NO_BOUNCE =
      SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_UP, SPEED_UP);

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewNewAvatar) NewAvatarView viewNewAvatar;
  @BindView(R.id.viewBG) View viewBG;

  // RESOURCES
  private int marginBG;
  private int marginAvatar;

  // VARIABLES
  private Unbinder unbinder;
  private User user;

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
    initSprings();

    LayoutInflater.from(getContext()).inflate(R.layout.view_tile_invite, this);
    unbinder = ButterKnife.bind(this);

    setBackground(null);
    setWillNotDraw(false);

    viewNewAvatar.setClickable(true);
    viewNewAvatar.setOnClickListener(this);
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
    springTile.setEndValue(0f).setAtRest();
  }

  @Override public void onClick(View view) {
    springTile.setEndValue(user.isSelected() ? 0 : 1);
    user.setSelected(!user.isSelected());
    onClick.onNext(this);
  }

  private class SpringTileListener extends SimpleSpringListener {
    @Override public void onSpringUpdate(Spring spring) {
      float value = (float) spring.getCurrentValue();

      float alpha = 1 - value;
      viewBG.setAlpha(alpha);

      float scale = 1f + value * 0.4f;

      viewNewAvatar.setScaleX(scale);
      viewNewAvatar.setScaleY(scale);

      int rotation = Math.max((int) (0 + (10 * value)), 0);
      setRotation(rotation);
    }
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void setUser(User user) {
    if (this.user != null && this.user.equals(user)) return;
    this.user = user;

    if (!StringUtils.isEmpty(user.getCurrentRoomId())) {
      viewNewAvatar.setType(NewAvatarView.LIVE);
    } else if (user.isOnline()) {
      viewNewAvatar.setType(NewAvatarView.ONLINE);
    } else {
      viewNewAvatar.setType(NewAvatarView.NORMAL);
    }

    viewNewAvatar.load(user.getProfilePicture());
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<View> onClick() {
    return onClick;
  }
}
