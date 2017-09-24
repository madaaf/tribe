package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.RxAdapterDelegatesManager;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
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

  public MessageAdapter(Context context) {
    delegatesManager = new RxAdapterDelegatesManager<>();

  /*  messageAdapterDelegate = new MessageAdapterDelegate(context);
    delegatesManager.addDelegate(messageAdapterDelegate);*/

    messageEmojiAdapterDelegate = new MessageEmojiAdapterDelegate(context);
    delegatesManager.addDelegate(messageEmojiAdapterDelegate);

    messageTextAdapterDelegate = new MessageTextAdapterDelegate(context);
    delegatesManager.addDelegate(messageTextAdapterDelegate);

    messageEventAdapterDelegate = new MessageEventAdapterDelegate(context);
    delegatesManager.addDelegate(messageEventAdapterDelegate);

    messageImageAdapterDelegate = new MessageImageAdapterDelegate(context);
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
    delegatesManager.onBindViewHolder(items, position, holder);
    Timber.w("BIND HOLDER EMPTY");
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
    if (!payloads.isEmpty()) {
      Timber.e("BIND PLAYLOAD ");
      delegatesManager.onBindViewHolder(items, holder, position, payloads);
    } else {
      Timber.w("BIND PLAYLOAD EMPTY");
      delegatesManager.onBindViewHolder(items, position, holder);
    }
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public int getPositionOfItem(Message m) {
    return 0;
  }

  public void setItems(Collection<Message> items) {
    this.items.addAll(items);
    super.notifyDataSetChanged();
  }

  public List<Message> getItems() {
    return items;
  }

  public Observable<List<Object>> onMessagePending() {
    return onMessagePending;
  }
}
