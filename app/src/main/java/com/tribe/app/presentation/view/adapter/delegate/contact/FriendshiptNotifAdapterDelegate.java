package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseNotifAdapterDelegate;
import java.util.List;
import rx.Observable;

public class FriendshiptNotifAdapterDelegate extends BaseNotifAdapterDelegate {

  public FriendshiptNotifAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof Recipient;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    return onCreateViewHolderNotif(parent);
  }

  @Override public void onBindViewHolder(@NonNull List<Object> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    //Friendship friendship = null;
    //if (items.get(position) instanceof Recipient) {
    //  friendship = (Friendship) items.get(position);
    //}
    //if (friendship.getStatus() != null && friendship.getStatus().equals(FriendshipRealm.HIDDEN)
    //    || friendship.getStatus().equals(FriendshipRealm.BLOCKED)) {
    //  onBindViewHolderForUnfriend(friendship.getFriend(), holder, true);
    //} else {
    //  onBindViewHolderForFriend(friendship.getFriend(), holder);
    //}
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  public Observable<View> clickMore() {
    return clickMore;
  }

  public Observable<View> onUnblock() {
    return onUnblock;
  }
}
