package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.PendingType;
import com.tribe.app.presentation.view.adapter.delegate.tribe.PendingTribeSheetAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 18/05/2016.
 */
public class PendingTribeSheetAdapter extends RecyclerView.Adapter {

    private RxAdapterDelegatesManager<List<PendingType>> delegatesManager;
    private List<PendingType> items;
    private PendingTribeSheetAdapterDelegate pendingTribeSheetAdapterDelegate;

    @Inject
    public PendingTribeSheetAdapter(Context context, List<PendingType> pendingTypeList) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        pendingTribeSheetAdapterDelegate = new PendingTribeSheetAdapterDelegate(context);
        delegatesManager.addDelegate(pendingTribeSheetAdapterDelegate);
        items = new ArrayList<>(pendingTypeList);
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

    public PendingType getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    public void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<PendingType> stringList) {
        items.clear();
        items.addAll(stringList);
        notifyDataSetChanged();
    }

    public Observable<View> clickPendingTribeItem() {
        return pendingTribeSheetAdapterDelegate.clickPendingTribeItem();
    }
}
