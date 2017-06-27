package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.adapter.delegate.grid.CallRouletteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyHeaderGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.MoreFriendsAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserConnectedGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserLiveGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.VideoDemoAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.RecyclerViewItemEnabler;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class HomeGridAdapter extends RecyclerView.Adapter implements RecyclerViewItemEnabler {

  public static final int EMPTY_HEADER_VIEW_TYPE = 99;

  protected RxAdapterDelegatesManager delegatesManager;
  private UserGridAdapterDelegate userGridAdapterDelegate;
  private UserLiveGridAdapterDelegate userLiveGridAdapterDelegate;
  private UserConnectedGridAdapterDelegate userConnectedGridAdapterDelegate;
  private MoreFriendsAdapterDelegate moreFriendsAdapterDelegate;
  private VideoDemoAdapterDelegate videoDemoAdapterDelegate;


  // VARIABLES
  private List<Recipient> items;
  private boolean allEnabled = true;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public HomeGridAdapter(Context context) {
    delegatesManager = new RxAdapterDelegatesManager<>();
    delegatesManager.addDelegate(new EmptyGridAdapterDelegate(context, true, false));
    delegatesManager.addDelegate(EMPTY_HEADER_VIEW_TYPE,
        new EmptyHeaderGridAdapterDelegate(context));

    userGridAdapterDelegate = new UserGridAdapterDelegate(context);
    delegatesManager.addDelegate(userGridAdapterDelegate);

    userLiveGridAdapterDelegate = new UserLiveGridAdapterDelegate(context);
    delegatesManager.addDelegate(userLiveGridAdapterDelegate);

    userConnectedGridAdapterDelegate = new UserConnectedGridAdapterDelegate(context);
    delegatesManager.addDelegate(userConnectedGridAdapterDelegate);

    moreFriendsAdapterDelegate = new MoreFriendsAdapterDelegate(context, true);
    delegatesManager.addDelegate(moreFriendsAdapterDelegate);

    videoDemoAdapterDelegate = new VideoDemoAdapterDelegate(context, true);
    delegatesManager.addDelegate(videoDemoAdapterDelegate);

    items = new ArrayList<>();

    setHasStableIds(true);
  }

  @Override public long getItemId(int position) {
    Recipient recipient = getItemAtPosition(position);
    return recipient.hashCode();
  }

  @Override public int getItemViewType(int position) {
    return delegatesManager.getItemViewType(items, position);
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return delegatesManager.onCreateViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    holder.itemView.setEnabled(isAllItemsEnabled());
    delegatesManager.onBindViewHolder(items, position, holder);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
    holder.itemView.setEnabled(isAllItemsEnabled());
    delegatesManager.onBindViewHolder(items, holder, position, payloads);
  }

  public void releaseSubscriptions() {
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    delegatesManager.releaseSubscriptions();
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public Observable<View> onClickMore() {
    return Observable.merge(userGridAdapterDelegate.onClickMore(),
        userConnectedGridAdapterDelegate.onClickMore(), userLiveGridAdapterDelegate.onClickMore());
  }

  public Observable<View> onClick() {
    return Observable.merge(userGridAdapterDelegate.onClick(),
        userLiveGridAdapterDelegate.onClick(), userConnectedGridAdapterDelegate.onClick(),
        moreFriendsAdapterDelegate.onClick(), videoDemoAdapterDelegate.onClick());
  }

  public Observable<View> onLongClick() {
    return Observable.merge(userGridAdapterDelegate.onLongClick(),
        userLiveGridAdapterDelegate.onLongClick(), userConnectedGridAdapterDelegate.onLongClick());
  }

  public void setItems(List<Recipient> items) {
    this.items.clear();
    this.items.addAll(items);
  }

  public Recipient getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<Recipient> getItems() {
    return items;
  }

  public void setAllItemsEnabled(boolean enable) {
    allEnabled = enable;
    notifyItemRangeChanged(0, getItemCount());
  }

  @Override public boolean isAllItemsEnabled() {
    return allEnabled;
  }

  @Override public boolean getItemEnabled(int position) {
    return true;
  }
}
