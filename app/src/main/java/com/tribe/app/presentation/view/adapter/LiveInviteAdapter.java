package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.grid.CallRouletteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserInviteHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserLiveCoInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.RecyclerViewItemEnabler;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveInviteAdapter extends RecyclerView.Adapter implements RecyclerViewItemEnabler {

  public static final int EMPTY_HEADER_VIEW_TYPE = 99;

  private ScreenUtils screenUtils;

  protected RxAdapterDelegatesManager delegatesManager;
  private UserInviteAdapterDelegate userInviteAdapterDelegate;
  private UserLiveCoInviteAdapterDelegate userLiveCoInviteAdapterDelegate;
  private UserInviteHeaderAdapterDelegate userInviteHeaderAdapterDelegate;
  private CallRouletteAdapterDelegate callRouletteAdapterDelegate;

  // VARIABLES
  private List<Recipient> items;
  private boolean allEnabled = true;
  private Context context;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Inject public LiveInviteAdapter(Context context) {
    this.context = context;
    screenUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
        .screenUtils();
    delegatesManager = new RxAdapterDelegatesManager<>();

    delegatesManager.addDelegate(EMPTY_HEADER_VIEW_TYPE,
        new UserInviteHeaderAdapterDelegate(context));

    delegatesManager.addDelegate(new EmptyGridAdapterDelegate(context, false, true));

    userInviteAdapterDelegate = new UserInviteAdapterDelegate(context);
    delegatesManager.addDelegate(userInviteAdapterDelegate);

    callRouletteAdapterDelegate = new CallRouletteAdapterDelegate(context);
    delegatesManager.addDelegate(callRouletteAdapterDelegate);

    userLiveCoInviteAdapterDelegate = new UserLiveCoInviteAdapterDelegate(context);
    delegatesManager.addDelegate(userLiveCoInviteAdapterDelegate);

    userInviteHeaderAdapterDelegate = new UserInviteHeaderAdapterDelegate(context);
    delegatesManager.addDelegate(userInviteHeaderAdapterDelegate);

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

  public void releaseSubscriptions() {
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

    delegatesManager.releaseSubscriptions();
  }

  @Override public int getItemCount() {
    return items.size();
  }

  private Friendship getDiceItem() {
    User friend = new User(Recipient.ID_CALL_ROULETTE);
    friend.setDisplayName(context.getResources().getString(R.string.roll_the_dice_invite_title));
    Friendship friendship = new Friendship(Recipient.ID_CALL_ROULETTE);
    friendship.setFriend(friend);
    return friendship;
  }

  public void setItems(List<Recipient> items) {
    this.items.clear();
    this.items.add(new Friendship(Recipient.ID_HEADER));
    this.items.add(getDiceItem());
    this.items.addAll(items);
    ListUtils.addEmptyItems(screenUtils, this.items);
    notifyDataSetChanged();
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

  public void removeItem(int position) {
    items.remove(position);
    notifyItemRemoved(position);
    notifyItemRangeChanged(position, items.size());
  }

  // OBSERVABLES
  public Observable<View> onInviteLiveClick() {
    return userInviteHeaderAdapterDelegate.onInviteLiveClick();
  }
}
