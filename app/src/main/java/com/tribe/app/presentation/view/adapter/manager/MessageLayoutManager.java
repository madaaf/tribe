package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Layout manager to position items inside a {@link android.support.v7.widget.RecyclerView}.
 */
public class MessageLayoutManager extends LinearLayoutManager {

    private PublishSubject<Integer> itemsDoneLayoutCallback = PublishSubject.create();

    public MessageLayoutManager(Context context) {
        super(context);
        setStackFromEnd(true);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);

        final int firstVisibleItemPosition = findFirstVisibleItemPosition();

        if (firstVisibleItemPosition != -1) {
            itemsDoneLayoutCallback.onNext(firstVisibleItemPosition);
        }
    }

    public Observable<Integer> itemsDoneLayoutCallback() {
        return itemsDoneLayoutCallback;
    }
}
