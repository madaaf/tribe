package com.tribe.app.presentation.view.adapter.diff;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.List;

public class GridDiffCallback extends DiffUtil.Callback {

  private List<HomeAdapterInterface> oldList;
  private List<HomeAdapterInterface> newList;

  public GridDiffCallback(List<HomeAdapterInterface> oldList, List<HomeAdapterInterface> newList) {
    this.oldList = new ArrayList<>();
    this.oldList.addAll(oldList);
    this.newList = new ArrayList<>();
    this.newList.addAll(newList);
  }

  @Override public int getOldListSize() {
    return oldList != null ? oldList.size() : 0;
  }

  @Override public int getNewListSize() {
    return newList != null ? newList.size() : 0;
  }

  @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
    return newList.get(newItemPosition).getId().equals(oldList.get(oldItemPosition).getId());
  }

  @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
    return newList.get(newItemPosition).equals(oldList.get(oldItemPosition));
  }

  @Nullable @Override public Object getChangePayload(int oldItemPosition, int newItemPosition) {
    HomeAdapterInterface newItem = newList.get(newItemPosition);
    HomeAdapterInterface oldItem = oldList.get(oldItemPosition);

    Bundle diffBundle = new Bundle();

    if (!ObjectUtils.nullSafeEquals(newItem.getDisplayName(), oldItem.getDisplayName())) {
      diffBundle.putString(Recipient.DISPLAY_NAME, newItem.getDisplayName());
    }

    if (!ObjectUtils.nullSafeEquals(newItem.getProfilePicture(), oldItem.getProfilePicture())) {
      diffBundle.putString(Recipient.PROFILE_PICTURE, newItem.getProfilePicture());
    }

    if (!ObjectUtils.nullSafeEquals(newItem.getLastSeenAt(), oldItem.getLastSeenAt())) {
      diffBundle.putSerializable(Recipient.LAST_ONLINE, newItem.getLastSeenAt());
    }

    if (newItem.isLive() != oldItem.isLive()) {
      diffBundle.putBoolean(Recipient.IS_LIVE, newItem.isLive());
    }

    if (newItem.isOnline() != oldItem.isOnline()) {
      diffBundle.putBoolean(Recipient.IS_ONLINE, newItem.isOnline());
    }

    if (newItem.isRead() != oldItem.isRead()) {
      diffBundle.putBoolean(Recipient.IS_ONLINE, newItem.isOnline());
    }

    if (diffBundle.size() == 0) return null;
    return diffBundle;
  }
}