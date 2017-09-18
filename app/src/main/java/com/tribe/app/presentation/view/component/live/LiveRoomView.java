package com.tribe.app.presentation.view.component.live;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.android.flexbox.FlexboxLayout;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.DiceView;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.tribe.app.presentation.view.activity.LiveActivity.SOURCE_CALL_ROULETTE;

public class LiveRoomView extends FrameLayout {

  public static final int CORNER_RADIUS = 5;

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @Inject Navigator navigator;

  // VARIABLES
  private Unbinder unbinder;
  private int onDroppedBarHeight = 0;
  private boolean landscapeMode = false;
  private int witdhScreen;
  private int heightScreen;
  private boolean isConfigurationChanged = false;
  private int heightOndropBar;
  private @LiveActivity.Source String source;
  private boolean isCallRouletteMode = false;

  @BindView(R.id.flexbox_layout) FlexboxLayout flexboxLayout;

  @BindView(R.id.cardview) CardView cardView;

  @BindView(R.id.diceLayoutRoomView) DiceView diceView;

  private PublishSubject<Void> onShouldCloseInvites = PublishSubject.create();
  private PublishSubject<Void> onChangeCallRouletteRoom = PublishSubject.create();

  public LiveRoomView(Context context) {
    super(context);
    init();
  }

  public LiveRoomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    initDependencyInjector();
    heightOndropBar = (int) getResources().getDimension(R.dimen.live_room_view_dropped_bar_height);
    onDroppedBarHeight = screenUtils.dpToPx(heightOndropBar);

    LayoutInflater.from(getContext()).inflate(R.layout.view_flexbox, this);
    unbinder = ButterKnife.bind(this);

    LayoutTransition transition = new LayoutTransition();
    transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
    flexboxLayout.setLayoutTransition(transition);

    flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_STRETCH);
    flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_STRETCH);
    flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);

    // retro-compatibiliy with lollipop
    cardView.setPreventCornerOverlap(false);
    cardView.setMaxCardElevation(0);
    cardView.setRadius(screenUtils.dpToPx(CORNER_RADIUS));

    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;
    } else {
      landscapeMode = false;
    }
    setScreenSize(0);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  public void onDropItem(TileView tileView) {
    // drag and drop dice item view
    LiveRowView lastViewAdded =
        (LiveRowView) flexboxLayout.getChildAt(flexboxLayout.getChildCount() - 1);
    if (lastViewAdded.getGuest().getId().equals(Recipient.ID_CALL_ROULETTE)) {
      isCallRouletteMode = true;
      removeView(lastViewAdded);
      new Handler().postDelayed(() -> {
        onShouldCloseInvites.onNext(null);
        diceView.setVisibility(VISIBLE);
        diceView.startDiceAnimation();
      }, 800);
    }
  }

  /**
   * ON ROLL THE DICE MESSAGE HAS RECEIVED. SOMEONE NEXT THE DICE
   * IF I ENTER NORMALY => THE DICE ENROLL & I WAITING TO SOME CALL ROULETTEUR
   * IF I AM IN CALL ROULETTE MODE => I AM NEXED, I LEAVE THE ROOM AND ENTER I ANOTHER ONE
   */
  public void onRollTheDiceReceived() {
    isCallRouletteMode = true;
    onShouldCloseInvites.onNext(null);
    diceView.setVisibility(VISIBLE);
    diceView.startDiceAnimation();
    if (source != null && source.equals(SOURCE_CALL_ROULETTE)) {
      onChangeCallRouletteRoom.onNext(null);
    }
  }

  public void setSource(@LiveActivity.Source String source) {
    this.source = source;
    if (source.equals(SOURCE_CALL_ROULETTE)) {
      diceView.setVisibility(VISIBLE);
    }
  }

  public void onDropEnabled(Boolean enabled) {
    LiveRowView lastViewAdded =
        (LiveRowView) flexboxLayout.getChildAt(flexboxLayout.getChildCount() - 1);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) lastViewAdded.getLayoutParams();
    l.flexGrow = 1;
    ResizeAnimation resizeAnimation = null;

    if (enabled) {
      resizeAnimation =
          new ResizeAnimation(l, lastViewAdded, flexboxLayout.getHeight(), onDroppedBarHeight);
    } else {
      resizeAnimation =
          new ResizeAnimation(l, lastViewAdded, onDroppedBarHeight, flexboxLayout.getHeight());
    }
    resizeAnimation.setDuration(DURATION * 2);
    resizeAnimation.setInterpolator(new OvershootInterpolator(0.4f));
    lastViewAdded.startAnimation(resizeAnimation);
  }

  /////////////////
  //   PUBLIC    //
  /////////////////

  public void removeView(LiveRowView view) {
    flexboxLayout.removeView(view);
    setViewsOrder();
    setConfigurationScreen();

    if (source != null &&
        source.equals(SOURCE_CALL_ROULETTE) &&
        flexboxLayout.getChildCount() < 2) {
      diceView.setVisibility(VISIBLE);
      diceView.startDiceAnimation();
    }
  }

  public int getRowsInLive() {
    return flexboxLayout.getChildCount();
  }

  public void removeGuest(String userId) {
    LiveRowView liveRowView;
    for (int i = 0; i < flexboxLayout.getChildCount(); i++) {
      View view = flexboxLayout.getChildAt(i);
      if (view instanceof LiveRowView) {
        liveRowView = (LiveRowView) view;
        if (liveRowView.getGuest().getId().equals(userId)) {
          removeView(liveRowView);
        }
      }
    }
  }

  public void addView(LiveRowView liveRowView) {
    int viewIndex = flexboxLayout.getChildCount();
    if ((source != null && source.equals(SOURCE_CALL_ROULETTE)) || isCallRouletteMode) {
      diceView.setNextAnimation();
    }
    setScreenSize(0);
    addViewInContainer(viewIndex, liveRowView);
    setViewsOrder();
    setConfigurationScreen();
  }

  /////////////////
  //    INIT     //
  /////////////////

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    isConfigurationChanged = true;
    flexboxLayout.invalidate();
    flexboxLayout.requestLayout();

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;
    } else {
      landscapeMode = false;
    }

    setScreenSize(0);
    setViewsOrder();
    setConfigurationScreen();
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (isConfigurationChanged) {
      setScreenSize(0);
      setConfigurationScreen();
      isConfigurationChanged = false;
    }
  }

  /////////////////
  //   PRIVATE   //
  /////////////////

  private void setConfigurationScreen() {
    if (!landscapeMode) {
      setSizeGridViewsInPortaitMode();

      if (flexboxLayout.getChildCount() < 3) {
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
      } else {
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
      }
    } else {
      setSizeGirdViewsInLandscapeMode();

      if (flexboxLayout.getChildCount() >= 5) {
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
      } else {
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
      }
    }
  }

  private void addViewInContainer(int viewIndex, LiveRowView liveRowView) {

    FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(1, 1);
    lp.flexGrow = 1;

    switch (viewIndex) {
      case 0:
        LiveLocalView viewLocalLive = (LiveLocalView) flexboxLayout.getChildAt(0);
        if (viewLocalLive.getParent() != null) {
          ((ViewGroup) viewLocalLive.getParent()).removeView(viewLocalLive);
        }
        viewLocalLive.setVisibility(VISIBLE);
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(1, 1);
        lp1.flexGrow = 1;
        viewLocalLive.setLayoutParams(lp1);
        flexboxLayout.addView(viewLocalLive);
        break;
      default:
        liveRowView.setLayoutParams(lp);
        flexboxLayout.addView(liveRowView);
    }

    setAvatarPicto(liveRowView, viewIndex);
  }

  private void setAvatarPicto(LiveRowView liveRowView, int index) {
    switch (index) {
      case 0:
        setAvatar(liveRowView, Gravity.CENTER, index);
      case 1:
        setAvatar(liveRowView, Gravity.START | Gravity.BOTTOM, index);
        break;
      case 2:
        setAvatar(liveRowView, Gravity.END | Gravity.BOTTOM, index);
        break;
      case 3:
        setAvatar(liveRowView, Gravity.START | Gravity.TOP, index);
        break;
      case 4:
        setAvatar(liveRowView, Gravity.END | Gravity.TOP, index);
        break;
      case 5:
        setAvatar(liveRowView, Gravity.START | Gravity.BOTTOM, index);
        break;
      case 6:
        setAvatar(liveRowView, Gravity.END | Gravity.BOTTOM, index);
        break;
      case 7:
        setAvatar(liveRowView, Gravity.START | Gravity.TOP, index);
        break;
      case 8:
        setAvatar(liveRowView, Gravity.CENTER, index);
        break;
    }
  }

  private void setAvatar(LiveRowView liveRowView, int gravity, int index) {
    AvatarView avatarView = new AvatarView(getContext());
    //avatarView.setBackgroundResource(R.drawable.shape_circle_white);
    int padding = screenUtils.dpToPx(10);
    avatarView.setPadding(padding, padding, padding, padding);
    avatarView.load(liveRowView.getGuest().getPicture());
    ViewCompat.setElevation(avatarView, 10);

 /*   TextView avatarView = new TextView(getContext());
    avatarView.setBackgroundResource(R.drawable.shape_circle_white);
    avatarView.setText(index + " ");*/

    FrameLayout.LayoutParams layoutParams =
        new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    layoutParams.gravity = gravity;
    layoutParams.width = screenUtils.dpToPx(60);
    layoutParams.height = screenUtils.dpToPx(60);
    avatarView.setLayoutParams(layoutParams);
    liveRowView.addView(avatarView);
  }

  private void setScreenSize(int openInviteWidth) {
    this.witdhScreen = flexboxLayout.getWidth() + openInviteWidth;
    this.heightScreen = flexboxLayout.getHeight();
  }

  private void setSizeGirdViewsInLandscapeMode() {
    int peopleOnLine = flexboxLayout.getChildCount();
    if (peopleOnLine < 5) {
      for (int i = 0; i < peopleOnLine; i++) {
        setWidth(i, witdhScreen / peopleOnLine);
        setHeight(i, heightScreen);
      }
    } else {
      for (int i = 0; i < peopleOnLine; i++) {
        if (peopleOnLine % 2 == 0) { // LOCAL VIEW
          setHeight(i, heightScreen / 2);
          setWidth(i, witdhScreen / ((peopleOnLine / 2) + 1));
        } else { // Impair
          setWidth(i, witdhScreen / ((peopleOnLine / 2) + 1));
          if (i == 0) {
            setHeight(i, heightScreen);
          } else {
            setHeight(i, heightScreen / 2);
          }
        }
      }
    }
  }

  private void setSizeGridViewsInPortaitMode() {
    int peopleOnLine = flexboxLayout.getChildCount();
    if (peopleOnLine % 2 == 0) {
      for (int i = 0; i < peopleOnLine; i++) {
        if (peopleOnLine > 2) {
          setWidth(i, (witdhScreen / 2));
          setHeight(i, (heightScreen / (peopleOnLine / 2)));
        } else {
          setWidth(i, (witdhScreen));
          setHeight(i, (heightScreen / peopleOnLine));
        }
      }
    } else { // IMPAIR
      for (int i = 0; i < peopleOnLine; i++) {
        setHeight(i, heightScreen / (peopleOnLine + 1));
        if (i == 0) {
          setWidth(i, (witdhScreen));
        } else {
          setWidth(i, (witdhScreen / 2));
        }
      }
    }
  }

  private void setSizeLinearViews() {
    int peopleOnLine = flexboxLayout.getChildCount();
    for (int i = 0; i < peopleOnLine; i++) {
      setWidth(i, (witdhScreen));
      setHeight(i, (heightScreen / peopleOnLine));
    }
  }

  private void setViewsOrder() {
    int peopleOnLine = flexboxLayout.getChildCount();
    for (int i = 0; i < peopleOnLine; i++) {
      if (i == 0) {
        setOrder(i, peopleOnLine);  // local view
      } else {
        setOrder(i, i);  // guest view
      }
    }
  }

  private void setOrder(int index, int order) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.order = order;
    view.setLayoutParams(l);
  }

  private void setHeight(int index, int height) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.height = height;
    l.flexGrow = 1;
    view.setLayoutParams(l);
  }

  private void setWidth(int index, int width) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.width = width;
    l.flexGrow = 1;
    view.setLayoutParams(l);
  }

  private class ResizeAnimation extends Animation {
    final int targetHeight;
    View view;
    int startHeight;
    FlexboxLayout.LayoutParams l;

    public ResizeAnimation(FlexboxLayout.LayoutParams l, View view, int targetHeight,
        int startHeight) {
      this.view = view;
      this.targetHeight = targetHeight;
      this.startHeight = startHeight;
      this.l = l;
    }

    @Override protected void applyTransformation(float interpolatedTime, Transformation t) {
      l.maxHeight = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
      view.requestLayout();
      view.setLayoutParams(l);
    }

    @Override public boolean willChangeBounds() {
      return true;
    }
  }

  public Observable<Void> onShouldCloseInvites() {
    return onShouldCloseInvites;
  }

  public Observable<Void> onChangeCallRouletteRoom() {
    return onChangeCallRouletteRoom;
  }
}