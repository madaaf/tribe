package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.leaderboard.LeaderboardUserAdapterDelegate;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/20/2017.
 */
public class LeaderboardUserAdapter extends RecyclerView.Adapter {

  // DELEGATES
  private RxAdapterDelegatesManager delegatesManager;
  private LeaderboardUserAdapterDelegate leaderboardUserAdapterDelegate;

  // VARIABLES
  private List<Score> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public LeaderboardUserAdapter(Context context) {
    items = new ArrayList<>();

    delegatesManager = new RxAdapterDelegatesManager();

    leaderboardUserAdapterDelegate = new LeaderboardUserAdapterDelegate(context);
    delegatesManager.addDelegate(leaderboardUserAdapterDelegate);

    setHasStableIds(true);
  }

  @Override public long getItemId(int position) {
    Score obj = getItemAtPosition(position);
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

  public Score getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<Score> getItems() {
    return items;
  }

  public void setItems(List<Score> items) {
    this.items.clear();
    this.items.addAll(items);

    this.notifyDataSetChanged();
  }

  public void clear() {
    this.items.clear();
    this.notifyDataSetChanged();
  }

  public void setCanClick(boolean canClick) {
    leaderboardUserAdapterDelegate.setCanClick(canClick);
  }

  public void setUser(User user) {
    leaderboardUserAdapterDelegate.setUser(user);
  }

  /**
   * OBSERVABLES
   */

  public Observable<View> onClick() {
    return leaderboardUserAdapterDelegate.onClick();
  }
}
