package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.RxAdapterDelegatesManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager<List<Message>> delegatesManager;
  private List<Message> items;
  private MessageAdapterDelegate messageAdapterDelegate;

  public MessageAdapter(Context context) {
    delegatesManager = new RxAdapterDelegatesManager<>();

    messageAdapterDelegate = new MessageAdapterDelegate(context);
    delegatesManager.addDelegate(messageAdapterDelegate);

    items = new ArrayList<>();
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

  public void setItems(List<Message> items) {
    this.items.addAll(items);
    notifyDataSetChanged();
  }
}
