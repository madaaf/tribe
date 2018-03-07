package com.tribe.app.presentation.view.adapter.viewholder;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.presentation.view.adapter.RxAdapterDelegatesManager;
import com.tribe.app.presentation.view.adapter.delegate.common.LoadMoreAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.leaderboard.LeaderboardDetailsAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.leaderboard.LeaderboardDetailsEmptyAdapterDelegate;
import com.tribe.app.presentation.view.adapter.helper.EndlessRecyclerViewScrollListener;
import com.tribe.app.presentation.view.adapter.model.LoadMoreModel;
import com.tribe.app.presentation.view.utils.StateManager;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/20/2017.
 */
public class LeaderboardDetailsAdapter extends RecyclerView.Adapter {

  // DELEGATES
  private RxAdapterDelegatesManager delegatesManager;
  private LeaderboardDetailsAdapterDelegate leaderboardDetailsAdapterDelegate;
  private LeaderboardDetailsEmptyAdapterDelegate leaderboardDetailsEmptyAdapterDelegate;
  private LoadMoreAdapterDelegate loadMoreAdapterDelegate;

  // VARIABLES
  private List<Object> items;
  private EndlessRecyclerViewScrollListener scrollListener;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onLoadMore = PublishSubject.create();

  @Inject public LeaderboardDetailsAdapter(Context context, RecyclerView recyclerView,
      StateManager stateManager) {
    items = new ArrayList<>();

    delegatesManager = new RxAdapterDelegatesManager();

    leaderboardDetailsAdapterDelegate = new LeaderboardDetailsAdapterDelegate(context, stateManager);
    delegatesManager.addDelegate(leaderboardDetailsAdapterDelegate);

    leaderboardDetailsEmptyAdapterDelegate = new LeaderboardDetailsEmptyAdapterDelegate(context);
    delegatesManager.addDelegate(leaderboardDetailsEmptyAdapterDelegate);

    loadMoreAdapterDelegate = new LoadMoreAdapterDelegate(context);
    delegatesManager.addDelegate(loadMoreAdapterDelegate);

    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
      final LinearLayoutManager linearLayoutManager =
          (LinearLayoutManager) recyclerView.getLayoutManager();

      scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
        @Override public void onLoadMore(int page, int totalItemsCount, RecyclerView view,
            boolean downwards) {
          onLoadMore.onNext(downwards);
        }
      };

      recyclerView.addOnScrollListener(scrollListener);
    }
    setHasStableIds(true);
  }

  @Override public long getItemId(int position) {
    Object obj = getItemAtPosition(position);
    if (obj != null) {
      return obj.hashCode();
    } else {
      return -1000;
    }
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

  public Object getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<Object> getItems() {
    return items;
  }

  public void setItems(List<Score> items) {
    this.items.clear();
    this.items.addAll(items);

    this.notifyDataSetChanged();
  }

  public void addItems(List<Score> items) {
    int range = items.size();
    this.items.addAll(items);
    this.notifyItemRangeInserted(range, items.size());
  }

  public void addItems(int position, List<Score> items) {
    this.items.addAll(position, items);
    this.notifyItemRangeInserted(0, items.size());
  }

  public void addItem(Object object) {
    this.items.add(object);
    this.notifyItemInserted(this.items.size() - 1);
  }

  public void clear() {
    this.items.clear();
  }

  public void showProgress() {
    addItem(new LoadMoreModel());
  }

  public void hideProgress() {
    if (this.items.size() == 0) return;
    Object obj = this.items.get(this.items.size() - 1);
    if (obj instanceof LoadMoreModel) {
      this.items.remove(obj);
      notifyItemRemoved(this.items.size() - 1);
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<View> onClick() {
    return leaderboardDetailsAdapterDelegate.onClick();
  }

  public Observable<Score> onClickPoke() {
    return leaderboardDetailsAdapterDelegate.onClickPoke();
  }

  public Observable<Boolean> onLoadMore() {
    return onLoadMore;
  }

  public void onPoke(boolean b) {
    leaderboardDetailsAdapterDelegate.onPoke(b);
    notifyDataSetChanged();
  }
}
