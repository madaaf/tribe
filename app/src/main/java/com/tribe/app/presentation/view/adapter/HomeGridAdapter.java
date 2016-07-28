package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class HomeGridAdapter extends RecyclerView.Adapter {

    private ScreenUtils screenUtils;

    protected RxAdapterDelegatesManager delegatesManager;
    private UserGridAdapterDelegate userGridAdapterDelegate;

    private List<Friendship> items;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public HomeGridAdapter(Context context) {
        screenUtils = ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().screenUtils();
        delegatesManager = new RxAdapterDelegatesManager<>();
        delegatesManager.addDelegate(new MeGridAdapterDelegate(context));
        delegatesManager.addDelegate(new EmptyGridAdapterDelegate(context));

        userGridAdapterDelegate = new UserGridAdapterDelegate(context);
        delegatesManager.addDelegate(userGridAdapterDelegate);
        items = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Friendship friendship = getItemAtPosition(position);
        return friendship.hashCode();
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

    public Observable<View> onClickChat() {
        return userGridAdapterDelegate.onClickChat();
    }

    public Observable<View> onClickMore() {
        return userGridAdapterDelegate.onClickMore();
    }

    public Observable<View> onRecordStart() {
        return userGridAdapterDelegate.onRecordStart();
    }

    public Observable<View> onClickTapToCancel() {
        return userGridAdapterDelegate.onClickTapToCancel();
    }

    public Observable<View> onNotCancel() {
        return userGridAdapterDelegate.onNotCancel();
    }

    public Observable<View> onRecordEnd() {
        return userGridAdapterDelegate.onRecordEnd();
    }

    public Observable<View> onOpenTribes() {
        return userGridAdapterDelegate.onOpenTribes();
    }

    public Observable<View> onClickErrorTribes() { return userGridAdapterDelegate.onClickErrorTribes(); }

    public void setItems(List<Friendship> items) {
        this.items.clear();
        this.items.addAll(items);

        double minItems = Math.ceil(((float) screenUtils.getHeight() / (screenUtils.getWidth() >> 1)) * 2);
        if (minItems % 2 != 0) minItems++;

        if (this.items.size() < minItems) {
            for (int i = this.items.size(); i < minItems; i++) {
                this.items.add(new Friendship(Friendship.ID_EMPTY));
            }
        }

        this.notifyDataSetChanged();
    }

    public Friendship getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    public List<Friendship> getItems() {
        return items;
    }

    public void updateItemWithTribe(int position, Tribe tribe) {
        items.get(position).setTribe(tribe);
    }
}
