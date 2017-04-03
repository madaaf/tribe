package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import java.util.List;

/**
 * Created by tiago on 03/21/2017
 */
public class MoreFriendsAdapterDelegate extends CTAAdapterDelegate {

  public MoreFriendsAdapterDelegate(Context context, boolean shouldRoundCorners) {
    super(context, shouldRoundCorners);
  }

  @Override protected int getLayoutId() {
    return R.layout.item_cta_grid;
  }

  @Override protected int getAvatarResource() {
    return R.drawable.picto_more_friends;
  }

  @Override protected String getTitle() {
    return context.getString(R.string.grid_name_more_friends);
  }

  @Override protected String getSubtitle() {
    return context.getString(R.string.grid_status_more_friends);
  }

  @Override public boolean isForViewType(@NonNull List<Recipient> items, int position) {
    return items.get(position).getSubId().equals(Recipient.ID_MORE);
  }
}
