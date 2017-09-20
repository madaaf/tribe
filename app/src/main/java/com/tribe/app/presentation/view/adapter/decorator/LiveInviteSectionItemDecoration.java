package com.tribe.app.presentation.view.adapter.decorator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.header.HomeListViewHeader;
import com.tribe.app.presentation.view.widget.header.LiveInviteViewHeader;

/**
 * https://github.com/paetztm/recycler_view_headers
 */

public class LiveInviteSectionItemDecoration extends RecyclerView.ItemDecoration {

  private final int headerOffset;
  private final boolean sticky;
  private final SectionCallback sectionCallback;
  private final ScreenUtils screenUtils;

  private View headerView;
  private TextViewFont txtLabel;
  private int previousHeader = -1;

  public LiveInviteSectionItemDecoration(int headerHeight, boolean sticky,
      @NonNull SectionCallback sectionCallback, ScreenUtils screenUtils) {
    headerOffset = headerHeight;
    this.sticky = sticky;
    this.sectionCallback = sectionCallback;
    this.screenUtils = screenUtils;
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
      txtLabel = (TextViewFont) headerView.findViewById(R.id.txtLabel);
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

  public void setHeaderType(Context context, @HomeListViewHeader.HeaderType int headerType,
      int position) {
    switch (headerType) {
      case LiveInviteViewHeader.CHAT_MEMBERS:
        txtLabel.setText(R.string.live_invite_section_chat_members);
        break;

      case LiveInviteViewHeader.INVITE_LINK:
        txtLabel.setText(R.string.live_invite_section_invite_link);
        break;

      case LiveInviteViewHeader.ADD_FRIENDS_IN_CALL:
        txtLabel.setText(R.string.live_invite_section_add_friends);
        break;
    }
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

  private View inflateHeaderView(RecyclerView parent) {
    return LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_home_list_header, parent, false);
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
}