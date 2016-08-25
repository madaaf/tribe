package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Layout manager to position items inside a {@link RecyclerView}.
 */
public class LevelLayoutManager extends GridLayoutManager {

    public static final int spanCount = 4;

    public LevelLayoutManager(Context context) {
        super(context, spanCount);
    }
}
