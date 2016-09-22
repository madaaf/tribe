package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Layout manager to position items inside a {@link RecyclerView}.
 */
public class PullToSearchLayoutManager extends GridLayoutManager {

    public static final int spanCount = 5;

    public PullToSearchLayoutManager(Context context) {
        super(context, spanCount);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}
