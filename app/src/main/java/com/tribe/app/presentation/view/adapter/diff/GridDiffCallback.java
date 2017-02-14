package com.tribe.app.presentation.view.adapter.diff;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.List;

public class GridDiffCallback extends DiffUtil.Callback {

  private List<Recipient> oldList;
  private List<Recipient> newList;

  public GridDiffCallback(List<Recipient> oldList, List<Recipient> newList) {
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
    Recipient newRecipient = newList.get(newItemPosition);
    Recipient oldRecipient = oldList.get(oldItemPosition);

    Bundle diffBundle = new Bundle();

    if (!ObjectUtils.nullSafeEquals(newRecipient.getDisplayName(), oldRecipient.getDisplayName())) {
      diffBundle.putString(Recipient.DISPLAY_NAME, newRecipient.getDisplayName());
    }

    if (!ObjectUtils.nullSafeEquals(newRecipient.getProfilePicture(),
        oldRecipient.getProfilePicture())) {
      diffBundle.putString(Recipient.PROFILE_PICTURE, newRecipient.getProfilePicture());
    }

    if (!ObjectUtils.nullSafeEquals(newRecipient.getLastSeenAt(), oldRecipient.getLastSeenAt())) {
      diffBundle.putSerializable(Recipient.LAST_ONLINE, newRecipient.getLastSeenAt());
    }

    if (newRecipient.isLive() != oldRecipient.isLive()) {
      diffBundle.putBoolean(Recipient.IS_LIVE, newRecipient.isLive());
    }

    if (newRecipient.isOnline() != oldRecipient.isOnline()) {
      diffBundle.putBoolean(Recipient.IS_ONLINE, newRecipient.isOnline());
    }

    if (diffBundle.size() == 0) return null;
    return diffBundle;
  }
}