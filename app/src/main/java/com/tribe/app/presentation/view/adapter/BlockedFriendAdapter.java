package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.friend.BlockedFriendAdapterDelegate;
import com.tribe.app.presentation.view.adapter.filter.BlockedFriendsFilter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tiago on 10/01/16.
 */
public class BlockedFriendAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<Friendship>> delegatesManager;
    private BlockedFriendAdapterDelegate friendAdapterDelegate;

    private List<Friendship> items;
    private List<Friendship> itemsFiltered;
    private boolean hasFilter = false;
    private BlockedFriendsFilter filter;

    public BlockedFriendAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();

        friendAdapterDelegate = new BlockedFriendAdapterDelegate(context);
        delegatesManager.addDelegate(friendAdapterDelegate);

        items = new ArrayList<>();
        itemsFiltered = new ArrayList<>();

        setHasStableIds(true);
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
        delegatesManager.onBindViewHolder(itemsFiltered, position, holder);
    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    @Override
    public long getItemId(int position) {
        Object obj = getItemAtPosition(position);
        return obj.hashCode();
    }

    public void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<Friendship> items) {
        hasFilter = false;
        this.items.clear();
        this.items.addAll(items);

        if (!hasFilter) {
            this.itemsFiltered.clear();
            this.itemsFiltered.addAll(this.items);
        }

        this.notifyDataSetChanged();
    }

    public void setFilteredItems(List<Friendship> items) {
        hasFilter = true;
        this.itemsFiltered.clear();
        this.itemsFiltered.addAll(items);
    }

    public Friendship getItemAtPosition(int position) {
        if (itemsFiltered.size() > 0 && position < itemsFiltered.size()) {
            return itemsFiltered.get(position);
        } else {
            return null;
        }
    }

    public void filterList(String text) {
        if (!StringUtils.isEmpty(text)) {
            filter.filter(text);
        } else {
            this.itemsFiltered.clear();
            this.itemsFiltered.addAll(this.items);
            notifyDataSetChanged();
        }
    }

    public void updateFriendship(Friendship friendship) {
        if (itemsFiltered != null && itemsFiltered.size() > 0) {
            int position = -1;
            for (Friendship fr : itemsFiltered) {
                if (fr.getId().equals(friendship.getId())) {
                    fr.setStatus(friendship.getStatus());
                    position = itemsFiltered.indexOf(fr);
                }
            }

            if (position != -1) notifyItemChanged(position);
        }
    }

    public Observable<View> clickAdd() {
        return friendAdapterDelegate.clickAdd();
    }
}
