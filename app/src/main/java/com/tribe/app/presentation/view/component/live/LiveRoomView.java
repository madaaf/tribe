package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
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
import com.tribe.app.presentation.view.utils.PaletteGrid;

/**
 * Created by tiago on 22/01/2017.
 */

public class LiveRoomView extends FrameLayout {

  @IntDef({ GRID, LINEAR }) public @interface TribeRoomViewType {
  }

  public static final int GRID = 0;
  public static final int LINEAR = 1;

  private static int DURATION = 300;
  private static int onDroppedBarHeight = 150;
  private static final int DEFAULT_TYPE = GRID;

  // VARIABLES
  private Unbinder unbinder;
  private @TribeRoomViewType int type;

  @BindView(R.id.flexbox_layout) FlexboxLayout flexboxLayout;

  public LiveRoomView(Context context) {
    super(context);
    init();
  }

  public LiveRoomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {

    type = DEFAULT_TYPE;
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_flexbox, this);
    unbinder = ButterKnife.bind(this);

    flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_STRETCH);
    flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_STRETCH);
    flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
  }

  public void removeView(LiveRowView view) {
    flexboxLayout.removeView(view);
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

    resizeAnimation.setDuration(DURATION);
    lastViewAdded.startAnimation(resizeAnimation);
  }

  public void addView(LiveRowView liveRowView) {
    int viewIndex = flexboxLayout.getChildCount();
    addViewInRow(viewIndex, liveRowView);
    setRowsOrder();
    if (type == GRID) {
      organizeGridParam();
      setRowsGridWidth();
    }
  }

  public void setType(@TribeRoomViewType int type) {
    if (this.type == type) return;
    this.type = type;
    if (type == GRID) {
      setRowsGridWidth();
      organizeGridParam();
    } else {
      flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
    }

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

  public int getColor(int color) {
    if (color == Color.BLACK || color == 0) color = PaletteGrid.getRandomColorExcluding(color);
    return color;
  }

  private void addViewInRow(int viewIndex, LiveRowView liveRowView) {
    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
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
        lp.maxHeight = onDroppedBarHeight;
        liveRowView.setLayoutParams(lp);
        flexboxLayout.addView(liveRowView);
    }
  }

  private void organizeGridParam() {
    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
    switch (flexboxLayout.getChildCount()) {
      case 2:
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
        break;
    }
  }

  private void setRowsOrder() {
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

  private void setRowsGridWidth() {
    int peopleOnLine = flexboxLayout.getChildCount();
    if (peopleOnLine % 2 == 0) {
      for (int i = 0; i < peopleOnLine; i++) {
        setWidth(i, (flexboxLayout.getWidth() / 2));
      }
    } else {
      for (int i = 0; i < peopleOnLine; i++) {
        if (i == 0) {
          setWidth(i, (flexboxLayout.getWidth()));
        } else {
          setWidth(i, (flexboxLayout.getWidth() / 2));
        }
      }
    }
  }

  private void setWidth(int index, int width) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.width = width - (LiveInviteView.WIDTH / 2);
    l.flexGrow = 1;
    view.setLayoutParams(l);
  }

  public class ResizeAnimation extends Animation {
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
      int newHeight = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
      l.maxHeight = newHeight;
      view.requestLayout();
      view.setLayoutParams(l);
    }

    @Override public boolean willChangeBounds() {
      return true;
    }
  }
}
