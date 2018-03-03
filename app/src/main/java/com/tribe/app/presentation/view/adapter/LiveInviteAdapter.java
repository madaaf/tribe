package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.presentation.view.adapter.delegate.EmptyHeaderInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.LiveInviteHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.LiveInviteSubHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShareAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutEmptyInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutInviteFullAdapterDelegate;
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
  public static final int HEADER_VIEW_TYPE = 98;
  public static final int SHORTCUT_FULL = 97;
  public static final int SHORTCUT_PARTIAL = 96;

  protected RxAdapterDelegatesManager delegatesManager;
  private UserRoomAdapterDelegate userRoomAdapterDelegate;
  private ShortcutInviteAdapterDelegate shortcutInviteAdapterDelegate;
  private ShortcutInviteFullAdapterDelegate shortcutInviteFullAdapterDelegate;
  private ShortcutEmptyInviteAdapterDelegate shortcutEmptyInviteAdapterDelegate;
  private LiveInviteHeaderAdapterDelegate liveInviteHeaderAdapterDelegate;
  private LiveInviteSubHeaderAdapterDelegate liveInviteSubHeaderAdapterDelegate;
  private ShareAdapterDelegate shareAdapterDelegate;

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

    shortcutInviteFullAdapterDelegate = new ShortcutInviteFullAdapterDelegate(context);
    shortcutInviteAdapterDelegate = new ShortcutInviteAdapterDelegate(context);
    delegatesManager.addDelegate(SHORTCUT_PARTIAL, shortcutInviteAdapterDelegate);

    shortcutEmptyInviteAdapterDelegate = new ShortcutEmptyInviteAdapterDelegate(context);
    delegatesManager.addDelegate(shortcutEmptyInviteAdapterDelegate);

    liveInviteHeaderAdapterDelegate = new LiveInviteHeaderAdapterDelegate(context);
    delegatesManager.addDelegate(HEADER_VIEW_TYPE, liveInviteHeaderAdapterDelegate);

    liveInviteSubHeaderAdapterDelegate = new LiveInviteSubHeaderAdapterDelegate(context);
    delegatesManager.addDelegate(liveInviteSubHeaderAdapterDelegate);

    shareAdapterDelegate = new ShareAdapterDelegate(context);
    delegatesManager.addDelegate(shareAdapterDelegate);

    items = new ArrayList<>();

    setHasStableIds(true);
  }

  @Override public long getItemId(int position) {
    LiveInviteAdapterSectionInterface object = getItemAtPosition(position);

    if (object != null) {
      return object.hashCode();
    }

    return 0L;
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

  public void removeItem(int position) {
    items.remove(position);
    notifyItemRemoved(position);
    notifyItemRangeChanged(position, items.size());
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
    subscriptions.add(obs.subscribe(width -> {
      shortcutInviteAdapterDelegate.updateWidth(width);
      shortcutInviteFullAdapterDelegate.updateWidth(width);
      shortcutEmptyInviteAdapterDelegate.updateWidth(width);
      shareAdapterDelegate.updateWidth(width);
    }));
  }

  public void setFullMode() {
    delegatesManager.removeDelegate(shortcutInviteAdapterDelegate);
    delegatesManager.removeDelegate(SHORTCUT_FULL);
    delegatesManager.addDelegate(SHORTCUT_FULL, shortcutInviteFullAdapterDelegate);
  }

  public void setPartialMode() {
    delegatesManager.removeDelegate(shortcutInviteFullAdapterDelegate);
    delegatesManager.removeDelegate(SHORTCUT_PARTIAL);
    delegatesManager.addDelegate(SHORTCUT_PARTIAL, shortcutInviteAdapterDelegate);
  }

  // OBSERVABLES

  public Observable<View> onClick() {
    return Observable.merge(shortcutInviteAdapterDelegate.onClick(),
        shortcutEmptyInviteAdapterDelegate.onClick());
  }

  public Observable<View> onClickEdit() {
    return liveInviteHeaderAdapterDelegate.onClickEdit();
  }

  public Observable<String> onShareLink() {
    return shareAdapterDelegate.onClick();
  }
}
