package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Message;
import com.tribe.app.presentation.view.adapter.delegate.text.EmojiMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.LinkMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.MeMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.OtherHeaderMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.RegularMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.TodayHeaderMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.TutorialMessageAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class MessageAdapter extends RecyclerView.Adapter {

    private RxAdapterDelegatesManager<List<Message>> delegatesManager;
    private List<Message> items;
    private Message oldestMessage;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public MessageAdapter(LayoutInflater inflater, Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        delegatesManager.addDelegate(new TutorialMessageAdapterDelegate(inflater, context));
        delegatesManager.addDelegate(new MeMessageAdapterDelegate(inflater, context));
        delegatesManager.addDelegate(new RegularMessageAdapterDelegate(inflater, context));
        delegatesManager.addDelegate(new EmojiMessageAdapterDelegate(inflater, context));
        delegatesManager.addDelegate(new LinkMessageAdapterDelegate(inflater, context));
        delegatesManager.addDelegate(new TodayHeaderMessageAdapterDelegate(inflater, context));
        delegatesManager.addDelegate(new OtherHeaderMessageAdapterDelegate(inflater, context));
        items = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public long getItemId(int position) {
        Message message = getMessage(position);

        if (message.getLocalId() == null) {
            return message.hashCode();
        } else {
            return message.getLocalId().hashCode() * 31 + message.getText().hashCode();
        }
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

    public void releaseSubscriptions() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        delegatesManager.releaseSubscriptions();
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
