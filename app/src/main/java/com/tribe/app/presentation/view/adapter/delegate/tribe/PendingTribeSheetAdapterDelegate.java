package com.tribe.app.presentation.view.adapter.delegate.tribe;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.PendingType;
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
public class PendingTribeSheetAdapterDelegate extends RxAdapterDelegate<List<PendingType>> {

    protected LayoutInflater layoutInflater;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickPendingTribeItem = PublishSubject.create();

    public PendingTribeSheetAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<PendingType> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder vh = new PendingTribeSheetViewHolder(layoutInflater.inflate(R.layout.item_sheet_pending_tribe, parent, false));

        subscriptions.add(RxView.clicks(vh.itemView)
                .takeUntil(RxView.detaches(parent))
                .map(country -> vh.itemView)
                .subscribe(clickPendingTribeItem));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<PendingType> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        PendingTribeSheetViewHolder vh = (PendingTribeSheetViewHolder) holder;
        PendingType pendingType = items.get(position);

        vh.txtView.setText(pendingType.getLabel());
    }

    public Observable<View> clickPendingTribeItem() {
        return clickPendingTribeItem;
    }

    static class PendingTribeSheetViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtView) public TextViewFont txtView;

        public PendingTribeSheetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
