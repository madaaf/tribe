package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fernandocejas.frodo.annotation.RxLogObservable;
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
public class UserGridAdapterDelegate extends RxAdapterDelegate<List<Friendship>> {

    private LayoutInflater layoutInflater;
    private Context context;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickOpenTribes = PublishSubject.create();
    private final PublishSubject<View> clickChatView = PublishSubject.create();
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
    private final PublishSubject<View> clickTapToCancel = PublishSubject.create();
    private final PublishSubject<View> onNotCancel = PublishSubject.create();
    private final PublishSubject<View> recordStarted = PublishSubject.create();
    private final PublishSubject<View> recordEnded = PublishSubject.create();

    public UserGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<Friendship> items, int position) {
        return position != 0;
    }

    @NonNull
    @RxLogObservable
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        UserGridViewHolder userGridViewHolder = new UserGridViewHolder(layoutInflater.inflate(R.layout.item_user_grid, parent, false));

        userGridViewHolder.viewTile.initWithParent(parent);
        userGridViewHolder.viewTile.onRecordStart().subscribe(recordStarted);
        userGridViewHolder.viewTile.onRecordEnd().subscribe(recordEnded);
        userGridViewHolder.viewTile.onClickChat().subscribe(clickChatView);
        userGridViewHolder.viewTile.onClickMore().subscribe(clickMoreView);
        userGridViewHolder.viewTile.onTapToCancel().subscribe(clickTapToCancel);
        userGridViewHolder.viewTile.onNotCancel().subscribe(onNotCancel);
        userGridViewHolder.viewTile.onOpenTribes().subscribe(clickOpenTribes);

        return userGridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        UserGridViewHolder vh = (UserGridViewHolder) holder;
        Friendship friendship = items.get(position);

        if (!vh.viewTile.isRecording() && !vh.viewTile.isTapToCancel()) {
            if (friendship.getTribe() == null) {
                vh.viewTile.setInfo(friendship);
                vh.viewTile.setBackground(position);
                friendship.setPosition(position);
            } else {
                vh.viewTile.showTapToCancel(friendship.getTribe());
            }
        }
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

    static class UserGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.viewTile) public TileView viewTile;

        public UserGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
