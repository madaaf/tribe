package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyHeaderGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.GroupGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserLiveCoGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.interfaces.RecyclerViewItemEnabler;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

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

    private ScreenUtils screenUtils;

    protected RxAdapterDelegatesManager delegatesManager;
    private UserGridAdapterDelegate userGridAdapterDelegate;
    private UserLiveCoGridAdapterDelegate userLiveCoGridAdapterDelegate;
    private GroupGridAdapterDelegate groupGridAdapterDelegate;

    // VARIABLES
    private List<Recipient> items;
    private boolean allEnabled = true;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public HomeGridAdapter(Context context) {
        screenUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
                .screenUtils();
        delegatesManager = new RxAdapterDelegatesManager<>();
        delegatesManager.addDelegate(new EmptyGridAdapterDelegate(context, true));
        delegatesManager.addDelegate(EMPTY_HEADER_VIEW_TYPE,
                new EmptyHeaderGridAdapterDelegate(context));

        userGridAdapterDelegate = new UserGridAdapterDelegate(context);
        delegatesManager.addDelegate(userGridAdapterDelegate);

        userLiveCoGridAdapterDelegate = new UserLiveCoGridAdapterDelegate(context);
        delegatesManager.addDelegate(userLiveCoGridAdapterDelegate);

        groupGridAdapterDelegate = new GroupGridAdapterDelegate(context);
        delegatesManager.addDelegate(groupGridAdapterDelegate);

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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        holder.itemView.setEnabled(isAllItemsEnabled());
        delegatesManager.onBindViewHolder(items, holder, position, payloads);
    }

    public void releaseSubscriptions() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        delegatesManager.releaseSubscriptions();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Observable<View> onClickMore() {
        return Observable.merge(userGridAdapterDelegate.onClickMore(),
                groupGridAdapterDelegate.onClickMore(), userLiveCoGridAdapterDelegate.onClickMore());
    }

    public Observable<View> onClick() {
        return Observable.merge(userGridAdapterDelegate.onClick(), groupGridAdapterDelegate.onClick(),
                userLiveCoGridAdapterDelegate.onClick());
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

    @Override
    public boolean isAllItemsEnabled() {
        return allEnabled;
    }

    @Override
    public boolean getItemEnabled(int position) {
        return true;
    }
}
