package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;

/**
 * Layout manager to position items inside a {@link android.support.v7.widget.RecyclerView}.
 */
public class HomeLayoutManager extends GridLayoutManager {

  private boolean isScrollEnabled = true;

  public HomeLayoutManager(Context context, int columnNumber) {
    super(context, columnNumber);
    setItemPrefetchEnabled(true);
    setInitialPrefetchItemCount(columnNumber ^ 4);
  }

  public void setScrollEnabled(boolean enabled) {
    this.isScrollEnabled = enabled;
  }

  @Override public boolean canScrollVertically() {
    return isScrollEnabled && super.canScrollVertically();
  }

  @Override public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
      int position) {
    RecyclerView.SmoothScroller smoothScroller =
        new TopSnappedSmoothScroller(recyclerView.getContext());
    smoothScroller.setTargetPosition(position);
    startSmoothScroll(smoothScroller);
  }

  private class TopSnappedSmoothScroller extends LinearSmoothScroller {
    public TopSnappedSmoothScroller(Context context) {
      super(context);
    }

    @Override public PointF computeScrollVectorForPosition(int targetPosition) {
      return HomeLayoutManager.this.computeScrollVectorForPosition(targetPosition);
    }

    @Override protected int getVerticalSnapPreference() {
      return SNAP_TO_START;
    }
  }
}
