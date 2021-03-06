package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseNotifAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.BaseNotifViewHolder;
import java.util.List;
import rx.Observable;

/**
 * Created by tiago on 11/29/16.
 */
public class UserNotifAdapterDelegate extends BaseNotifAdapterDelegate {
  private User user;

  public UserNotifAdapterDelegate(Context context, User user) {
    super(context);
    this.user = user;
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    if (items.get(position) instanceof User) {
      User user = (User) items.get(position);
      return !user.getId().equals(User.ID_EMPTY);
    } else {
      return false;
    }
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    return onCreateViewHolderNotif(parent);
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    User friend = (User) items.get(position);
    onBindViewHolderForUnfriend(friend, holder, false, user);
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {

  }

  public Observable<BaseNotifViewHolder> onClickAdd() {
    return onClickAdd;
  }

  public Observable<BaseNotifViewHolder> clickMore() {
    return clickMore;
  }
}
