package com.tribe.app.presentation.view.adapter.decorator;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Adds interior dividers to a RecyclerView with a GridLayoutManager.
 */
public class GridDividerTopItemDecoration extends RecyclerView.ItemDecoration {

  private int marginStart;
  private int spanCount;

  /**
   * Sole constructor. Takes in {@link Drawable} objects to be used as
   * horizontal and vertical dividers.
   *
   * @param marginStart the margin start
   * @param spanCount The number of columns in the grid of the RecyclerView
   */
  public GridDividerTopItemDecoration(int marginStart, int spanCount) {
    this.marginStart = marginStart;
    this.spanCount = spanCount;
  }

  /**
   * Determines the size and location of offsets between items in the parent
   * RecyclerView.
   *
   * @param outRect The {@link Rect} of offsets to be added around the child view
   * @param view The child view to be decorated with an offset
   * @param parent The RecyclerView onto which dividers are being added
   * @param state The current RecyclerView.State of the RecyclerView
   */
  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);

    boolean childIsInFirstRow = (parent.getChildAdapterPosition(view)) < spanCount;
    if (childIsInFirstRow) {
      outRect.top = marginStart;
    }
  }
}