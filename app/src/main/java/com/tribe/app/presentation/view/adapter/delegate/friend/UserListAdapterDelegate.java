package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import java.util.List;

/**
 * Created by tiago on 11/29/16.
 */
public class UserListAdapterDelegate extends BaseListAdapterDelegate {

  public UserListAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    if (items.get(position) instanceof User) {
      User user = (User) items.get(position);
      return !user.getId().equals(User.ID_EMPTY);
    } else {
      return false;
    }
  }

  @Override protected ButtonModel getButtonModelFrom(BaseListInterface baseListItem) {
    User user = (User) baseListItem;

    if (!user.isFriend()) {
      return getAddFriendButton();
    } else {
      return getHangLiveButton();
    }
  }

  @Override protected ButtonModel getButtonModelTo(BaseListInterface baseListItem) {
    return getHangLiveButton();
  }

  private ButtonModel getAddFriendButton() {
    return new ButtonModel(context.getString(R.string.action_add_friend),
        ContextCompat.getColor(context, R.color.blue_new), Color.WHITE);
  }

  private ButtonModel getHangLiveButton() {
    return new ButtonModel(context.getString(R.string.action_add_friend),
        ContextCompat.getColor(context, R.color.red), Color.WHITE);
  }

  @Override protected void setClicks(BaseListInterface baseListItem, BaseListViewHolder vh) {
    User user = (User) baseListItem;

    if (!user.isFriend() && !user.isInvisibleMode()) {
      vh.btnAdd.setOnClickListener(v -> {
        user.setAnimateAdd(true);
        clickAdd.onNext(vh.itemView);
      });
    } else if (user.isFriend()) {
      vh.btnAdd.setOnClickListener(null);
    }
  }
}
