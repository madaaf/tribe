package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.RxAdapterDelegatesManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class ChatUserAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager<List<User>> delegatesManager;
  private List<User> items;
  private ChatUserAdapterDelegate chatUserAdapterDelegate;

  public ChatUserAdapter(Context context) {
    items = new ArrayList<>();

    delegatesManager = new RxAdapterDelegatesManager<>();

    chatUserAdapterDelegate = new ChatUserAdapterDelegate(context);
    delegatesManager.addDelegate(chatUserAdapterDelegate);
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return delegatesManager.onCreateViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    delegatesManager.onBindViewHolder(items, position, holder);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
    if (payloads.isEmpty()) {
      delegatesManager.onBindViewHolder(items, position, holder);
    } else {
      //Timber.e("ON BOND HOLDER PLAYLOAD " + position + " " + payloads.size());
      delegatesManager.onBindViewHolder(items, holder, position, payloads);
    }
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public int getIndexOfUser(User user) {
    return items.indexOf(user);
  }

  public void sorrList(List<User> list) {
    Collections.sort(list, (o1, o2) -> {
      int b1, b2;
      if (o1.isOnline() && o1.isTyping()) {
        b1 = 3;
      } else if (o1.isTyping()) {
        b1 = 2;
      } else if (o1.isOnline()) {
        b1 = 1;
      } else {
        b1 = 0;
      }

      if (o2.isOnline() && o2.isTyping()) {
        b2 = 3;
      } else if (o2.isTyping()) {
        b2 = 2;
      } else if (o2.isOnline()) {
        b2 = 1;
      } else {
        b2 = 0;
      }
      return b2 - b1;
    });
  }

  public void setItems(List<User> list) {
    sorrList(list);
    this.items.clear();
    this.items.addAll(list);
    notifyDataSetChanged();
  }
}
