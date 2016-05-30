package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegatesManager;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.view.adapter.delegate.grid.MeGridAdapterDelegate;
import com.tribe.app.presentation.view.adapter.delegate.grid.UserGridAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class HomeGridAdapter extends RxAdapter {

    private AdapterDelegatesManager<List<MarvelCharacter>> delegatesManager;
    private List<MarvelCharacter> items;

    private UserGridAdapterDelegate userGridAdapterDelegate;

    @Inject
    public HomeGridAdapter(Context context) {
        delegatesManager = new AdapterDelegatesManager<>();
        delegatesManager.addDelegate(new MeGridAdapterDelegate(context));

        userGridAdapterDelegate = new UserGridAdapterDelegate(context);
        delegatesManager.addDelegate(userGridAdapterDelegate);
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

    public void setItems(List<MarvelCharacter> items) {
        this.items = items;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Observable<View> onClickChat() {
        return userGridAdapterDelegate.onClickChat();
    }

    public MarvelCharacter getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }
}
