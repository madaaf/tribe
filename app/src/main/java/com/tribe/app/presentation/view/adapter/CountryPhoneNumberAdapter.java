package com.tribe.app.presentation.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.domain.entity.Country;
import com.tribe.app.presentation.view.adapter.delegate.country.CountryAdapterDelegate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 18/05/2016.
 */

// TODO: add ripples

    // TODO: country misaligned
    // TODO: fix icon
public class CountryPhoneNumberAdapter extends RecyclerView.Adapter {

    private RxAdapterDelegatesManager<List<Country>> delegatesManager;
    private List<Country> items;
    private CountryAdapterDelegate countryAdapterDelegate;

    @Inject
    public CountryPhoneNumberAdapter(Context context) {
        delegatesManager = new RxAdapterDelegatesManager<>();
        countryAdapterDelegate = new CountryAdapterDelegate(context);
        delegatesManager.addDelegate(countryAdapterDelegate);
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

    public Country getItemAtPosition(int position) {
        if (items.size() > 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    public void releaseSubscriptions() {
        delegatesManager.releaseSubscriptions();
    }

    public void setItems(List<Country> countryList) {
        items.clear();
        items.addAll(countryList);
        notifyDataSetChanged();
    }

    public Observable<View> clickCountryItem() {
        return countryAdapterDelegate.clickCountryItem();
    }
}
