package com.tribe.app.presentation.view.adapter.interfaces;

public interface RecyclerViewItemEnabler {
    boolean isAllItemsEnabled();
    boolean getItemEnabled(int position);
}