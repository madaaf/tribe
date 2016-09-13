package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.adapter.delegate.friend.FriendAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 9/7/16.
 */
public class FriendAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<Friendship>> delegatesManager;
    private List<Friendship> items;
    private FriendAdapterDelegate friendAdapterDelegate;

    @Inject
    public FriendAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        friendAdapterDelegate = new FriendAdapterDelegate(context);
        delegatesManager.addDelegate(friendAdapterDelegate);
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

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();;
    }

    public void setItems(List<Friendship> friendshipList) {
        items.clear();
        items.addAll(friendshipList);
        notifyDataSetChanged();
    }

    public Observable<View> clickFriendItem() {
        return friendAdapterDelegate.clickFriendItem();
    }
}