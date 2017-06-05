package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.delegate.gamesfilters.FilterAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.gamesfilters.GameAdapterDelegate;
import com.tribe.tribelivesdk.entity.GameFilter;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * Created by tiago on 06/02/17.
 */
public class GamesFiltersAdapter extends RecyclerView.Adapter {

  protected RxAdapterDelegatesManager<List<GameFilter>> delegatesManager;
  private GameAdapterDelegate gameAdapterDelegate;
  private FilterAdapterDelegate filterAdapterDelegate;

  private List<GameFilter> items;

  public GamesFiltersAdapter(Context context) {
    delegatesManager = new RxAdapterDelegatesManager<>();

    gameAdapterDelegate = new GameAdapterDelegate(context);
    delegatesManager.addDelegate(gameAdapterDelegate);

    filterAdapterDelegate = new FilterAdapterDelegate(context);
    delegatesManager.addDelegate(filterAdapterDelegate);

    items = new ArrayList<>();

    setHasStableIds(true);
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

  @Override public int getItemCount() {
    return items.size();
  }

  @Override public long getItemId(int position) {
    GameFilter gameFilter = getItemAtPosition(position);
    return gameFilter.hashCode();
  }

  public void releaseSubscriptions() {
    delegatesManager.releaseSubscriptions();
  }

  public void setItems(List<GameFilter> items) {
    this.items.clear();
    this.items.addAll(items);
    this.notifyDataSetChanged();
  }

  public GameFilter getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<GameFilter> getItems() {
    return items;
  }

  public void updateSelected(GameFilter selectedGameFilter) {
    for (GameFilter gameFilter : items) {
      if (gameFilter.getId().equals(selectedGameFilter.getId())) {
        gameFilter.setActivated(!selectedGameFilter.isActivated());
      } else {
        gameFilter.setActivated(false);
      }
    }
    notifyDataSetChanged();
  }

  public Observable<View> onClick() {
    return Observable.merge(filterAdapterDelegate.onClick(), gameAdapterDelegate.onClick());
  }
}
