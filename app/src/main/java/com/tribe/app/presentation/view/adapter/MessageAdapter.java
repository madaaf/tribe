package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegatesManager;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.presentation.view.adapter.delegate.text.MeMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.UserMessageAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tiago on 18/05/2016.
 */
public class MessageAdapter extends RecyclerView.Adapter {

    private AdapterDelegatesManager<List<Message>> delegatesManager;
    private List<Message> items;
    private Message oldestMessage;

    @Inject
    public MessageAdapter(Context context) {
        delegatesManager = new AdapterDelegatesManager<>();
        delegatesManager.addDelegate(new MeMessageAdapterDelegate(context));
        delegatesManager.addDelegate(new UserMessageAdapterDelegate(context));
        items = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, position, holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Message> messageList) {
        if (!items.isEmpty()) {
            oldestMessage = items.get(0);
        }

        items.clear();
        items.addAll(messageList);
        notifyDataSetChanged();
    }

    public void addItem(Message message) {
        if (!items.isEmpty()) {
            oldestMessage = items.get(items.size() - 1);
        }

        items.add(message);
        notifyDataSetChanged();
    }

    public Message getMessage(int position) {
        return items.get(position);
    }

    public Message getOldestMessage() {
        return oldestMessage;
    }

    public int getIndexOfMessage(Message message) {
        return items.indexOf(message);
    }
}
