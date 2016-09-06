package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

/**
 * Layout manager to position items inside a {@link android.support.v7.widget.RecyclerView}.
 */
public class HomeLayoutManager extends GridLayoutManager {

    public HomeLayoutManager(Context context) {
        super(context, 2);
    }
}
