package com.tribe.app.presentation.view.adapter.decorator;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Adds interior dividers to a RecyclerView.
 */
public class DividerFirstLastItemDecoration extends RecyclerView.ItemDecoration {

  private int marginTop;
  private int marginBottom;
  private int startPosition;

  /**
   * Sole constructor. Takes in {@link Drawable} objects to be used as
   * horizontal and vertical dividers.
   *
   * @param marginTop the margin top
   * @param marginBottom the margin bottom
   * @param startPosition the start position of the decorator
   */
  public DividerFirstLastItemDecoration(int marginTop, int marginBottom, int startPosition) {
    this.marginTop = marginTop;
    this.marginBottom = marginBottom;
    this.startPosition = startPosition;
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

    final int itemPosition = parent.getChildAdapterPosition(view);
    final int itemCount = state.getItemCount();

    boolean childIsInFirstRow = itemPosition == startPosition;

    outRect.top = 0;
    outRect.bottom = 0;

    if (childIsInFirstRow) {
      outRect.top = marginTop;
    }

    boolean childIsInLastRow = itemCount > 0 && itemPosition == itemCount - 1;
    if (childIsInLastRow) {
      outRect.bottom = marginBottom;
    }
  }
}