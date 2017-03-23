package com.tribe.app.presentation.view.component.live;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
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
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;

public class LiveRoomView extends FrameLayout {

  @IntDef({ GRID, LINEAR }) public @interface TribeRoomViewType {
  }

  public static final int GRID = 0;
  public static final int LINEAR = 1;

  private static final int DURATION = 300;
  private static final int DEFAULT_TYPE = GRID;

  @Inject ScreenUtils screenUtils;

  // VARIABLES
  private Unbinder unbinder;
  private @TribeRoomViewType int type;
  private int onDroppedBarHeight = 0;
  private boolean landscapeMode = false;

  private int witdhScreen;
  private int heightScreen;

  @BindView(R.id.flexbox_layout) FlexboxLayout flexboxLayout;

  public LiveRoomView(Context context) {
    super(context);
    init();
  }

  public LiveRoomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private View view;

  private void init() {
    type = DEFAULT_TYPE;
    initDependencyInjector();
    onDroppedBarHeight = screenUtils.dpToPx(65);

    view = LayoutInflater.from(getContext()).inflate(R.layout.view_flexbox, this);
    unbinder = ButterKnife.bind(this);

    flexboxLayout = (FlexboxLayout) findViewById(R.id.flexbox_layout);
    LayoutTransition transition = new LayoutTransition();
    transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
    flexboxLayout.setLayoutTransition(transition);

    flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_STRETCH);
    flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_STRETCH);
    flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);

    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;
      setScreenSize();
    } else {
      landscapeMode = false;
      setScreenSize();
    }
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
    setConfigurationScreen();
  }

  public void addView(LiveRowView liveRowView, boolean guestDraguedByMy) {
    int viewIndex = flexboxLayout.getChildCount();
    addViewInContainer(viewIndex, liveRowView, guestDraguedByMy);
    setConfigurationScreen();
  }

  public void setType(@TribeRoomViewType int type) {
    if (this.type == type) return;
    this.type = type;

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child instanceof LiveRowView) {
        LiveRowView liveRowView = (LiveRowView) child;
        liveRowView.setRoomType(type);
      }
    }
  }

  public @TribeRoomViewType int getType() {
    return type;
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    flexboxLayout.invalidate();
    flexboxLayout.requestLayout();

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;
    } else {
      landscapeMode = false;
    }
    setScreenSize();
    setConfigurationScreen();
  }

  private void setConfigurationScreen() {
    setViewsOrder();
    if (!landscapeMode) {
      if (type == GRID) {
        setSizeGridViewsInPortaitMode();

        if (flexboxLayout.getChildCount() < 3) {
          flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
        } else {
          flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
        }
      } else {
        setSizeLinearViews();
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
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
  /////////////////
  //   PRIVATE    //
  /////////////////

  private void addViewInContainer(int viewIndex, LiveRowView liveRowView,
      boolean guestDraguedByMe) {
    flexboxLayout.setBackgroundColor(liveRowView.getColor());
    FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(1, 1);

    lp.flexGrow = 1;

    switch (viewIndex) {
      case 0:
      case 1:
        LiveLocalView viewLocalLive = (LiveLocalView) flexboxLayout.getChildAt(0);
        if (viewLocalLive.getParent() != null) {
          ((ViewGroup) viewLocalLive.getParent()).removeView(viewLocalLive);
        }
        viewLocalLive.setVisibility(VISIBLE);
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(1, 1);
        lp1.flexGrow = 1;
        viewLocalLive.setLayoutParams(lp1);
        flexboxLayout.addView(viewLocalLive);

        liveRowView.setLayoutParams(lp);
        flexboxLayout.addView(liveRowView);
        break;
      default:
        if (guestDraguedByMe) {
          lp.flexGrow = 1;
          lp.maxHeight = 0;
          flexboxLayout.addView(liveRowView);
          ResizeAnimation resizeAnimation =
              new ResizeAnimation(lp, liveRowView, onDroppedBarHeight, 0);
          resizeAnimation.setDuration(DURATION);
          resizeAnimation.setInterpolator(new OvershootInterpolator(0.4f));
          liveRowView.startAnimation(resizeAnimation);
          liveRowView.setLayoutParams(lp);
        } else {
          liveRowView.setLayoutParams(lp);
          flexboxLayout.addView(liveRowView);
        }
    }
  }

  @Override public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    setScreenSize();
    setConfigurationScreen();
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    setScreenSize();
    setConfigurationScreen();
  }

  private void setScreenSize() {
    this.witdhScreen = flexboxLayout.getWidth();
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
    l.width = width - (LiveInviteView.WIDTH / 2);
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
}
