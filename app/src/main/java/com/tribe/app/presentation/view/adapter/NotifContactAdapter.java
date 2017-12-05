package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.contact.FriendshiptNotifAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.contact.TribeGuestAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.friend.UserNotifAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewholder.BaseNotifViewHolder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class NotifContactAdapter extends RecyclerView.Adapter {

  // DELEGATES
  protected RxAdapterDelegatesManager delegatesManager;
  private FriendshiptNotifAdapterDelegate friendshiptNotifAdapterDelegate; // my friend
  private TribeGuestAdapterDelegate tribeGuestAdapterDelegate; // on the web
  private UserNotifAdapterDelegate userListAdapterDelegate; // other user on tribe
  // VARIABLES
  private List<Object> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public NotifContactAdapter(Context context, User user) {
    items = new ArrayList<>();

    delegatesManager = new RxAdapterDelegatesManager();

    friendshiptNotifAdapterDelegate = new FriendshiptNotifAdapterDelegate(context);
    delegatesManager.addDelegate(friendshiptNotifAdapterDelegate);

    tribeGuestAdapterDelegate = new TribeGuestAdapterDelegate(context);
    delegatesManager.addDelegate(tribeGuestAdapterDelegate);

    userListAdapterDelegate = new UserNotifAdapterDelegate(context, user);
    delegatesManager.addDelegate(userListAdapterDelegate);

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
        u.setNewFriend(true);
        u.setAnimateAdd(true);
        notifyDataSetChanged();
      }
    }
  }

  public Observable<View> onUnblock() {
    return friendshiptNotifAdapterDelegate.onUnblock();
  }

  public Observable<View> onClickInvite() {
    return tribeGuestAdapterDelegate.onClickInvite();
  }

  public Observable<View> onClickAdd() {
    return userListAdapterDelegate.onClickAdd();
  }

  public Observable<BaseNotifViewHolder> onClickMore() {
    return userListAdapterDelegate.clickMore();
  }

  /*

    public Observable<BaseNotifViewHolder> onClickMore() {
    return Observable.merge(friendshiptNotifAdapterDelegate.clickMore(),
        userListAdapterDelegate.clickMore());
  }

   */
}
