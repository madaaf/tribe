package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.adapter.delegate.common.ShortcutAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactToInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.UserToAddAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutChatActiveHomeAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutEmptyListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.ShortcutLiveHomeAdapterDelegate;
import com.tribe.app.presentation.view.adapter.helper.ItemTouchHelperAdapter;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.adapter.interfaces.RecyclerViewItemEnabler;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 18/05/2016.
 */
public class HomeListAdapter extends RecyclerView.Adapter
    implements RecyclerViewItemEnabler, ItemTouchHelperAdapter {

  public static final int EMPTY_HEADER_VIEW_TYPE = 99;
  public static final int HEADERS_VIEW_TYPE = 98;
  public static final int EMPTY_VIEW_TYPE = 97;

  protected RxAdapterDelegatesManager delegatesManager;
  private ShortcutAdapterDelegate shortcutHomeAdapterDelegate;
  private ShortcutLiveHomeAdapterDelegate shortcutLiveHomeAdapterDelegate;
  private ShortcutChatActiveHomeAdapterDelegate shortcutChatActiveHomeAdapterDelegate;
  private UserToAddAdapterDelegate userToAddAdapterDelegate;
  private ContactToInviteAdapterDelegate contactToInviteAdapterDelegate;

  // VARIABLES
  private List<HomeAdapterInterface> items;
  private boolean allEnabled = true;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public HomeListAdapter(Context context) {
    delegatesManager = new RxAdapterDelegatesManager<>();
    delegatesManager.addDelegate(EMPTY_VIEW_TYPE, new ShortcutEmptyListAdapterDelegate(context));

    shortcutHomeAdapterDelegate = new ShortcutAdapterDelegate(context);
    delegatesManager.addDelegate(shortcutHomeAdapterDelegate);

    shortcutLiveHomeAdapterDelegate = new ShortcutLiveHomeAdapterDelegate(context);
    delegatesManager.addDelegate(shortcutLiveHomeAdapterDelegate);

    shortcutChatActiveHomeAdapterDelegate = new ShortcutChatActiveHomeAdapterDelegate(context);
    delegatesManager.addDelegate(shortcutChatActiveHomeAdapterDelegate);

    userToAddAdapterDelegate = new UserToAddAdapterDelegate(context);
    delegatesManager.addDelegate(userToAddAdapterDelegate);

    contactToInviteAdapterDelegate = new ContactToInviteAdapterDelegate(context);
    delegatesManager.addDelegate(contactToInviteAdapterDelegate);

    items = new ArrayList<>();

    setHasStableIds(true);

    subscriptions.add(Observable.merge(contactToInviteAdapterDelegate.onClickFb(),
        contactToInviteAdapterDelegate.onClickAddressBook()).subscribe(pair -> {
      int postion = (Integer) pair.first;
      Contact c = (Contact) pair.second;
      items.remove(c);
      notifyItemRemoved(postion);
    }));
  }

  @Override public long getItemId(int position) {
    HomeAdapterInterface recipient = getItemAtPosition(position);
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

  public void setHasChat(boolean hasChat) {
    shortcutLiveHomeAdapterDelegate.setHasChat(hasChat);
    shortcutChatActiveHomeAdapterDelegate.setHasChat(hasChat);
    shortcutHomeAdapterDelegate.setHasChat(hasChat);
  }

  public void releaseSubscriptions() {
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    delegatesManager.releaseSubscriptions();
  }

  public int getSupportPosition() {
    for (int i = 0; i < items.size(); i++) {
      HomeAdapterInterface item = items.get(i);
      if (item instanceof Shortcut) {
        if (((Shortcut) item).isSupport()) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public Observable<View> onClickMore() {
    return Observable.merge(shortcutChatActiveHomeAdapterDelegate.onClickMore(),
        shortcutHomeAdapterDelegate.onClickMore(), shortcutLiveHomeAdapterDelegate.onClickMore());
  }

  public Observable<View> onClick() {
    return Observable.merge(shortcutChatActiveHomeAdapterDelegate.onClick(),
        shortcutLiveHomeAdapterDelegate.onClick(), shortcutHomeAdapterDelegate.onClick());
  }

  public Observable<View> onChatClick() {
    return Observable.merge(shortcutHomeAdapterDelegate.onChatClick(),
        shortcutLiveHomeAdapterDelegate.onChatClick(),
        shortcutChatActiveHomeAdapterDelegate.onChatClick());
  }

  public Observable<View> onLiveClick() {
    return Observable.merge(shortcutHomeAdapterDelegate.onLiveClick(),
        shortcutLiveHomeAdapterDelegate.onLiveClick(),
        shortcutChatActiveHomeAdapterDelegate.onLiveClick());
  }

  public Observable<View> onLongClick() {
    return Observable.merge(shortcutHomeAdapterDelegate.onLongClick(),
        shortcutLiveHomeAdapterDelegate.onLongClick(),
        shortcutChatActiveHomeAdapterDelegate.onLongClick());
  }

  public Observable<View> onMainClick() {
    return Observable.merge(shortcutHomeAdapterDelegate.onMainClick(),
        shortcutLiveHomeAdapterDelegate.onMainClick(),
        shortcutChatActiveHomeAdapterDelegate.onMainClick());
  }

  public Observable<View> onAddUser() {
    return userToAddAdapterDelegate.onClick();
  }

  public Observable<View> onInvite() {
    return contactToInviteAdapterDelegate.onInvite();
  }

  public Observable<Pair> onClickFb() {
    return contactToInviteAdapterDelegate.onClickFb();
  }

  public Observable<Pair> onClickAddressBook() {
    return contactToInviteAdapterDelegate.onClickAddressBook();
  }

  public void setItems(List<HomeAdapterInterface> list) {
    this.items.clear();
    this.items.addAll(list);
  }

  public HomeAdapterInterface getItemAtPosition(int position) {
    if (items.size() > 0 && position < items.size()) {
      return items.get(position);
    } else {
      return null;
    }
  }

  public List<HomeAdapterInterface> getItems() {
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

  @Override public void onItemDismiss(int position) {
    Timber.d("onItemDismiss");
  }
}
