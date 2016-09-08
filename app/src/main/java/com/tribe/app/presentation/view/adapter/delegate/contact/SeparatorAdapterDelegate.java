package com.tribe.app.presentation.view.adapter.delegate.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;

import java.util.List;

/**
 * Created by tiago on 18/05/2016.
 */
public class SeparatorAdapterDelegate extends RxAdapterDelegate<List<Object>> {

    protected LayoutInflater layoutInflater;
    private Context context;

    // OBSERVABLES

    public SeparatorAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<Object> items, int position) {
        return (items.get(position) instanceof String);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        SeparatorViewHolder vh = new SeparatorViewHolder(layoutInflater.inflate(R.layout.item_large_separator, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Object> items, int position, @NonNull RecyclerView.ViewHolder holder) {
    }

    static class SeparatorViewHolder extends RecyclerView.ViewHolder {

        public SeparatorViewHolder(View itemView) {
            super(itemView);
        }
    }
}
