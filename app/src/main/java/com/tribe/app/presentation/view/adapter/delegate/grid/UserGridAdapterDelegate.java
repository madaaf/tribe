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
import com.tribe.app.domain.entity.User;
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
    private final PublishSubject<View> clickChatView = PublishSubject.create();
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
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

        return userGridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        UserGridViewHolder vh = (UserGridViewHolder) holder;
        User user = (User) items.get(position);

        if (user.getTribe() == null) {
            vh.viewTile.setInfo(user);
            vh.viewTile.setBackground(position);
            user.setPosition(position);
        } else {
            vh.viewTile.showTapToCancel(user.getTribe());
        }
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

    static class UserGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.viewTile) public TileView viewTile;

        public UserGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
