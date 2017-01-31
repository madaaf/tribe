package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.List;

/**
 * Created by tiago on 11/15/2016.
 */
public class EmptyHeaderGridAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

    private LayoutInflater layoutInflater;
    private Context context;
    private ScreenUtils screenUtils;

    public EmptyHeaderGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.screenUtils =
                ((AndroidApplication) context.getApplicationContext()).getApplicationComponent()
                        .screenUtils();
    }

    @Override
    public boolean isForViewType(@NonNull List<Recipient> items, int position) {
        return items.get(position).getSubId().equals(Recipient.ID_HEADER);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new EmptyHeaderGridViewHolder(
                layoutInflater.inflate(R.layout.item_empty_header_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<Recipient> items, int position,
                                 @NonNull RecyclerView.ViewHolder holder) {
    }

    @Override
    public void onBindViewHolder(@NonNull List<Recipient> items,
                                 @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

    }

    static class EmptyHeaderGridViewHolder extends RecyclerView.ViewHolder {

        public EmptyHeaderGridViewHolder(View itemView) {
            super(itemView);
        }
    }
}
