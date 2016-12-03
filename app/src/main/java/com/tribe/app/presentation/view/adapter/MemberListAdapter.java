package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.presentation.view.adapter.delegate.friend.MemberListAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tiago on 11/29/16.
 */
public class MemberListAdapter extends RecyclerView.Adapter {

    protected RxAdapterDelegatesManager<List<GroupMember>> delegatesManager;
    private MemberListAdapterDelegate memberListAdapterDelegate;

    private List<GroupMember> items;

    public MemberListAdapter(Context context, boolean currentUserAdmin) {
        delegatesManager = new RxAdapterDelegatesManager<>();

        memberListAdapterDelegate = new MemberListAdapterDelegate(context, currentUserAdmin);
        delegatesManager.addDelegate(memberListAdapterDelegate);

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
        GroupMember recipient = getItemAtPosition(position);
        return recipient.hashCode();
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

    public List<GroupMember> getItems() {
        return items;
    }

    // OBSERVABLES

    public Observable<View> clickAdd() {
        return memberListAdapterDelegate.clickAdd();
    }

    public Observable<View> longClick() { return memberListAdapterDelegate.onLongClick(); }
}
