package com.tribe.app.presentation.view.adapter.delegate.common;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.adapter.delegate.common.RecipientAdapterDelegate;
import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class ShortcutAdapterDelegate extends RecipientAdapterDelegate {

  public ShortcutAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    Recipient recipient = items.get(position);
    return recipient instanceof Shortcut &&
        !recipient.getId().equals(Recipient.ID_HEADER) &&
        !recipient.getId().equals(Recipient.ID_EMPTY) &&
        recipient.isRead();
  }

  @Override protected int getLayoutId() {
    return R.layout.item_home_normal;
  }
}
