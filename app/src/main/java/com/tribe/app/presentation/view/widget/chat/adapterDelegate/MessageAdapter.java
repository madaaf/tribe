package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.RxAdapterDelegatesManager;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import rx.Observable;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager<List<Message>> delegatesManager;
  private List<Message> items;
  private List<Integer> positionPendingMessage = new ArrayList<>();

  private MessageEmojiAdapterDelegate messageEmojiAdapterDelegate;
  private MessageTextAdapterDelegate messageTextAdapterDelegate;
  private MessageImageAdapterDelegate messageImageAdapterDelegate;
  private MessageEventAdapterDelegate messageEventAdapterDelegate;
  private MessageAudioAdapterDelegate messageAudioAdapterDelegate;

  public MessageAdapter(Context context, int type) {
    delegatesManager = new RxAdapterDelegatesManager<>();

    messageEmojiAdapterDelegate = new MessageEmojiAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageEmojiAdapterDelegate);

    messageTextAdapterDelegate = new MessageTextAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageTextAdapterDelegate);

    messageEventAdapterDelegate = new MessageEventAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageEventAdapterDelegate);

    messageImageAdapterDelegate = new MessageImageAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageImageAdapterDelegate);

    messageAudioAdapterDelegate = new MessageAudioAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageAudioAdapterDelegate);

    items = new ArrayList<>();
  }

  public void setArrIds(String[] arrIds) {
    messageImageAdapterDelegate.setArrIds(arrIds);
  }

  @Override public int getItemViewType(int position) {
    return delegatesManager.getItemViewType(items, position);
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return delegatesManager.onCreateViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    // delegatesManager.onBindViewHolder(items, position, holder);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
    if (!payloads.isEmpty()) {
      Message newMessage = (Message) payloads.get(0);
      Message m = items.get(position);
      m.setPending(false);
      m.setId(newMessage.getId());
      delegatesManager.onBindViewHolder(items, holder, position, payloads);
    } else {
      delegatesManager.onBindViewHolder(items, position, holder);
    }
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public void setItems(Collection<Message> items, int position) {
    this.items.addAll(position, items);
    super.notifyDataSetChanged();
  }

  public void clearItem() {
    this.items.clear();
    super.notifyDataSetChanged();
  }

  public void setItem(Message message) {
    this.items.add(message);
    super.notifyDataSetChanged();
  }

  public int getIndexOfMessage(Message message) {
    return items.indexOf(message);
  }

  public Message getItem(int index) {
    return items.get(index);
  }

  public Message getMessage(int position) {
    return items.get(position);
  }

  public List<Message> getItems() {
    return items;
  }

  public void updateItem(int position, Message message) {
    Message pendingItem = items.get(position);
    pendingItem.setId(message.getId());
    pendingItem.setPending(false);
    if (position != -1) notifyItemChanged(position);
  }

  public void removeItem(Message message) {
    int index = getIndexOfMessage(message);
    items.remove(message);
    notifyItemRemoved(index);
  }

  public int getPendingMessage() {
    positionPendingMessage = new ArrayList<>();
    for (Message m : items) {
      if (m.getId().equals(Message.PENDING)) {
        positionPendingMessage.add(items.indexOf(m));
      }
    }
    return (positionPendingMessage.size() - 1);
  }

  public Observable<List<Object>> onClickItem() {
    return Observable.merge(messageTextAdapterDelegate.onPopulateSCLastSeen(),
        messageEventAdapterDelegate.onPopulateSCLastSeen(),
        messageImageAdapterDelegate.onPopulateSCLastSeen(),
        messageEmojiAdapterDelegate.onPopulateSCLastSeen(),
        messageAudioAdapterDelegate.onPopulateSCLastSeen());
  }

  public Observable<Message> onLongClickItem() {
    return Observable.merge(messageTextAdapterDelegate.onLongClickItem(),
        messageEventAdapterDelegate.onLongClickItem(),
        messageImageAdapterDelegate.onLongClickItem(),
        messageEmojiAdapterDelegate.onLongClickItem(),
        messageAudioAdapterDelegate.onLongClickItem());
  }

  public void onStartRecording() {
    messageAudioAdapterDelegate.stopListenVoiceNote();
  }
}

