package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 01/19/2017.
 */
public class UserInviteHeaderAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

    protected LayoutInflater layoutInflater;
    protected Context context;

    // RX SUBSCRIPTIONS / SUBJECTS
    protected final PublishSubject<View> click = PublishSubject.create();

    public UserInviteHeaderAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<Recipient> items, int position) {
        return items.get(position).getSubId().equals(Recipient.ID_HEADER);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        UserInviteHeaderViewHolder userInviteHeaderViewHolder = new UserInviteHeaderViewHolder(
                layoutInflater.inflate(R.layout.item_live_invite, parent, false));
        userInviteHeaderViewHolder.btnInvite.setOnClickListener(
                v -> click.onNext(userInviteHeaderViewHolder.itemView));
        return userInviteHeaderViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Recipient> items, int position,
                                 @NonNull RecyclerView.ViewHolder holder) {
    }

    @Override
    public void onBindViewHolder(@NonNull List<Recipient> items,
                                 @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

    }

    public Observable<View> onClick() {
        return click;
    }

    static class UserInviteHeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.btnInvite)
        ViewGroup btnInvite;

        public UserInviteHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
