package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.delegate.EmptyHeaderInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.RoomLinkAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserRoomAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveInviteAdapter extends RecyclerView.Adapter {

  public static final int EMPTY_HEADER_VIEW_TYPE = 99;

  protected RxAdapterDelegatesManager delegatesManager;
  private UserRoomAdapterDelegate userRoomAdapterDelegate;
  private RoomLinkAdapterDelegate roomLinkAdapterDelegate;
  private ShortcutInviteAdapterDelegate shortcutInviteAdapterDelegate;

  // VARIABLES
  private List<LiveInviteAdapterSectionInterface> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public LiveInviteAdapter(Context context) {
    delegatesManager = new RxAdapterDelegatesManager<>();

    delegatesManager.addDelegate(EMPTY_HEADER_VIEW_TYPE,
        new EmptyHeaderInviteAdapterDelegate(context));

    userRoomAdapterDelegate = new UserRoomAdapterDelegate(context);
    delegatesManager.addDelegate(userRoomAdapterDelegate);

    roomLinkAdapterDelegate = new RoomLinkAdapterDelegate(context);
    delegatesManager.addDelegate(roomLinkAdapterDelegate);

    shortcutInviteAdapterDelegate = new ShortcutInviteAdapterDelegate(context);
    delegatesManager.addDelegate(shortcutInviteAdapterDelegate);

    items = new ArrayList<>();

    setHasStableIds(true);
  }

  @Override public long getItemId(int position) {
    LiveInviteAdapterSectionInterface object = getItemAtPosition(position);
    return object.hashCode();
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

  public void setItems(List<LiveInviteAdapterSectionInterface> items) {
    this.items.clear();
    this.items.addAll(items);
  }

  public LiveInviteAdapterSectionInterface getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<LiveInviteAdapterSectionInterface> getItems() {
    return items;
  }

  public void initInviteViewWidthChange(Observable<Integer> obs) {
    subscriptions.add(obs.subscribe(width -> shortcutInviteAdapterDelegate.updateWidth(width)));
  }

  // OBSERVABLES

  public Observable<Void> onShareLink() {
    return roomLinkAdapterDelegate.onShareLink();
  }
}
