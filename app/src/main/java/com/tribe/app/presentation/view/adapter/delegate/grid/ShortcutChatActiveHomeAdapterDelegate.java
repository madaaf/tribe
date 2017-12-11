package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.adapter.delegate.common.RecipientAdapterDelegate;
import java.util.List;

/**
 * Created by tiago on 01/18/2017.
 */
public class ShortcutChatActiveHomeAdapterDelegate extends RecipientAdapterDelegate {

  public ShortcutChatActiveHomeAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    if (items.get(position) instanceof Recipient) {
      Recipient recipient = items.get(position);
      return recipient instanceof Shortcut && !recipient.isRead() && !recipient.getId()
          .equals(Recipient.ID_HEADER) && !recipient.getId().equals(Recipient.ID_EMPTY);
    }

    return false;
  }

  @Override protected int getLayoutId() {
    return R.layout.item_home_chat;
  }
}
