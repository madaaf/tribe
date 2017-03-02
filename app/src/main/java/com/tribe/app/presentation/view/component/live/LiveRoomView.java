package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
  private @TribeRoomViewType int type;

  public LiveRoomView(Context context) {
    super(context);
    type = DEFAULT_TYPE;
  }

  public LiveRoomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    type = DEFAULT_TYPE;
  }

  public void addView(View liveRowView, ViewGroup.LayoutParams params) {
    super.addView(liveRowView, params);
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

    //requestLayout();
  }

  public @TribeRoomViewType int getType() {
    return type;
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (getChildCount() > 0) {
      if (type == GRID) {
        //layoutChildrenGrid();
      } else {
        //layoutChildrenLinear();
      }
    }
  }
}
