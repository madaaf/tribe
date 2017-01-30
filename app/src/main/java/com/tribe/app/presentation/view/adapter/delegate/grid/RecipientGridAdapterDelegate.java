package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.component.TileView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 18/05/2016.
 */
public abstract class RecipientGridAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

  protected LayoutInflater layoutInflater;
  protected Context context;

  // RX SUBSCRIPTIONS / SUBJECTS
  protected final PublishSubject<View> clickMoreView = PublishSubject.create();
  protected final PublishSubject<View> click = PublishSubject.create();

  public RecipientGridAdapterDelegate(Context context) {
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    RecipientGridViewHolder recipientGridViewHolder =
        new RecipientGridViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));

    recipientGridViewHolder.viewTile.initClicks();
    recipientGridViewHolder.viewTile.onClickMore().subscribe(clickMoreView);
    recipientGridViewHolder.viewTile.onClick().subscribe(click);

    return recipientGridViewHolder;
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    RecipientGridViewHolder vh = (RecipientGridViewHolder) holder;
    Recipient recipient = items.get(position);

    vh.viewTile.setInfo(recipient);
    vh.viewTile.setBackground(position);
  }

  @Override public void onBindViewHolder(@NonNull List<Recipient> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    RecipientGridViewHolder vh = (RecipientGridViewHolder) holder;
    Recipient recipient = items.get(position);

    if (payloads.isEmpty()) {
      onBindViewHolder(items, position, vh);
    } else {
      vh.viewTile.setRecipient(recipient);
      vh.viewTile.setBackground(position);

      Bundle o = (Bundle) payloads.get(0);

      for (String key : o.keySet()) {
        if (key.equals(Recipient.IS_ONLINE) || key.equals(Recipient.IS_LIVE)) {
          vh.viewTile.setStatus();
        }

        if (key.equals(Recipient.DISPLAY_NAME)) {
          vh.viewTile.setName();
        }

        if (key.equals(Recipient.PROFILE_PICTURE)) {
          vh.viewTile.setAvatar();
        }
      }
    }
  }

  public Observable<View> onClickMore() {
    return clickMoreView;
  }

  public Observable<View> onClick() {
    return click;
  }

  protected abstract int getLayoutId();

  static class RecipientGridViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewTile) public TileView viewTile;

    public RecipientGridViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
