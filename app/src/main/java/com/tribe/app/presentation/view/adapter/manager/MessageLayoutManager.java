package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import com.tribe.app.presentation.view.decorator.GridDividerItemDecoration;

/**
 * Layout manager to position items inside a {@link android.support.v7.widget.RecyclerView}.
 */
public class MessageLayoutManager extends LinearLayoutManager {

    public MessageLayoutManager(Context context) {
        super(context);
        setStackFromEnd(true);
    }
}