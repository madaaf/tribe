package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class UserHomeAdapterDelegate extends RecipientHomeAdapterDelegate {

  public UserHomeAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    Recipient recipient = items.get(position);
    return !recipient.getSubId().equals(Recipient.ID_HEADER);
  }

  @Override protected int getLayoutId() {
    return R.layout.item_home_normal;
  }
}
