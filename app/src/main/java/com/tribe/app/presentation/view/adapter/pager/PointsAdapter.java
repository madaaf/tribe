package com.tribe.app.presentation.view.adapter.pager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tribe.app.presentation.view.adapter.RxAdapterDelegatesManager;
import com.tribe.app.presentation.view.adapter.delegate.points.PointsAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class PointsAdapter extends RecyclerView.Adapter {

    private RxAdapterDelegatesManager<List<ScoreUtils.Point>> delegatesManager;
    private List<ScoreUtils.Point> items;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public PointsAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        delegatesManager.addDelegate(new PointsAdapterDelegate(context));

        items = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public long getItemId(int position) {
        ScoreUtils.Point point = getPoint(position);
        return point.hashCode();
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

    public void setItems(List<ScoreUtils.Point> pointList) {
        items.clear();
        items.addAll(pointList);
        notifyDataSetChanged();
    }

    public ScoreUtils.Point getPoint(int position) {
        return items.get(position);
    }
}
