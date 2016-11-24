package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.adapter.delegate.friend.MemberAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 11/22/16.
 */
public class MembersAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<GroupMember>> delegatesManager;
    private MemberAdapterDelegate memberAdapterDelegate;

    private List<GroupMember> items;

    public MembersAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();

        memberAdapterDelegate = new MemberAdapterDelegate(context);
        delegatesManager.addDelegate(memberAdapterDelegate);

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
        Object obj = getItemAtPosition(position);
        return obj.hashCode();
    }

    public void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<GroupMember> items) {
        this.items.clear();
        this.items.addAll(items);
        this.notifyDataSetChanged();
    }

    public GroupMember getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    // Returns true if add, false otherwise
    public boolean compute(GroupMember groupMember) {
        boolean result = false;

        if (items.contains(groupMember)) {
            remove(groupMember);
        } else {
            add(groupMember);
            result = true;
        }

        return result;
    }

    public void remove(GroupMember groupMember) {
        int position = items.indexOf(groupMember);
        items.remove(position);
        notifyItemRemoved(position);
    }

    public void add(GroupMember groupMember) {
        items.add(groupMember);
        notifyItemInserted(items.size() - 1);
    }
}
