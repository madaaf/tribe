package com.tribe.app.presentation.view.adapter.decorator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BaseListDividerDecoration extends RecyclerView.ItemDecoration {

  private final Paint paint;
  private float heightDp;

  public BaseListDividerDecoration(Context context, int color, float heightDp) {
    paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(color);
    this.heightDp = heightDp;
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    if (hasDividerOnBottom(view, parent, state)) {
      outRect.set(0, 0, 0, (int) heightDp);
    } else {
      outRect.setEmpty();
    }
  }

  private boolean hasDividerOnBottom(View view, RecyclerView parent, RecyclerView.State state) {
    int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
    return position < state.getItemCount() - 1;
  }

  @Override public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      View view = parent.getChildAt(i);
      if (hasDividerOnBottom(view, parent, state)) {
        c.drawRect(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom() + heightDp,
            paint);
      }
    }
  }
}