package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import timber.log.Timber;

import static com.google.android.flexbox.FlexboxLayout.ALIGN_CONTENT_STRETCH;

/**
 * Created by tiago on 22/01/2017.
 */

public class LiveRoomView extends FrameLayout {

  @IntDef({ GRID, LINEAR }) public @interface TribeRoomViewType {
  }

  public static final int GRID = 0;
  public static final int LINEAR = 1;
  private static final int DEFAULT_TYPE = GRID;

  // VARIABLES
  private Unbinder unbinder;
  private @TribeRoomViewType int type;

  @BindView(R.id.flexbox_layout) FlexboxLayout flexboxLayout;

  public LiveRoomView(Context context) {
    super(context);
    type = DEFAULT_TYPE;
    init();
  }

  public LiveRoomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    type = DEFAULT_TYPE;
    init();
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_flexbox, this);
    unbinder = ButterKnife.bind(this);

    flexboxLayout.setAlignContent(ALIGN_CONTENT_STRETCH);
    flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_STRETCH);
    flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
  }

  public void addView(LiveRowView liveRowView, ViewGroup.LayoutParams params) {
    int viewIndex = flexboxLayout.getChildCount();

    if (viewIndex < 8) {
      if (type == GRID) {
        addViewInRow(viewIndex, liveRowView);
        organizeGridParam();
      } else {
        addViewInRow(viewIndex, liveRowView);
      }
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

  public void setType(@TribeRoomViewType int type) {
    if (this.type == type) return;

    this.type = type;
    if (type == GRID) {
      Timber.e("ORGANISE CODE " + "ORGANISE CODE");
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

  private void addViewInRow(int viewIndex, LiveRowView liveRowView) {
    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
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
        liveRowView.setLayoutParams(lp);
        flexboxLayout.addView(liveRowView);
    }
  }

  private void organizeGridParam() {
    flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW);
    setRowsGridWidth();
    switch (flexboxLayout.getChildCount()) {
      case 0:
      case 1:
        break;
      case 2:
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
        setOrder(1, 1);  //B
        setOrder(0, 2);  //A

        break;
      case 3:
        setWidth(0, flexboxLayout.getWidth() / 2);
        setWidth(1, flexboxLayout.getWidth() / 2);
        setWidth(2, flexboxLayout.getWidth() / 2);

        setOrder(0, 3);  //A
        setOrder(1, 1);  //B
        setOrder(2, 2);  //C
        break;
      case 4:
        setOrder(0, 3);  //A
        setOrder(1, 1);  //B
        setOrder(2, 2);  //C
        setOrder(3, 4);  //D
        break;

      case 5:
        setOrder(0, 3);  //A
        setOrder(1, 1);  //B
        setOrder(2, 2);  //C
        setOrder(3, 4);  //D
        setOrder(4, 5);  //E

        break;
      case 6:
        setOrder(0, 3);  //A
        setOrder(1, 1);  //B
        setOrder(2, 2);  //C
        setOrder(3, 4);  //D
        setOrder(4, 5);  //E
        setOrder(5, 6);  //F

        break;
      case 7:
        setOrder(0, 3);  //A
        setOrder(1, 1);  //B
        setOrder(2, 2);  //C
        setOrder(3, 4);  //D
        setOrder(4, 5);  //E
        setOrder(5, 6);  //F
        setOrder(6, 7);  //G
        break;
      case 8:
        setOrder(0, 3);  //A
        setOrder(1, 1);  //B
        setOrder(2, 2);  //C
        setOrder(3, 4);  //D
        setOrder(4, 5);  //E
        setOrder(5, 6);  //F
        setOrder(6, 7);  //G
        setOrder(7, 8);  //H
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
      for (int i = 0; i < flexboxLayout.getChildCount(); i++) {
        setWidth(i, flexboxLayout.getWidth() / 2);
      }
    } else {
      for (int i = 0; i < peopleOnLine; i++) {
        setWidth(i, flexboxLayout.getWidth() / 2);
        if (i == (peopleOnLine - 1)) {
          setWidth(i, flexboxLayout.getWidth());
        }
      }
    }
  }

  private void setWidth(int index, int width) {
    View view = flexboxLayout.getChildAt(index);
    FlexboxLayout.LayoutParams l = (FlexboxLayout.LayoutParams) view.getLayoutParams();
    l.minWidth = width;
    view.setLayoutParams(l);
  }

/*

        flexboxLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));
        liveRowView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet));

 */
}
