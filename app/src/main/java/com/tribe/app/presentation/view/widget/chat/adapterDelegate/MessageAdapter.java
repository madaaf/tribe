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
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager<List<Message>> delegatesManager;
  private List<Message> items;

  //private MessageAdapterDelegate messageAdapterDelegate;

  private MessageEmojiAdapterDelegate messageEmojiAdapterDelegate;
  private MessageTextAdapterDelegate messageTextAdapterDelegate;
  private MessageImageAdapterDelegate messageImageAdapterDelegate;
  private MessageEventAdapterDelegate messageEventAdapterDelegate;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<List<Object>> onMessagePending = PublishSubject.create();
  // private PublishSubject<View> onMessagePending = PublishSubject.create();

  public MessageAdapter(Context context, int type) {
    delegatesManager = new RxAdapterDelegatesManager<>();

  /*  messageAdapterDelegate = new MessageAdapterDelegate(context);
    delegatesManager.addDelegate(messageAdapterDelegate);*/

    messageEmojiAdapterDelegate = new MessageEmojiAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageEmojiAdapterDelegate);

    messageTextAdapterDelegate = new MessageTextAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageTextAdapterDelegate);

    messageEventAdapterDelegate = new MessageEventAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageEventAdapterDelegate);

    messageImageAdapterDelegate = new MessageImageAdapterDelegate(context, type);
    delegatesManager.addDelegate(messageImageAdapterDelegate);

    items = new ArrayList<>();

    subscriptions.add(messageTextAdapterDelegate.onMessagePending().subscribe(onMessagePending));
    subscriptions.add(messageEmojiAdapterDelegate.onMessagePending().subscribe(onMessagePending));
    subscriptions.add(messageImageAdapterDelegate.onMessagePending().subscribe(onMessagePending));

   /* subscriptions.add(Observable.merge(messageEmojiAdapterDelegate.onMessagePending(),
        messageEmojiAdapterDelegate.onMessagePending(),
        messageImageAdapterDelegate.onMessagePending()).subscribe(onMessagePending));*/

  }

  @Override public int getItemViewType(int position) {
    return delegatesManager.getItemViewType(items, position);
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return delegatesManager.onCreateViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
  /*  delegatesManager.onBindViewHolder(items, position, holder);
    Timber.w("BIND HOLDER EMPTY");*/
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
    if (!payloads.isEmpty()) {
      Message m = (Message) payloads.get(0);
      Message last = items.get(items.size() - 1);
      last.setPending(false);
      last.setId(m.getId());
      Timber.w(" PLAYLOAD BINDING " + m.toString());
      delegatesManager.onBindViewHolder(items, holder, position, payloads);
    } else {
      delegatesManager.onBindViewHolder(items, position, holder);
    }
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public void setItems(Collection<Message> items) {
    //
    this.items.clear();
    this.items.addAll(items);
    super.notifyDataSetChanged();
  }

  public void setItems(List<Message> items, int position) {
    //some fictitious objectList where we're populating data
    for (Message obj : items) {
      this.items.add(position, obj);
    }
    super.notifyDataSetChanged();
  }

  public Message getMessage(int position) {
    return items.get(position);
  }

  public List<Message> getItems() {
    return items;
  }

  public Observable<List<Object>> onMessagePending() {
    return onMessagePending;
  }
}
