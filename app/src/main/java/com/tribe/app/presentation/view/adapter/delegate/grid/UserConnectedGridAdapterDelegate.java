package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import java.util.List;

/**
 * Created by tiago on 01/18/2017.
 */
public class UserConnectedGridAdapterDelegate extends RecipientGridAdapterDelegate {

  public UserConnectedGridAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    return (items.get(position) instanceof Friendship
        || items.get(position) instanceof Invite
        || items.get(position) instanceof Membership)
        && !items.get(position)
        .getSubId()
        .equals(Recipient.ID_HEADER)
        && !items.get(position).getSubId().equals(Recipient.ID_EMPTY)
        && items.get(position).isOnline()
        && !items.get(position).isLive();
  }

  @Override protected int getLayoutId() {
    return R.layout.item_user_connected_grid;
  }
}
