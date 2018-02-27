package com.tribe.app.presentation.view.adapter.delegate.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.RecipientHomeViewHolder;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 09/04/2017
 */
public abstract class RecipientAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

  protected LayoutInflater layoutInflater;
  protected Context context;
  protected boolean hasChat = true;

  // RX SUBSCRIPTIONS / SUBJECTS
  protected final PublishSubject<View> clickMoreView = PublishSubject.create();
  protected final PublishSubject<View> click = PublishSubject.create();
  protected final PublishSubject<View> longClick = PublishSubject.create();
  protected final PublishSubject<View> onChatClick = PublishSubject.create();
  protected final PublishSubject<View> onLiveClick = PublishSubject.create();
  protected final PublishSubject<View> onMainClick = PublishSubject.create();

  public RecipientAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecipientHomeViewHolder recipientGridViewHolder =
        new RecipientHomeViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));
    recipientGridViewHolder.viewListItem.onLongClick()
        .map(view -> recipientGridViewHolder.itemView)
        .subscribe(longClick);
    recipientGridViewHolder.viewListItem.onChatClick()
        .map(view -> recipientGridViewHolder.itemView)
        .subscribe(onChatClick);
    recipientGridViewHolder.viewListItem.onLiveClick()
        .map(view -> recipientGridViewHolder.itemView)
        .subscribe(onLiveClick);
    recipientGridViewHolder.viewListItem.onMainClick()
        .map(view -> recipientGridViewHolder.itemView)
        .subscribe(onMainClick);
    recipientGridViewHolder.viewListItem.setHasChat(hasChat);
    return recipientGridViewHolder;
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    bind((RecipientHomeViewHolder) holder, items.get(position));
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    bind((RecipientHomeViewHolder) holder, items.get(position));
  }

  private void bind(RecipientHomeViewHolder vh, Recipient recipient) {
    vh.viewListItem.setRecipient(recipient, vh);
    vh.viewListItem.setHasChat(hasChat);
  }

  public void setHasChat(boolean hasChat) {
    this.hasChat = hasChat;
  }

  public Observable<View> onClickMore() {
    return clickMoreView;
  }

  public Observable<View> onClick() {
    return click;
  }

  public Observable<View> onChatClick() {
    return onChatClick;
  }

  public Observable<View> onLongClick() {
    return longClick;
  }

  public Observable<View> onLiveClick() {
    return onLiveClick;
  }

  public Observable<View> onMainClick() {
    return onMainClick;
  }

  protected abstract int getLayoutId();
}
