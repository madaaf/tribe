package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.PTSEntity;
import com.tribe.app.presentation.view.adapter.delegate.pulltosearch.LetterAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.pulltosearch.IconAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class PullToSearchAdapter extends RecyclerView.Adapter {

    private RxAdapterDelegatesManager<List<PTSEntity>> delegatesManager;
    private List<PTSEntity> items;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public PullToSearchAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        delegatesManager.addDelegate(new IconAdapterDelegate(context));
        delegatesManager.addDelegate(new LetterAdapterDelegate(context));

        items = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public long getItemId(int position) {
        PTSEntity ptsEntity = getPTSEntity(position);
        return ptsEntity.hashCode();
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

    public void releaseSubscriptions() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<PTSEntity> ptsEntityList) {
        items.clear();
        items.addAll(ptsEntityList);
        notifyDataSetChanged();
    }

    public PTSEntity getPTSEntity(int position) {
        return items.get(position);
    }
}
