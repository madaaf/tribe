package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactsGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.ContactsHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.TribeGuestAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.RecipientListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.UserListAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class ContactAdapter extends RecyclerView.Adapter {

  public static final int HEADER_TYPE = 99;

  // DELEGATES
  protected RxAdapterDelegatesManager delegatesManager;
  //private SearchResultGridAdapterDelegate searchResultGridAdapterDelegate;
  private ContactsGridAdapterDelegate contactsGridAdapterDelegate;
  private UserListAdapterDelegate userListAdapterDelegate;
  private RecipientListAdapterDelegate recipientListAdapterDelegate;
  private TribeGuestAdapterDelegate tribeGuestAdapterDelegate;

  // VARIABLES
  private List<Object> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public ContactAdapter(Context context) {
    items = new ArrayList<>();

    delegatesManager = new RxAdapterDelegatesManager();

    //searchResultGridAdapterDelegate = new SearchResultGridAdapterDelegate(context);
    //delegatesManager.addDelegate(searchResultGridAdapterDelegate);

    contactsGridAdapterDelegate = new ContactsGridAdapterDelegate(context);
    delegatesManager.addDelegate(contactsGridAdapterDelegate);

    userListAdapterDelegate = new UserListAdapterDelegate(context);
    delegatesManager.addDelegate(userListAdapterDelegate);

    recipientListAdapterDelegate = new RecipientListAdapterDelegate(context);
    delegatesManager.addDelegate(recipientListAdapterDelegate);

    tribeGuestAdapterDelegate = new TribeGuestAdapterDelegate(context);
    delegatesManager.addDelegate(tribeGuestAdapterDelegate);

    delegatesManager.addDelegate(HEADER_TYPE, new ContactsHeaderAdapterDelegate(context));

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
    this.items.add(R.string.search_usernames);
    this.items.add(searchResult);

    if (contactList != null && contactList.size() > 0) {
      this.items.addAll(contactList);
    }

    this.notifyDataSetChanged();
  }

  public void updateAdd(User user) {
    for (Object obj : items) {
      if (obj instanceof Contact) {
        Contact c = (Contact) obj;
        if (c.getUserList() != null && c.getUserList().size() > 0) {
          User oldUser = c.getUserList().get(0);
          if (oldUser.equals(user)) {
            oldUser.setAnimateAdd(true);
            oldUser.setFriend(true);
            notifyDataSetChanged();
            break;
          }
        }
      } else if (obj instanceof User) {
        User u = (User) obj;
        if (u.equals(user)) {
          u.setAnimateAdd(true);
          u.setFriend(true);
          notifyDataSetChanged();
        }
      }
    }
  }

  // OBSERVABLES
  public Observable<View> onClickAdd() {
    return userListAdapterDelegate.clickAdd();
  }

  public Observable<View> onClickInvite() {
    return Observable.merge(tribeGuestAdapterDelegate.onClickInvite(),
        contactsGridAdapterDelegate.onClickInvite());
  }

  public Observable<BaseListViewHolder> onHangLive() {
    return recipientListAdapterDelegate.onHangLive();
  }

  public Observable<BaseListViewHolder> onUnblock() {
    return recipientListAdapterDelegate.onUnblock();
  }
}
