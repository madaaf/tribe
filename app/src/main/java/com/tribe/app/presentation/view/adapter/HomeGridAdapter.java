package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 18/05/2016.
 */
public class HomeGridAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager delegatesManager;
    private UserGridAdapterDelegate userGridAdapterDelegate;

    private List<Friendship> items;

    @Inject
    public HomeGridAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        delegatesManager.addDelegate(new MeGridAdapterDelegate(context));

        userGridAdapterDelegate = new UserGridAdapterDelegate(context);
        delegatesManager.addDelegate(userGridAdapterDelegate);
        items = new ArrayList<>();
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
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<Friendship> items) {
        this.items = items;
        this.notifyDataSetChanged();
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

    public Friendship getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }
}
