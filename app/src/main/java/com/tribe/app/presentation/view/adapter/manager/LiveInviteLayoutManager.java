package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Layout manager to position items inside a {@link android.support.v7.widget.RecyclerView}.
 */
public class LiveInviteLayoutManager extends LinearLayoutManager {

  private boolean isScrollEnabled = true;

  public LiveInviteLayoutManager(Context context) {
    super(context);
    setItemPrefetchEnabled(true);
    setInitialPrefetchItemCount(8);
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
      return LiveInviteLayoutManager.this.computeScrollVectorForPosition(targetPosition);
    }

    @Override protected int getVerticalSnapPreference() {
      return SNAP_TO_START;
    }

    @Override protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
      return 50f / displayMetrics.densityDpi;
    }
  }
}
