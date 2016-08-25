package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.grid.EmptyGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.GroupGridAdapterDelegate;
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
    private MeGridAdapterDelegate meGridAdapterDelegate;
    private UserGridAdapterDelegate userGridAdapterDelegate;
    private GroupGridAdapterDelegate groupGridAdapterDelegate;

    private List<Recipient> items;

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
        return Observable.merge(userGridAdapterDelegate.onClickChat(), groupGridAdapterDelegate.onClickChat());
    }

    public Observable<View> onClickMore() {
        return Observable.merge(userGridAdapterDelegate.onClickMore(), groupGridAdapterDelegate.onClickMore());
    }

    public Observable<View> onRecordStart() {
        return Observable.merge(userGridAdapterDelegate.onRecordStart(), groupGridAdapterDelegate.onRecordStart());
    }

    public Observable<View> onClickTapToCancel() {
        return Observable.merge(userGridAdapterDelegate.onClickTapToCancel(), groupGridAdapterDelegate.onClickTapToCancel());
    }

    public Observable<View> onNotCancel() {
        return Observable.merge(userGridAdapterDelegate.onNotCancel(), groupGridAdapterDelegate.onNotCancel());
    }

    public Observable<View> onRecordEnd() {
        return Observable.merge(userGridAdapterDelegate.onRecordEnd(), groupGridAdapterDelegate.onRecordEnd());
    }

    public Observable<View> onOpenTribes() {
        return Observable.merge(userGridAdapterDelegate.onOpenTribes(), groupGridAdapterDelegate.onOpenTribes());
    }

    public Observable<View> onClickErrorTribes() {
        return Observable.merge(userGridAdapterDelegate.onClickErrorTribes(), groupGridAdapterDelegate.onClickErrorTribes());
    }

    public Observable<View> onClickOpenPoints() {
        return meGridAdapterDelegate.clickOpenPoints();
    }

    public void setItems(List<Recipient> items) {
        this.items.clear();
        this.items.addAll(items);

        double minItems = Math.ceil(((float) screenUtils.getHeight() / (screenUtils.getWidth() >> 1)) * 2);
        if (minItems % 2 != 0) minItems++;

        if (this.items.size() < minItems) {
            for (int i = this.items.size(); i < minItems; i++) {
                this.items.add(new Friendship(Recipient.ID_EMPTY));
            }
        } else {
            this.items.add(new Friendship(Recipient.ID_EMPTY));
            this.items.add(new Friendship(Recipient.ID_EMPTY));

            if (this.items.size() % 2 == 1) {
                this.items.add(new Friendship(Recipient.ID_EMPTY));
            }
        }

        this.notifyDataSetChanged();
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

    public void updateItemWithTribe(int position, TribeMessage tribe) {
        items.get(position).setTribe(tribe);
    }
}
