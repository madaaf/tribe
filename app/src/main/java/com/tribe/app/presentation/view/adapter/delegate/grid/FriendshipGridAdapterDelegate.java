package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
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
public abstract class FriendshipGridAdapterDelegate extends RxAdapterDelegate<List<Friendship>> {

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

    public FriendshipGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        FriendshipGridViewHolder friendshipGridViewHolder = new FriendshipGridViewHolder(layoutInflater.inflate(getLayoutId(), parent, false));

        friendshipGridViewHolder.viewTile.initWithParent(parent);
        friendshipGridViewHolder.viewTile.onRecordStart().subscribe(recordStarted);
        friendshipGridViewHolder.viewTile.onRecordEnd().subscribe(recordEnded);
        friendshipGridViewHolder.viewTile.onClickChat().subscribe(clickChatView);
        friendshipGridViewHolder.viewTile.onClickMore().subscribe(clickMoreView);
        friendshipGridViewHolder.viewTile.onTapToCancel().subscribe(clickTapToCancel);
        friendshipGridViewHolder.viewTile.onNotCancel().subscribe(onNotCancel);
        friendshipGridViewHolder.viewTile.onOpenTribes().subscribe(clickOpenTribes);
        friendshipGridViewHolder.viewTile.onClickErrorTribes().subscribe(clickErrorTribes);

        return friendshipGridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        FriendshipGridViewHolder vh = (FriendshipGridViewHolder) holder;
        Friendship friendship = items.get(position);

        if (!vh.viewTile.isRecording() && !vh.viewTile.isTapToCancel()) {
            if (friendship.getTribe() == null) {
                vh.viewTile.setInfo(friendship.getDisplayName(), friendship.getProfilePicture(), friendship.getReceivedTribes());
                vh.viewTile.setBackground(position);
                friendship.setPosition(position);
            } else {
                vh.viewTile.showTapToCancel(friendship.getTribe());
            }
        }

        vh.viewTile.setStatus(friendship.getReceivedTribes(), friendship.getSentTribes(), friendship.getErrorTribes());
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

    static class FriendshipGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.viewTile) public TileView viewTile;

        public FriendshipGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
