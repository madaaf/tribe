package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.RxAdapterDelegatesManager;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager<List<Message>> delegatesManager;
  private List<Message> items;
  private List<Integer> positionPendingMessage = new ArrayList<>();

  private Set<Message> treeSet = new TreeSet<>((o1, o2) -> {
    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
    DateTime d1 = parser.parseDateTime(o1.getCreationDate());
    DateTime d2 = parser.parseDateTime(o2.getCreationDate());
    return d1.compareTo(d2);
  });

  private MessageEmojiAdapterDelegate messageEmojiAdapterDelegate;
  private MessageTextAdapterDelegate messageTextAdapterDelegate;
  private MessageImageAdapterDelegate messageImageAdapterDelegate;
  private MessageEventAdapterDelegate messageEventAdapterDelegate;

  private CompositeSubscription subscriptions = new CompositeSubscription();

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

    items = new ArrayList<>();
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
      Message m = (Message) payloads.get(0);
/*      Message last = items.get(items.size() - 1);
      last.setPending(false);
      last.setId(m.getId());*/
      Timber.w("SOEF PLAYLOAD BINDING " + m.toString());
      delegatesManager.onBindViewHolder(items, holder, position, payloads);
    } else {
      delegatesManager.onBindViewHolder(items, position, holder);
      //Timber.w("SOEF PLAYLOAD BINDING " + ((Message)items.get(position)).toString());

    }
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public void setItems(Collection<Message> items) {
    this.items.clear();
    this.treeSet.addAll(items);
    this.items.addAll(
        new ArrayList<>(treeSet)); // Use tree set to put items in bottom or in top of the list
    //  Timber.e("SOEF ADD ITEM " + this.items.size());
    super.notifyDataSetChanged();
  }

  public int getIndexOfMessage(Message message) {
    for (Message m : items) {
      Timber.i(items.indexOf(m) + " " + m.toString());
    }
    return items.indexOf(message);
  }

  public Message getMessage(int position) {
    return items.get(position);
  }

  public List<Message> getItems() {
    return items;
  }

  public void updateItem(int position, Message message) {
    Message pendingItem = items.get(position);
    Timber.w("SOEF UPDATE ITEM : " + position + " " + pendingItem.toString());
    pendingItem.setId(message.getId());
    pendingItem.setPending(false);
    if (position != -1) notifyItemChanged(position, pendingItem);
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
}
