package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.utils.ScreenUtils;
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
  private boolean landscapeMode = false;
  private int witdhScreen;
  private int heightScreen;
  private boolean isConfigurationChanged = false;
  private @LiveActivity.Source String source;
  private boolean isCallRouletteMode = false;

  @BindView(R.id.layoutConstraint) ConstraintLayout constraintLayout;

  //@BindView(R.id.diceLayoutRoomView) DiceView diceView;

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

    LayoutInflater.from(getContext()).inflate(R.layout.view_room, this);
    unbinder = ButterKnife.bind(this);

    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;
    } else {
      landscapeMode = false;
    }

    //setScreenSize(0);
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

  /**
   * ON ROLL THE DICE MESSAGE HAS RECEIVED. SOMEONE NEXT THE DICE
   * IF I ENTER NORMALY => THE DICE ENROLL & I WAITING TO SOME CALL ROULETTEUR
   * IF I AM IN CALL ROULETTE MODE => I AM NEXED, I LEAVE THE ROOM AND ENTER I ANOTHER ONE
   */
  public void onRollTheDiceReceived() {
    isCallRouletteMode = true;
    onShouldCloseInvites.onNext(null);
    //diceView.setVisibility(VISIBLE);
    //diceView.startDiceAnimation();
    if (source != null && source.equals(SOURCE_CALL_ROULETTE)) {
      onChangeCallRouletteRoom.onNext(null);
    }
  }

  public void setSource(@LiveActivity.Source String source) {
    this.source = source;
    //if (source.equals(SOURCE_CALL_ROULETTE)) {
    //  diceView.setVisibility(VISIBLE);
    //}
  }

  /////////////////
  //   PUBLIC    //
  /////////////////

  //public void removeView(LiveRowView view) {
  //  flexboxLayout.removeView(view);
  //  setViewsOrder();
  //  setConfigurationScreen();
  //
  //  if (source != null &&
  //      source.equals(SOURCE_CALL_ROULETTE) &&
  //      flexboxLayout.getChildCount() < 2) {
  //    diceView.setVisibility(VISIBLE);
  //    diceView.startDiceAnimation();
  //  }
  //}
  //
  //public int getRowsInLive() {
  //  return flexboxLayout.getChildCount();
  //}
  //
  //public void removeGuest(String userId) {
  //  LiveRowView liveRowView;
  //  for (int i = 0; i < flexboxLayout.getChildCount(); i++) {
  //    View view = flexboxLayout.getChildAt(i);
  //    if (view instanceof LiveRowView) {
  //      liveRowView = (LiveRowView) view;
  //      if (liveRowView.getGuest().getId().equals(userId)) {
  //        removeView(liveRowView);
  //      }
  //    }
  //  }
  //}
  //

  public void addView(LiveRowView liveRowView) {
    int viewCount = constraintLayout.getChildCount();

    if (viewCount == 1) {
      Guideline gl = new Guideline(getContext());
      ConstraintLayout.LayoutParams glp =
          new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      glp.guidePercent = 0.5f;
      glp.orientation = LinearLayout.VERTICAL;
      gl.setLayoutParams(glp);
      gl.setId(View.generateViewId());
      constraintLayout.addView(gl);

      LiveLocalView liveLocalView = (LiveLocalView) constraintLayout.getChildAt(0);
      ConstraintLayout.LayoutParams params =
          (ConstraintLayout.LayoutParams) liveLocalView.getLayoutParams();
      params.topToTop = gl.getId();
      liveLocalView.setLayoutParams(params);

      TransitionManager.beginDelayedTransition(constraintLayout);
    }
  }

  /////////////////
  //    INIT     //
  /////////////////

  //@Override public void onConfigurationChanged(Configuration newConfig) {
  //  super.onConfigurationChanged(newConfig);
  //  isConfigurationChanged = true;
  //  flexboxLayout.invalidate();
  //  flexboxLayout.requestLayout();
  //
  //  if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
  //    landscapeMode = true;
  //  } else {
  //    landscapeMode = false;
  //  }
  //
  //  setScreenSize(0);
  //  setViewsOrder();
  //  setConfigurationScreen();
  //}

  //@Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
  //  super.onLayout(changed, l, t, r, b);
  //  if (isConfigurationChanged) {
  //    setScreenSize(0);
  //    setConfigurationScreen();
  //    isConfigurationChanged = false;
  //  }
  //}

  /////////////////
  //   PRIVATE   //
  /////////////////

  private void setConfigurationScreen() {
    //if (!landscapeMode) {
    //  setSizeGridViewsInPortaitMode();
    //
    //  if (flexboxLayout.getChildCount() < 3) {
    //    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
    //  } else {
    //    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
    //  }
    //} else {
    //  setSizeGirdViewsInLandscapeMode();
    //
    //  if (flexboxLayout.getChildCount() >= 5) {
    //    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
    //  } else {
    //    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
    //  }
    //}
  }

  private void addViewInContainer(int viewIndex, LiveRowView liveRowView) {

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

  //private void setScreenSize(int openInviteWidth) {
  //this.witdhScreen = flexboxLayout.getWidth() + openInviteWidth;
  //this.heightScreen = flexboxLayout.getHeight();
  //}

  //private void setSizeGirdViewsInLandscapeMode() {
  //  int peopleOnLine = flexboxLayout.getChildCount();
  //  if (peopleOnLine < 5) {
  //    for (int i = 0; i < peopleOnLine; i++) {
  //      setWidth(i, witdhScreen / peopleOnLine);
  //      setHeight(i, heightScreen);
  //    }
  //  } else {
  //    for (int i = 0; i < peopleOnLine; i++) {
  //      if (peopleOnLine % 2 == 0) { // LOCAL VIEW
  //        setHeight(i, heightScreen / 2);
  //        setWidth(i, witdhScreen / ((peopleOnLine / 2) + 1));
  //      } else { // Impair
  //        setWidth(i, witdhScreen / ((peopleOnLine / 2) + 1));
  //        if (i == 0) {
  //          setHeight(i, heightScreen);
  //        } else {
  //          setHeight(i, heightScreen / 2);
  //        }
  //      }
  //    }
  //  }
  //}

  //private void setSizeGridViewsInPortaitMode() {
  //  int peopleOnLine = flexboxLayout.getChildCount();
  //  if (peopleOnLine % 2 == 0) {
  //    for (int i = 0; i < peopleOnLine; i++) {
  //      if (peopleOnLine > 2) {
  //        setWidth(i, (witdhScreen / 2));
  //        setHeight(i, (heightScreen / (peopleOnLine / 2)));
  //      } else {
  //        setWidth(i, (witdhScreen));
  //        setHeight(i, (heightScreen / peopleOnLine));
  //      }
  //    }
  //  } else { // IMPAIR
  //    for (int i = 0; i < peopleOnLine; i++) {
  //      setHeight(i, heightScreen / (peopleOnLine + 1));
  //      if (i == 0) {
  //        setWidth(i, (witdhScreen));
  //      } else {
  //        setWidth(i, (witdhScreen / 2));
  //      }
  //    }
  //  }
  //}
  //
  //private void setSizeLinearViews() {
  //  int peopleOnLine = flexboxLayout.getChildCount();
  //  for (int i = 0; i < peopleOnLine; i++) {
  //    setWidth(i, (witdhScreen));
  //    setHeight(i, (heightScreen / peopleOnLine));
  //  }
  //}
  //
  //private void setViewsOrder() {
  //  int peopleOnLine = flexboxLayout.getChildCount();
  //  for (int i = 0; i < peopleOnLine; i++) {
  //    if (i == 0) {
  //      setOrder(i, peopleOnLine);  // local view
  //    } else {
  //      setOrder(i, i);  // guest view
  //    }
  //  }
  //}
  //
  //private void setOrder(int index, int order) {
  //  View view = flexboxLayout.getChildAt(index);
  //  FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
  //  l.order = order;
  //  view.setLayoutParams(l);
  //}
  //
  //private void setHeight(int index, int height) {
  //  View view = flexboxLayout.getChildAt(index);
  //  FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
  //  l.height = height;
  //  l.flexGrow = 1;
  //  view.setLayoutParams(l);
  //}
  //
  //private void setWidth(int index, int width) {
  //  View view = flexboxLayout.getChildAt(index);
  //  FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
  //  l.width = width;
  //  l.flexGrow = 1;
  //  view.setLayoutParams(l);
  //}

  public Observable<Void> onShouldCloseInvites() {
    return onShouldCloseInvites;
  }

  public Observable<Void> onChangeCallRouletteRoom() {
    return onChangeCallRouletteRoom;
  }
}