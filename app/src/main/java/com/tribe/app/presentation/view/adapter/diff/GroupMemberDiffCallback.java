package com.tribe.app.presentation.view.adapter.diff;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.tribe.app.domain.entity.GroupMember;

import java.util.List;

public class GroupMemberDiffCallback extends DiffUtil.Callback {

    private List<GroupMember> oldList;
    private List<GroupMember> newList;

    public GroupMemberDiffCallback(List<GroupMember> oldList, List<GroupMember> newList) {
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
//        GroupMember newMember = newList.get(newItemPosition);
//        GroupMember oldMember = oldList.get(oldItemPosition);
//
//        Bundle diffBundle = new Bundle();
//
//        if (newMember.getUser().getProfilePicture().equals(oldMember.getUser().getProfilePicture())) {
//            diffBundle.putBoolean(KEY_DISCOUNT, newProduct.hasDiscount());
//        }
//
//        if (newProduct.getReviews().size() != oldProduct.getReviews().size()) {
//            diffBundle.putInt(Product.KEY_REVIEWS_COUNT, newProduct.getReviews().size());
//        }
//
//        if (newProduct.getPrice() != oldProduct.getPrice()) {
//            diffBundle.putFloat(Product.KEY_PRICE, newProduct.getPrice());
//        }
//
//        if (diffBundle.size() == 0) return null;
//
//        return diffBundle;
    }
}