package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.presentation.view.adapter.delegate.live.LiveGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.diff.UserLiveDiffCallback;
import com.tribe.app.presentation.view.adapter.viewmodel.UserLive;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/29/2016.
 */
public class LiveGridAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager delegatesManager;
  private LiveGridAdapterDelegate liveGridAdapterDelegate;

  // VARIABLES
  private List<UserLive> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public LiveGridAdapter(Context context) {
    delegatesManager = new RxAdapterDelegatesManager<>();

    liveGridAdapterDelegate = new LiveGridAdapterDelegate(context);
    delegatesManager.addDelegate(liveGridAdapterDelegate);

    items = new ArrayList<>();

    setHasStableIds(true);
  }

  @Override public long getItemId(int position) {
    UserLive userLive = getItemAtPosition(position);
    System.out.println("Position : " + position + " Hash code : " + userLive.getUser().hashCode());
    return userLive.getUser().hashCode();
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

  public Observable<View> onClick() {
    return liveGridAdapterDelegate.onClick();
  }

  public void setItems(List<UserLive> items) {
    final UserLiveDiffCallback diffCallback = new UserLiveDiffCallback(this.items, items);
    final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

    liveGridAdapterDelegate.setCount(items.size());

    this.items.clear();
    this.items.addAll(items);
    diffResult.dispatchUpdatesTo(this);
  }

  public UserLive getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<UserLive> getItems() {
    return items;
  }

  public void setScreenHeight(int screenHeight) {
    liveGridAdapterDelegate.setScreenHeight(screenHeight);
  }
}
