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
    protected final PublishSubject<View> clickOpenTribes = PublishSubject.create();
    protected final PublishSubject<View> clickChatView = PublishSubject.create();
    protected final PublishSubject<View> clickMoreView = PublishSubject.create();
    protected final PublishSubject<View> clickTapToCancel = PublishSubject.create();
    protected final PublishSubject<View> onNotCancel = PublishSubject.create();
    protected final PublishSubject<View> recordStarted = PublishSubject.create();
    protected final PublishSubject<View> recordEnded = PublishSubject.create();
    protected final PublishSubject<View> clickErrorTribes = PublishSubject.create();

    public RecipientGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecipientGridViewHolder recipientGridViewHolder = new RecipientGridViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));

        recipientGridViewHolder.viewTile.initWithParent(parent);
        recipientGridViewHolder.viewTile.onRecordStart().subscribe(recordStarted);
        recipientGridViewHolder.viewTile.onRecordEnd().subscribe(recordEnded);
        recipientGridViewHolder.viewTile.onClickChat().subscribe(clickChatView);
        recipientGridViewHolder.viewTile.onClickMore().subscribe(clickMoreView);
        recipientGridViewHolder.viewTile.onTapToCancel().subscribe(clickTapToCancel);
        recipientGridViewHolder.viewTile.onNotCancel().subscribe(onNotCancel);
        recipientGridViewHolder.viewTile.onOpenTribes().subscribe(clickOpenTribes);
        recipientGridViewHolder.viewTile.onClickErrorTribes().subscribe(clickErrorTribes);

        return recipientGridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Recipient> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        RecipientGridViewHolder vh = (RecipientGridViewHolder) holder;
        Recipient recipient = items.get(position);

        if (!vh.viewTile.isRecording() && !vh.viewTile.isTapToCancel()) {
            if (recipient.getTribe() == null) {
                vh.viewTile.setInfo(recipient);
                vh.viewTile.setBackground(position);
                recipient.setPosition(position);
            } else {
                vh.viewTile.showTapToCancel(recipient.getTribe(), vh.viewTile.getCurrentTribeMode());
            }
        }

        vh.viewTile.setStatus(recipient.getReceivedTribes(), recipient.getSentTribes(), recipient.getErrorTribes());
    }

    public Observable<View> onOpenTribes() {
        return clickOpenTribes;
    }

    public Observable<View> onClickChat() {
        return clickChatView;
    }

    public Observable<View> onClickMore() {
        return clickMoreView;
    }

    public Observable<View> onRecordStart() {
        return recordStarted;
    }

    public Observable<View> onRecordEnd() {
        return recordEnded;
    }

    public Observable<View> onClickTapToCancel() {
        return clickTapToCancel;
    }

    public Observable<View> onNotCancel() {
        return onNotCancel;
    }

    public Observable<View> onClickErrorTribes() {
        return clickErrorTribes;
    }

    protected abstract int getLayoutId();

    static class RecipientGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.viewTile) public TileView viewTile;

        public RecipientGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
