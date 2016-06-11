package com.tribe.app.presentation.view.adapter.delegate.country;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Country;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class CountryAdapterDelegate extends RxAdapterDelegate<List<Country>> {

    protected LayoutInflater layoutInflater;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickCountryItem = PublishSubject.create();

    public CountryAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<Country> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new CountryViewHolder(layoutInflater.inflate(R.layout.item_country, parent, false));

        subscriptions.add(RxView.clicks(vh.itemView)
                .takeUntil(RxView.detaches(parent))
                .map(country -> vh.itemView)
                .subscribe(clickCountryItem));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Country> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        CountryViewHolder vh = (CountryViewHolder) holder;
        Country country = items.get(position);

        vh.txtName.setText(country.name);
    }

    public Observable<View> clickCountryItem() {
        return clickCountryItem;
    }

    static class CountryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;

        public CountryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
