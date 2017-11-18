package com.tribe.app.presentation.view.adapter.diff;

import android.support.v7.util.DiffUtil;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import java.util.ArrayList;
import java.util.List;

public class LiveInviteDiffCallback extends DiffUtil.Callback {

  private List<LiveInviteAdapterSectionInterface> oldList;
  private List<LiveInviteAdapterSectionInterface> newList;

  public LiveInviteDiffCallback(List<LiveInviteAdapterSectionInterface> oldList,
      List<LiveInviteAdapterSectionInterface> newList) {
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
    if (newList != null &&
        newList.get(newItemPosition) != null &&
        newList.get(newItemPosition).getId() != null) {
      return newList.get(newItemPosition).getId().equals(oldList.get(oldItemPosition).getId());
    } else {
      return false;
    }
  }

  @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
    return newList.get(newItemPosition).equals(oldList.get(oldItemPosition));
  }
}