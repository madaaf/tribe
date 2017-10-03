package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.presentation.view.adapter.delegate.common.ShortcutAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactToInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.SearchResultGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.UserToAddAdapterDelegate;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class SearchAdapter extends RecyclerView.Adapter {

  public static final int HEADER_TYPE = 99;
  public static final int HEADERS_VIEW_TYPE = 98;
  public static final int EMPTY_VIEW_TYPE = 97;

  // DELEGATES
  private RxAdapterDelegatesManager delegatesManager;
  private ShortcutAdapterDelegate shortcutAdapterDelegate;
  private SearchResultGridAdapterDelegate searchResultGridAdapterDelegate;
  private UserToAddAdapterDelegate userToAddAdapterDelegate;
  private ContactToInviteAdapterDelegate contactToInviteAdapterDelegate;

  // VARIABLES
  private List<Object> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public SearchAdapter(Context context) {
    items = new ArrayList<>();

    delegatesManager = new RxAdapterDelegatesManager();

    shortcutAdapterDelegate = new ShortcutAdapterDelegate(context);
    delegatesManager.addDelegate(shortcutAdapterDelegate);

    searchResultGridAdapterDelegate = new SearchResultGridAdapterDelegate(context);
    delegatesManager.addDelegate(searchResultGridAdapterDelegate);

    userToAddAdapterDelegate = new UserToAddAdapterDelegate(context);
    delegatesManager.addDelegate(userToAddAdapterDelegate);

    contactToInviteAdapterDelegate = new ContactToInviteAdapterDelegate(context);
    delegatesManager.addDelegate(contactToInviteAdapterDelegate);

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

  public void setItems(List<Object> items) {
    this.items.clear();
    this.items.addAll(items);

    this.notifyDataSetChanged();
  }

  public void addItem(Object obj) {
    this.items.add(obj);
    this.notifyItemInserted(items.size() - 1);
  }

  public void clear() {
    this.items.clear();
    this.notifyDataSetChanged();
  }

  public void updateSearch(SearchResult searchResult, List<Object> contactList) {
    this.items.clear();
    this.items.add(searchResult);

    if (contactList != null && contactList.size() > 0) {
      this.items.addAll(contactList);
    }

    this.notifyDataSetChanged();
  }

  /**
   * OBSERVABLES
   */

  public Observable<View> onClick() {
    return Observable.merge(searchResultGridAdapterDelegate.onClick(),
        userToAddAdapterDelegate.onClick());
  }

  public Observable<View> onInvite() {
    return contactToInviteAdapterDelegate.onInvite();
  }
}
