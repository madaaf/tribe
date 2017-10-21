package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Layout manager to position items inside a {@link android.support.v7.widget.RecyclerView}.
 */
public class NewChatLayoutManager extends LinearLayoutManager {

  private boolean isScrollEnabled = true;

  public NewChatLayoutManager(Context context) {
    super(context);
  }

  public void setScrollEnabled(boolean flag) {
    this.isScrollEnabled = flag;
  }

  @Override public boolean canScrollVertically() {
    return isScrollEnabled && super.canScrollVertically();
  }
}
