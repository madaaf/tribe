package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserInviteHeaderAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserLiveCoInviteAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.RecyclerViewItemEnabler;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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

    // VARIABLES
    private List<Recipient> items;
    private boolean allEnabled = true;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public LiveInviteAdapter(Context context) {
        screenUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().screenUtils();
        delegatesManager = new RxAdapterDelegatesManager<>();

        delegatesManager.addDelegate(EMPTY_HEADER_VIEW_TYPE, new UserInviteHeaderAdapterDelegate(context));
        delegatesManager.addDelegate(new EmptyGridAdapterDelegate(context, false));

        userInviteAdapterDelegate = new UserInviteAdapterDelegate(context);
        delegatesManager.addDelegate(userInviteAdapterDelegate);

        userLiveCoInviteAdapterDelegate = new UserLiveCoInviteAdapterDelegate(context);
        delegatesManager.addDelegate(userLiveCoInviteAdapterDelegate);

        items = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Recipient recipient = getItemAtPosition(position);
        return recipient.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setEnabled(isAllItemsEnabled());
        delegatesManager.onBindViewHolder(items, position, holder);
    }

    public void releaseSubscriptions() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        delegatesManager.releaseSubscriptions();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Recipient> items) {
        this.items.clear();
        this.items.add(new Friendship(Recipient.ID_HEADER));
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

    @Override
    public boolean isAllItemsEnabled() {
        return allEnabled;
    }

    @Override
    public boolean getItemEnabled(int position) {
        return true;
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }
}