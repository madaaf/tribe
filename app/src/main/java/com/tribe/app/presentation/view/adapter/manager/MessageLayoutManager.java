package com.tribe.app.presentation.view.adapter.manager;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

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

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    private class TopSnappedSmoothScroller extends LinearSmoothScroller {

        private static final float MILLISECONDS_PER_INCH = 100f;

        public TopSnappedSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return MessageLayoutManager.this.computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_ANY;
        }
    }

    // OBSERVABLES
    public Observable<Integer> itemsDoneLayoutCallback() {
        return itemsDoneLayoutCallback;
    }
}
