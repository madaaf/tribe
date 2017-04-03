package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

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
}
