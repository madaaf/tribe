package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyHeaderGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.GroupGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserLiveCoGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.filter.RecipientFilter;
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
    private List<Recipient> itemsFiltered;
    private boolean allEnabled = true;
    private boolean hasFilter = false;
    private RecipientFilter filter;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public HomeGridAdapter(Context context) {
        screenUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().screenUtils();
        delegatesManager = new RxAdapterDelegatesManager<>();
        delegatesManager.addDelegate(new EmptyGridAdapterDelegate(context));
        delegatesManager.addDelegate(EMPTY_HEADER_VIEW_TYPE, new EmptyHeaderGridAdapterDelegate(context));

        userGridAdapterDelegate = new UserGridAdapterDelegate(context);
        delegatesManager.addDelegate(userGridAdapterDelegate);

        userLiveCoGridAdapterDelegate = new UserLiveCoGridAdapterDelegate(context);
        delegatesManager.addDelegate(userLiveCoGridAdapterDelegate);

        groupGridAdapterDelegate = new GroupGridAdapterDelegate(context);
        delegatesManager.addDelegate(groupGridAdapterDelegate);

        items = new ArrayList<>();
        itemsFiltered = new ArrayList<>();
        filter = new RecipientFilter(screenUtils, items, this);

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Recipient recipient = getItemAtPosition(position);
        return recipient.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(itemsFiltered, position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setEnabled(isAllItemsEnabled());
        delegatesManager.onBindViewHolder(itemsFiltered, position, holder);
    }

    public void releaseSubscriptions() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        delegatesManager.releaseSubscriptions();
    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    public Observable<View> onClickMore() {
        return Observable.merge(
                userGridAdapterDelegate.onClickMore(),
                groupGridAdapterDelegate.onClickMore(),
                userLiveCoGridAdapterDelegate.onClickMore()
        );
    }

    public Observable<View> onClick() {
        return Observable.merge(
                userGridAdapterDelegate.onClick(),
                groupGridAdapterDelegate.onClick(),
                userLiveCoGridAdapterDelegate.onClick()
        );
    }

    public void setItems(List<Recipient> items) {
        this.items.clear();
        this.items.add(new Friendship(Recipient.ID_HEADER));
        this.items.addAll(items);

        ListUtils.addEmptyItems(screenUtils, this.items);

        if (!hasFilter) {
            this.itemsFiltered.clear();
            this.itemsFiltered.addAll(this.items);
            this.notifyDataSetChanged();
        } else {
            filterList(filter.getFilter());
        }
    }

    public void setFilteredItems(List<Recipient> items) {
        this.itemsFiltered.clear();
        this.itemsFiltered.addAll(items);
    }

    public Recipient getItemAtPosition(int position) {
        if (itemsFiltered.size() > 0 && position < itemsFiltered.size()) {
            return itemsFiltered.get(position);
        } else {
            return null;
        }
    }

    public List<Recipient> getItems() {
        return itemsFiltered;
    }

    public void setAllItemsEnabled(boolean enable) {
        allEnabled = enable;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void filterList(String text) {
        if (!StringUtils.isEmpty(text)) {
            hasFilter = true;
            filter.filter(text);
        } else {
            hasFilter = false;
            this.itemsFiltered.clear();
            this.itemsFiltered.addAll(this.items);
            notifyDataSetChanged();
            filter.setFilter(null);
        }
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
