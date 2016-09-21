package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.group.GroupMemberAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 9/19/16.
 */
public class GroupMemberAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<GroupMember>> delegatesManager;
    private List<GroupMember> items;
    private GroupMemberAdapterDelegate groupMemberAdapterDelegate;

    @Inject
    public GroupMemberAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        groupMemberAdapterDelegate = new GroupMemberAdapterDelegate(context);
        delegatesManager.addDelegate(groupMemberAdapterDelegate);
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

    public void setItems(List<GroupMember> groupMemberList) {
        items.clear();
        items.addAll(groupMemberList);
        notifyDataSetChanged();
    }

    public GroupMember getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    public Observable<View> clickMemberItem() {
        return groupMemberAdapterDelegate.clickMemberItem();
    }

}
