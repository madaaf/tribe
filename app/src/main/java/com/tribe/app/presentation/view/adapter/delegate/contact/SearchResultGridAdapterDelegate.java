package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.presentation.view.adapter.delegate.base.BaseListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.ButtonModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class SearchResultGridAdapterDelegate extends BaseListAdapterDelegate {

  private ButtonModel buttonModelFirst = null;
  private ButtonModel buttonModelSecond = null;

  public SearchResultGridAdapterDelegate(Context context) {
    super(context);
  }

  @Override public boolean isForViewType(@NonNull List<Object> items, int position) {
    return (items.get(position) instanceof SearchResult);
  }

  @Override protected ButtonModel getButtonModelFrom(BaseListInterface baseListItem) {
    SearchResult searchResult = (SearchResult) baseListItem;
    if (buttonModelFirst != null) return buttonModelFirst;

    if (searchResult.getFriendship() == null) {
      buttonModelFirst = getAddFriendButton();
    } else if (searchResult.getFriendship() != null && !searchResult.getFriendship()
        .isBlockedOrHidden()) {
      buttonModelFirst = getHangLiveButton();
    } else if (searchResult.getFriendship().isBlocked()) {
      buttonModelFirst = getUnblockButton();
    } else if (searchResult.getFriendship().isHidden()) {
      buttonModelFirst = getUnhideButton();
    }

    return buttonModelFirst;
  }

  @Override protected ButtonModel getButtonModelTo(BaseListInterface baseListItem) {
    SearchResult searchResult = (SearchResult) baseListItem;
    if (buttonModelSecond != null) return buttonModelSecond;

    if (searchResult.getFriendship() == null) {
      buttonModelSecond = getAddFriendButton();
    } else if (searchResult.getFriendship() != null && !searchResult.getFriendship()
        .isBlockedOrHidden()) {
      buttonModelSecond = getHangLiveButton();
    } else if (searchResult.getFriendship().isBlocked()) {
      buttonModelSecond = getUnblockButton();
    } else if (searchResult.getFriendship().isHidden()) {
      buttonModelSecond = getUnhideButton();
    }

    return buttonModelSecond;
  }

  private ButtonModel getAddFriendButton() {
    return new ButtonModel(context.getString(R.string.action_add_friend),
        ContextCompat.getColor(context, R.color.blue_new), Color.WHITE);
  }

  private ButtonModel getHangLiveButton() {
    return new ButtonModel(context.getString(R.string.action_add_friend),
        ContextCompat.getColor(context, R.color.red), Color.WHITE);
  }

  private ButtonModel getUnblockButton() {
    return new ButtonModel(context.getString(R.string.action_add_friend),
        ContextCompat.getColor(context, R.color.grey_unblock), Color.WHITE);
  }

  private ButtonModel getUnhideButton() {
    return new ButtonModel(context.getString(R.string.action_add_friend),
        ContextCompat.getColor(context, R.color.blue_new), Color.WHITE);
  }

  @Override protected void setClicks(BaseListInterface baseListItem, BaseListViewHolder vh) {
    SearchResult searchResult = (SearchResult) baseListItem;
    if (!searchResult.isInvisibleMode() && !searchResult.isMyself() && (searchResult.getFriendship()
        == null || searchResult.getFriendship().isBlockedOrHidden())) {
      vh.btnAdd.setOnClickListener(v -> {
        searchResult.setShouldAnimateAdd(true);
        clickAdd.onNext(vh.itemView);
      });
    } else {
      vh.btnAdd.setOnClickListener(v -> clickHangLive.onNext(vh.itemView));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<Object> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
  }
}
