package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.view.adapter.delegate.text.EmojiMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.LinkMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.MeMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.OtherHeaderMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.PhotoMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.RegularMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.TodayHeaderMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.TutorialMessageAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.text.VideoMessageAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class MessageAdapter extends RecyclerView.Adapter {

    private RxAdapterDelegatesManager<List<ChatMessage>> delegatesManager;
    private List<ChatMessage> items;
    private ChatMessage oldestChatMessage;

    // DELEGATES
    private PhotoMessageAdapterDelegate photoMessageAdapterDelegate;
    private VideoMessageAdapterDelegate videoMessageAdapterDelegate;

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

        photoMessageAdapterDelegate = new PhotoMessageAdapterDelegate(inflater, context);
        delegatesManager.addDelegate(photoMessageAdapterDelegate);

        videoMessageAdapterDelegate = new VideoMessageAdapterDelegate(inflater, context);
        delegatesManager.addDelegate(videoMessageAdapterDelegate);

        items = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public long getItemId(int position) {
        ChatMessage chatMessage = getMessage(position);
        return chatMessage.hashCode();
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

    public void setItems(List<ChatMessage> chatMessageList) {
        if (!items.isEmpty()) {
            oldestChatMessage = items.get(0);
        }

        items.clear();
        items.addAll(chatMessageList);
        notifyDataSetChanged();
    }

    public List<ChatMessage> getItems() {
        return items;
    }

    public void addItem(ChatMessage chatMessage) {
        if (!items.isEmpty()) {
            oldestChatMessage = items.get(items.size() - 1);
        }

        items.add(chatMessage);
        notifyDataSetChanged();
    }

    public ChatMessage getMessage(int position) {
        return items.get(position);
    }

    public ChatMessage getOldestChatMessage() {
        return oldestChatMessage;
    }

    public int getIndexOfMessage(ChatMessage chatMessage) {
        return items.indexOf(chatMessage);
    }

    public Observable<ImageView> clickPhoto() {
        return photoMessageAdapterDelegate.clickPhoto();
    }

    public Observable<ImageView> clickVideo() {
        return videoMessageAdapterDelegate.clickVideo();
    }
}
