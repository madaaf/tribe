package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tribe.app.presentation.view.adapter.delegate.points.LevelAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.points.LevelLockedAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class LevelAdapter extends RecyclerView.Adapter {

    private RxAdapterDelegatesManager<List<ScoreUtils.Level>> delegatesManager;
    private List<ScoreUtils.Level> items;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public LevelAdapter(Context context, int score) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        delegatesManager.addDelegate(new LevelAdapterDelegate(context, score));
        delegatesManager.addDelegate(new LevelLockedAdapterDelegate(context, score));

        items = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public long getItemId(int position) {
        ScoreUtils.Level level = getLevel(position);
        return level.hashCode();
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

    public void setItems(List<ScoreUtils.Level> levelList) {
        items.clear();
        items.addAll(levelList);
        notifyDataSetChanged();
    }

    public ScoreUtils.Level getLevel(int position) {
        return items.get(position);
    }
}
