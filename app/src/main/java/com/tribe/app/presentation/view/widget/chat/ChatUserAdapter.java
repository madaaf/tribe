package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.RxAdapterDelegatesManager;
import java.util.ArrayList;
import java.util.List;

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

  @Override public int getItemCount() {
    return items.size();
  }

  public void setItems(List<User> items) {
    this.items.clear();
    this.items.addAll(items);
    notifyDataSetChanged();
  }
}
