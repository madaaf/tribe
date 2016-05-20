package com.tribe.app.presentation.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegatesManager;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.view.adapter.delegate.UserGridAdapterDelegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tiago on 18/05/2016.
 */
public class HomeGridAdapter extends RecyclerView.Adapter {

    private AdapterDelegatesManager<List<MarvelCharacter>> delegatesManager;
    private List<MarvelCharacter> items;

    @Inject
    public HomeGridAdapter(Context context) {
        delegatesManager = new AdapterDelegatesManager<>();
        delegatesManager.addDelegate(new UserGridAdapterDelegate(context));
        items = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, position, holder);
    }

    public void setItems(List<MarvelCharacter> items) {
        this.items = items;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
