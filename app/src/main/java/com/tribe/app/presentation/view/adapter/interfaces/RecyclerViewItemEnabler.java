package com.tribe.app.presentation.view.adapter.interfaces;

public interface RecyclerViewItemEnabler {
    public boolean isAllItemsEnabled();
    public boolean getItemEnabled(int position);
}