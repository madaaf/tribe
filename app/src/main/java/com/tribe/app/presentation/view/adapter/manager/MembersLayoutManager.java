package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Layout manager to position items inside a {@link android.support.v7.widget.RecyclerView}.
 */
public class MembersLayoutManager extends LinearLayoutManager {

    public MembersLayoutManager(Context context) {
        super(context, LinearLayoutManager.HORIZONTAL, false);
    }
}
