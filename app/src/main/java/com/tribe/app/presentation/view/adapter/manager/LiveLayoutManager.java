package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

/**
 * Layout manager to position items inside a {@link android.support.v7.widget.RecyclerView}.
 */
public class LiveLayoutManager extends GridLayoutManager {

    private boolean isScrollEnabled = true;

    public LiveLayoutManager(Context context) {
        super(context, 2);
        setItemPrefetchEnabled(true);
        setReverseLayout(true);
        setInitialPrefetchItemCount(8);
    }

    public void setScrollEnabled(boolean enabled) {
        this.isScrollEnabled = enabled;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
