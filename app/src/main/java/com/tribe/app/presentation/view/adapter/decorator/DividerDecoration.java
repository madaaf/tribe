package com.tribe.app.presentation.view.adapter.decorator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import com.tribe.app.presentation.view.adapter.HomeListAdapter;
import timber.log.Timber;

public class DividerDecoration extends RecyclerView.ItemDecoration {

  private final Paint paint;
  private int heightDp;

  public DividerDecoration(Context context) {
    this(context, Color.argb((int) (255 * 0.2), 0, 0, 0), 1f);
  }

  public DividerDecoration(Context context, int color, float heightDp) {
    paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(color);
    this.heightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp,
        context.getResources().getDisplayMetrics());
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    if (hasDividerOnBottom(view, parent, state)) {
      outRect.set(0, 0, 0, heightDp);
    } else {
      outRect.setEmpty();
    }
  }

  private boolean hasDividerOnBottom(View view, RecyclerView parent, RecyclerView.State state) {
    int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
    Timber.d("Position : " + position + " / " + state.getItemCount());
    return position < state.getItemCount() - 1 &&
        parent.getAdapter().getItemViewType(position) != HomeListAdapter.HEADERS_VIEW_TYPE &&
        parent.getAdapter().getItemViewType(position + 1) != HomeListAdapter.HEADERS_VIEW_TYPE;
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