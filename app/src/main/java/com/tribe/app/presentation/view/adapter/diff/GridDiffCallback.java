package com.tribe.app.presentation.view.adapter.diff;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.adapter.viewmodel.UserLive;
import java.util.List;

public class GridDiffCallback extends DiffUtil.Callback {

    private List<Recipient> oldList;
    private List<Recipient> newList;

    public GridDiffCallback(List<Recipient> oldList, List<Recipient> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition).getUser().getId().equals(oldList.get(oldItemPosition).getUser().getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition).equals(oldList.get(oldItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return null;
    }
}