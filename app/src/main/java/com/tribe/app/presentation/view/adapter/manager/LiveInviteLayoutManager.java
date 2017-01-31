package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

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
}
