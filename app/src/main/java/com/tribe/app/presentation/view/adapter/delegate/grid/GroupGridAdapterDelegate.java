package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;

import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class GroupGridAdapterDelegate extends RecipientGridAdapterDelegate {

  public GroupGridAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    return items.get(position) instanceof Membership;
  }

  @Override protected int getLayoutId() {
    return R.layout.item_user_grid;
  }
}
