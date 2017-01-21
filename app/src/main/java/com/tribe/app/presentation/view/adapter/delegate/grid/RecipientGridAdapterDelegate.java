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
import com.tribe.app.presentation.view.component.TileView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public abstract class RecipientGridAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

    protected LayoutInflater layoutInflater;
    protected Context context;

    // RX SUBSCRIPTIONS / SUBJECTS
    protected final PublishSubject<View> clickMoreView = PublishSubject.create();
    protected final PublishSubject<View> click = PublishSubject.create();

    public RecipientGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecipientGridViewHolder recipientGridViewHolder = new RecipientGridViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));

        recipientGridViewHolder.viewTile.initClicks();
        recipientGridViewHolder.viewTile.onClickMore().subscribe(clickMoreView);
        recipientGridViewHolder.viewTile.onClick().subscribe(click);

        return recipientGridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Recipient> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        RecipientGridViewHolder vh = (RecipientGridViewHolder) holder;
        Recipient recipient = items.get(position);

        vh.viewTile.setInfo(recipient);
        vh.viewTile.setBackground(position);
    }

    public Observable<View> onClickMore() {
        return clickMoreView;
    }

    public Observable<View> onClick() { return click; }

    protected abstract int getLayoutId();

    static class RecipientGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.viewTile) public TileView viewTile;

        public RecipientGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
