package com.tribe.app.data.realm.helpers;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.tribe.app.data.realm.TribeRealm;

import java.util.List;

public class TribeRealmListDiffCallback extends DiffUtil.Callback {

    private List<TribeRealm> oldList;
    private List<TribeRealm> newList;

    public TribeRealmListDiffCallback(List<TribeRealm> oldList, List<TribeRealm> newList) {
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
        return newList.get(newItemPosition).getLocalId() == oldList.get(oldItemPosition).getLocalId();
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