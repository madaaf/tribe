package com.tribe.app.presentation.view.adapter.decorator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.utils.ScreenUtils;

/**
 * https://github.com/paetztm/recycler_view_headers
 */

public abstract class BaseSectionItemDecoration extends RecyclerView.ItemDecoration {

  @IntDef({
      LIVE_CHAT_MEMBERS, LIVE_INVITE_LINK, LIVE_ADD_FRIENDS_IN_CALL, HOME_ONGOING, HOME_ONLINE,
      HOME_RECENT
  }) public @interface HeaderType {
  }

  public static final int LIVE_CHAT_MEMBERS = 0;
  public static final int LIVE_INVITE_LINK = 1;
  public static final int LIVE_ADD_FRIENDS_IN_CALL = 2;
  public static final int HOME_ONGOING = 3;
  public static final int HOME_ONLINE = 4;
  public static final int HOME_RECENT = 5;
  public static final int SEARCH_RESULTS = 6;
  public static final int SEARCH_SUGGESTED_CONTACTS = 7;
  public static final int SEARCH_INVITES_TO_SEND = 8;
  public static final int SEARCH_EMPTY = 9;

  protected final int headerOffset;
  protected final boolean sticky;
  protected final SectionCallback sectionCallback;
  protected final ScreenUtils screenUtils;

  protected View headerView;
  protected int previousHeader = -1;
  protected float[] radiusMatrix;

  public BaseSectionItemDecoration(int headerHeight, boolean sticky,
      @NonNull SectionCallback sectionCallback, ScreenUtils screenUtils) {
    headerOffset = headerHeight;
    this.sticky = sticky;
    this.sectionCallback = sectionCallback;
    this.screenUtils = screenUtils;
    this.radiusMatrix = new float[] { 5, 5, 5, 5, 0, 0, 0, 0 };
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);

    int pos = parent.getChildAdapterPosition(view);
    if (sectionCallback.isSection(pos)) {
      outRect.top = headerOffset;
    }
  }

  @Override public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
    super.onDrawOver(c, parent, state);

    if (headerView == null) {
      headerView = inflateHeaderView(parent);
      fixLayoutSize(headerView, parent);
    }

    for (int i = 0; i < parent.getChildCount(); i++) {
      View child = parent.getChildAt(i);
      final int position = parent.getChildAdapterPosition(child);

      int headerType = sectionCallback.getSectionType(position);
      setHeaderType(parent.getContext(), headerType, position);
      if (previousHeader != headerType || sectionCallback.isSection(position)) {
        drawHeader(c, child, headerView);
        previousHeader = headerType;
      }
    }
  }

  public void setHeaderType(Context context, @HeaderType int headerType, int position) {
    GradientDrawable gradientDrawable;

    if (headerView.getBackground() == null) {
      gradientDrawable = new GradientDrawable();
      gradientDrawable.setShape(GradientDrawable.RECTANGLE);
      headerView.setBackground(gradientDrawable);
    } else {
      gradientDrawable = (GradientDrawable) headerView.getBackground();
    }

    if (position <= 1) {
      gradientDrawable.setCornerRadii(radiusMatrix);
    } else {
      gradientDrawable.setCornerRadius(0);
    }

    populateHeader(context, headerType, gradientDrawable);
  }

  private void drawHeader(Canvas c, View child, View headerView) {
    c.save();
    if (sticky) {
      c.translate(0, Math.max(0, child.getTop() - headerView.getHeight()));
    } else {
      c.translate(0, child.getTop() - headerView.getHeight());
    }
    headerView.draw(c);
    c.restore();
  }

  /**
   * Measures the header view to make sure its size is greater than 0 and will be drawn
   * https://yoda.entelect.co.za/view/9627/how-to-android-recyclerview-item-decorations
   */
  private void fixLayoutSize(View view, ViewGroup parent) {
    int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
    int heightSpec =
        View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

    int childWidth =
        ViewGroup.getChildMeasureSpec(widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(),
            view.getLayoutParams().width);
    int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
        parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams().height);

    view.measure(childWidth, childHeight);

    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
  }

  protected abstract void populateHeader(Context context, int headerType,
      GradientDrawable gradientDrawable);

  protected abstract View inflateHeaderView(RecyclerView parent);
}