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
 * Created by tiago on 11/29/16.
 */
public class MemberListAdapterDelegate extends BaseListAdapterDelegate {

  public MemberListAdapterDelegate(Context context) {
    super(context);
  }

  @Override protected ButtonModel getButtonModelFrom(BaseListInterface baseListItem) {
    GroupMember groupMember = (GroupMember) baseListItem;

    if (groupMember.isFriend()) {
      return getHangLiveButton();
    } else {
      return getAddFriendButton();
    }
  }

  @Override protected ButtonModel getButtonModelTo(BaseListInterface baseListItem) {
    GroupMember groupMember = (GroupMember) baseListItem;

    if (!groupMember.isFriend()) {
      return getHangLiveButton();
    } else {
      return getAddFriendButton();
    }
  }

  @Override protected void setClicks(BaseListInterface baseListItem, BaseListViewHolder vh) {
    GroupMember groupMember = (GroupMember) baseListItem;
    if (groupMember.getUser().isInvisibleMode()) {
      vh.btnAdd.setOnClickListener(v -> {
        clickAdd.onNext(vh.itemView);
      });
    } else if (!groupMember.isFriend() && !groupMember.getUser().isInvisibleMode()) {
      vh.btnAdd.setOnClickListener(v -> {
        animations.put(vh, animateProgressBar(vh));
        groupMember.setAnimateAdd(true);
        clickAdd.onNext(vh.itemView);
      });
    } else if (groupMember.isFriend()) {
      vh.btnAdd.setOnClickListener(v -> clickHangLive.onNext(vh.itemView));
    }
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof GroupMember;
  }

  private ButtonModel getHangLiveButton() {
    return new ButtonModel(context.getString(R.string.action_hang_live),
        ContextCompat.getColor(context, R.color.red), Color.WHITE);
  }

  private ButtonModel getAddFriendButton() {
    return new ButtonModel(context.getString(R.string.action_add_friend),
        ContextCompat.getColor(context, R.color.blue_new), Color.WHITE);
  }
}
