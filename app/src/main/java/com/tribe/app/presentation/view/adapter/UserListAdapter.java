package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.adapter.delegate.friend.UserListAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tiago on 12/19/16.
 */
public class UserListAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<User>> delegatesManager;
    private UserListAdapterDelegate userListAdapterDelegate;

    private List<User> items;

    public UserListAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();

        userListAdapterDelegate = new UserListAdapterDelegate(context);
        delegatesManager.addDelegate(userListAdapterDelegate);

        items = new ArrayList<>();

        setHasStableIds(true);
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

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        User user = getItemAtPosition(position);
        return user.hashCode();
    }

    public void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<User> items) {
        this.items.clear();
        this.items.addAll(items);
        this.notifyDataSetChanged();
    }

    public User getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    public List<User> getItems() {
        return items;
    }

    // OBSERVABLES

    public Observable<View> clickAdd() {
        return userListAdapterDelegate.clickAdd();
    }
}
