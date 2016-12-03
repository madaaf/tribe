package com.tribe.app.presentation.view.adapter.decorator;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Adds margin at the end of the RecyclerView.
 */
public class MemberListLastItemDecoration extends RecyclerView.ItemDecoration {

    private int marginEnd;

    /**
     * Sole constructor. Takes in {@link Drawable} objects to be used as
     * horizontal and vertical dividers.
     * @param marginEnd the margin end
     */
    public MemberListLastItemDecoration(int marginEnd) {
        this.marginEnd = marginEnd;
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
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        final int itemPosition = parent.getChildAdapterPosition(view);
        final int itemCount = state.getItemCount();

        outRect.right = 0;

        boolean childIsInLastRow = itemCount > 0 && itemPosition == itemCount - 1;
        if (childIsInLastRow) {
            outRect.right = marginEnd;
        }
    }
}