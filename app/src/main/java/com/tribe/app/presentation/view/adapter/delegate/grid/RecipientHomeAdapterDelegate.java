package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 09/04/2017
 */
public abstract class RecipientHomeAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

  protected LayoutInflater layoutInflater;
  protected Context context;

  // RX SUBSCRIPTIONS / SUBJECTS
  protected final PublishSubject<View> clickMoreView = PublishSubject.create();
  protected final PublishSubject<View> click = PublishSubject.create();
  protected final PublishSubject<View> longClick = PublishSubject.create();

  public RecipientHomeAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecipientHomeViewHolder recipientGridViewHolder =
        new RecipientHomeViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));

    return recipientGridViewHolder;
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    RecipientHomeViewHolder vh = (RecipientHomeViewHolder) holder;
    Recipient recipient = items.get(position);
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    RecipientHomeViewHolder vh = (RecipientHomeViewHolder) holder;
    Recipient recipient = items.get(position);
  }

  public Observable<View> onClickMore() {
    return clickMoreView;
  }

  public Observable<View> onClick() {
    return click;
  }

  public Observable<View> onLongClick() {
    return longClick;
  }

  protected abstract int getLayoutId();

  static class RecipientHomeViewHolder extends RecyclerView.ViewHolder {

    public RecipientHomeViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
