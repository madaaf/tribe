package com.tribe.app.presentation.view.adapter.delegate.friend;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import java.util.List;

/**
 * Created by tiago on 11/29/16.
 */
public class RecipientListAdapterDelegate extends BaseListAdapterDelegate {

  public RecipientListAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return items.get(position) instanceof Recipient;
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }

  @Override protected ButtonModel getButtonModelFrom(BaseListInterface baseListItem) {
    Recipient recipient = (Recipient) baseListItem;

    Shortcut shortcut = (Shortcut) recipient;

    if (shortcut.isBlocked()) {
      return getUnblockButton();
    } else if (shortcut.isHidden()) {
      return getUnhideButton();
    } else {
      return getHangLiveButton();
    }
  }

  @Override protected ButtonModel getButtonModelTo(BaseListInterface baseListItem) {
    return getHangLiveButton();
  }

  private ButtonModel getHangLiveButton() {
    return new ButtonModel(context.getString(R.string.action_hang_live),
        ContextCompat.getColor(context, R.color.red), Color.WHITE);
  }

  private ButtonModel getUnblockButton() {
    return new ButtonModel(context.getString(R.string.action_unblock),
        ContextCompat.getColor(context, R.color.grey_unblock), Color.WHITE);
  }

  private ButtonModel getUnhideButton() {
    return new ButtonModel(context.getString(R.string.action_unhide),
        ContextCompat.getColor(context, R.color.blue_new), Color.WHITE);
  }

  @Override protected void setClicks(BaseListInterface baseList, BaseListViewHolder vh) {
    boolean isBlockedOrHidden = false;

    // TODO REPLACE WITH SHORTCUTS
    //if (baseList instanceof Friendship) {
    //  Friendship fr = (Friendship) baseList;
    //  isBlockedOrHidden = fr.isBlockedOrHidden();
    //}

    if (isBlockedOrHidden) {
      vh.btnAdd.setOnClickListener(v -> clickUnblock.onNext(vh.itemView));
    } else {
      vh.btnAdd.setOnClickListener(v -> clickHangLive.onNext(vh.itemView));
    }
  }
}
