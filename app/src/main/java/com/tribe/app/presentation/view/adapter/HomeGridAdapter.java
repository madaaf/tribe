package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.GroupGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.SupportGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;
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

    private ScreenUtils screenUtils;

    protected RxAdapterDelegatesManager delegatesManager;
    private MeGridAdapterDelegate meGridAdapterDelegate;
    private UserGridAdapterDelegate userGridAdapterDelegate;
    private GroupGridAdapterDelegate groupGridAdapterDelegate;
    private SupportGridAdapterDelegate supportGridAdapterDelegate;

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

        meGridAdapterDelegate = new MeGridAdapterDelegate(context);
        delegatesManager.addDelegate(meGridAdapterDelegate);

        userGridAdapterDelegate = new UserGridAdapterDelegate(context);
        delegatesManager.addDelegate(userGridAdapterDelegate);

        groupGridAdapterDelegate = new GroupGridAdapterDelegate(context);
        delegatesManager.addDelegate(groupGridAdapterDelegate);

        supportGridAdapterDelegate = new SupportGridAdapterDelegate(context);
        delegatesManager.addDelegate(supportGridAdapterDelegate);

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

    public Observable<View> onClickChat() {
        return Observable.merge(userGridAdapterDelegate.onClickChat(),
                groupGridAdapterDelegate.onClickChat(),
                supportGridAdapterDelegate.onClickChat());
    }

    public Observable<View> onClickMore() {
        return Observable.merge(userGridAdapterDelegate.onClickMore(),
                groupGridAdapterDelegate.onClickMore(),
                supportGridAdapterDelegate.onClickMore());
    }

    public Observable<View> onRecordStart() {
        return Observable.merge(userGridAdapterDelegate.onRecordStart(),
                groupGridAdapterDelegate.onRecordStart(),
                supportGridAdapterDelegate.onRecordStart());
    }

    public Observable<View> onClickTapToCancel() {
        return Observable.merge(userGridAdapterDelegate.onClickTapToCancel(),
                groupGridAdapterDelegate.onClickTapToCancel(),
                supportGridAdapterDelegate.onClickTapToCancel());
    }

    public Observable<View> onNotCancel() {
        return Observable.merge(userGridAdapterDelegate.onNotCancel(),
                groupGridAdapterDelegate.onNotCancel(),
                supportGridAdapterDelegate.onNotCancel());
    }

    public Observable<View> onRecordEnd() {
        return Observable.merge(userGridAdapterDelegate.onRecordEnd(),
                groupGridAdapterDelegate.onRecordEnd(),
                supportGridAdapterDelegate.onRecordEnd());
    }

    public Observable<View> onOpenTribes() {
        return Observable.merge(userGridAdapterDelegate.onOpenTribes(),
                groupGridAdapterDelegate.onOpenTribes(),
                supportGridAdapterDelegate.onOpenTribes());
    }

    public Observable<View> onClickErrorTribes() {
        return Observable.merge(userGridAdapterDelegate.onClickErrorTribes(),
                groupGridAdapterDelegate.onClickErrorTribes(),
                supportGridAdapterDelegate.onClickErrorTribes());
    }

    public Observable<View> onClickOpenPoints() {
        return meGridAdapterDelegate.clickOpenPoints();
    }

    public Observable<View> onClickOpenSettings() {
        return meGridAdapterDelegate.clickOpenSettings();
    }

    public void setItems(List<Recipient> items) {
        this.items.clear();
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

    public void updateItemWithTribe(int position, TribeMessage tribe) {
        itemsFiltered.get(position).setTribe(tribe);
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
