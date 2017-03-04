package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import com.tribe.app.R;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import java.util.List;

/**
 * Created by tiago on 11/22/16.
 */
public class FriendMemberAdapterDelegate extends BaseListAdapterDelegate {

  public FriendMemberAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof GroupMember;
  }

  @Override protected ButtonModel getButtonModelFrom(BaseListInterface baseListItem) {
    GroupMember groupMember = (GroupMember) baseListItem;

    if (groupMember.isMember()) {
      return getAddedButton();
    } else {
      return getAddButton();
    }
  }

  @Override protected ButtonModel getButtonModelTo(BaseListInterface baseListItem) {
    GroupMember groupMember = (GroupMember) baseListItem;

    if (groupMember.isMember()) {
      return getAddButton();
    } else {
      return getAddedButton();
    }
  }

  private ButtonModel getAddedButton() {
    return new ButtonModel(context.getString(R.string.group_add_members_added),
        ContextCompat.getColor(context, R.color.violet_opacity_10),
        ContextCompat.getColor(context, R.color.violet));
  }

  private ButtonModel getAddButton() {
    return new ButtonModel(context.getString(R.string.group_add_members_in_group),
        ContextCompat.getColor(context, R.color.violet), Color.WHITE);
  }

  @Override protected void setClicks(BaseListInterface baseList, BaseListViewHolder vh) {
    GroupMember groupMember = (GroupMember) baseList;
    if (!groupMember.isOgMember()) {
      vh.btnAdd.setOnClickListener(v -> onClick(baseList, vh));
      vh.itemView.setOnClickListener(v -> onClick(baseList, vh));
    } else {
      vh.itemView.setOnClickListener(null);
    }
  }

  private void onClick(BaseListInterface baseList, BaseListViewHolder vh) {
    clickAdd.onNext(vh.itemView);
    baseList.setAnimateAdd(true);
  }
}
