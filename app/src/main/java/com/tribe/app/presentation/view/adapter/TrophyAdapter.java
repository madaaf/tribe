package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.TrophyEnum;
import com.tribe.app.presentation.view.adapter.delegate.trophy.TrophyAdapterDelegate;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/20/2017.
 */
public class TrophyAdapter extends RecyclerView.Adapter {

  // DELEGATES
  private RxAdapterDelegatesManager delegatesManager;
  private TrophyAdapterDelegate trophyAdapterDelegate;

  // VARIABLES
  private List<TrophyEnum> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public TrophyAdapter(Context context) {
    items = new ArrayList<>();

    delegatesManager = new RxAdapterDelegatesManager();

    trophyAdapterDelegate = new TrophyAdapterDelegate(context);
    delegatesManager.addDelegate(trophyAdapterDelegate);

    setHasStableIds(true);
  }

  @Override public long getItemId(int position) {
    TrophyEnum obj = getItemAtPosition(position);
    return obj.hashCode();
  }

  @Override public int getItemViewType(int position) {
    return delegatesManager.getItemViewType(items, position);
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return delegatesManager.onCreateViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    delegatesManager.onBindViewHolder(items, position, holder);
  }

  public void releaseSubscriptions() {
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    delegatesManager.releaseSubscriptions();
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public TrophyEnum getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<TrophyEnum> getItems() {
    return items;
  }

  public void setItems(List<TrophyEnum> items) {
    this.items.clear();
    this.items.addAll(items);

    this.notifyDataSetChanged();
  }

  public void clear() {
    this.items.clear();
    this.notifyDataSetChanged();
  }

  /**
   * OBSERVABLES
   */

  public Observable<View> onClick() {
    return trophyAdapterDelegate.onClick();
  }
}
