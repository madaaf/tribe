package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.adapter.delegate.friend.FriendAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by horatiothomas on 9/7/16.
 */
public class FriendAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<Friendship>> delegatesManager;
    private List<Friendship> items;
    private FriendAdapterDelegate friendAdapterDelegate;

    public FriendAdapter(Context context, boolean privateGroup) {
        delegatesManager = new RxAdapterDelegatesManager<>();

        friendAdapterDelegate = new FriendAdapterDelegate(context, privateGroup);
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

    @Override
    public long getItemId(int position) {
        Object obj = getItemAtPosition(position);
        return obj.hashCode();
    }

    private void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<Friendship> friendshipList) {
        items.clear();
        items.addAll(friendshipList);
        notifyDataSetChanged();
    }

    public Friendship getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    public Observable<View> clickFriendItem() {
        return friendAdapterDelegate.clickFriendItem();
    }
}
