package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.adapter.delegate.friend.ManageShortcutListAdapterDelegate;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 05/11/2016.
 */
public class ShortcutsAdapter extends RecyclerView.Adapter {

  // DELEGATES
  protected RxAdapterDelegatesManager delegatesManager;
  private ManageShortcutListAdapterDelegate manageShortcutListAdapterDelegate;

  // VARIABLES
  private List<Shortcut> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public ShortcutsAdapter(Context context) {
    items = new ArrayList<>();

    delegatesManager = new RxAdapterDelegatesManager();

    manageShortcutListAdapterDelegate = new ManageShortcutListAdapterDelegate(context);
    delegatesManager.addDelegate(manageShortcutListAdapterDelegate);

    setHasStableIds(true);
  }

  @Override public long getItemId(int position) {
    Object obj = getItemAtPosition(position);
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

  public Shortcut getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<Shortcut> getItems() {
    return items;
  }

  public void setItems(List<Shortcut> items) {
    this.items.clear();
    this.items.addAll(items);
    this.notifyDataSetChanged();
  }

  public void remove(Shortcut shortcut) {
    int indexOf = items.indexOf(shortcut);

    if (indexOf != -1) {
      items.remove(shortcut);
      notifyItemRemoved(indexOf);
    }
  }

  public void reset(Shortcut shortcut) {
    int indexOf = items.indexOf(shortcut);

    if (indexOf != -1) {
      notifyItemChanged(indexOf);
    }
  }

  public Observable<View> onClickMute() {
    return manageShortcutListAdapterDelegate.onClickMute();
  }

  public Observable<View> onClickRemove() {
    return manageShortcutListAdapterDelegate.onClickRemove();
  }
}