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
        setScrollEnabled(false);
        setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (getItemCount() <= 2) return 2;
                else if (getItemCount() == 4
                        || getItemCount() == 6
                        || getItemCount() == 8) {
                    return 1;
                } else if (getItemCount() == 3
                        || getItemCount() == 5
                        || getItemCount() == 7) {
                    return position == 0 ? 2 : 1;
                }

                return getSpanCount();
            }
        });
    }

    public void setScrollEnabled(boolean enabled) {
        this.isScrollEnabled = enabled;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
