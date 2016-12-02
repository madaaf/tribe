package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.friend.FriendMemberAdapterDelegate;
import com.tribe.app.presentation.view.adapter.filter.UserListFilter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tiago on 10/01/16.
 */
public class FriendMembersAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<GroupMember>> delegatesManager;
    private FriendMemberAdapterDelegate friendMemberAdapterDelegate;

    private List<GroupMember> items;
    private List<GroupMember> itemsFiltered;
    private boolean hasFilter = false;
    private UserListFilter filter;

    public FriendMembersAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();

        friendMemberAdapterDelegate = new FriendMemberAdapterDelegate(context);
        delegatesManager.addDelegate(friendMemberAdapterDelegate);

        items = new ArrayList<>();
        itemsFiltered = new ArrayList<>();
        filter = new UserListFilter(items, this);

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
        GroupMember obj = getItemAtPosition(position);
        return obj.hashCode();
    }

    public void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<GroupMember> items) {
        hasFilter = false;
        this.items.clear();
        this.items.addAll(items);

        if (!hasFilter) {
            this.itemsFiltered.clear();
            this.itemsFiltered.addAll(this.items);
        }

        this.notifyDataSetChanged();
    }

    public void setFilteredItems(List<GroupMember> items) {
        hasFilter = true;
        this.itemsFiltered.clear();
        this.itemsFiltered.addAll(items);
    }

    public GroupMember getItemAtPosition(int position) {
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

    public Observable<View> clickAdd() {
        return friendMemberAdapterDelegate.clickAdd();
    }
}
