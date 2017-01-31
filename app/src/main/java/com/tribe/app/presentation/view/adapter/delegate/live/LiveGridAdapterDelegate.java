package com.tribe.app.presentation.view.adapter.delegate.live;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewmodel.UserLive;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 12/29/2016.
 */
public class LiveGridAdapterDelegate extends RxAdapterDelegate<List<UserLive>> {

    @Inject
    ScreenUtils screenUtils;

    // VARIABLES
    protected LayoutInflater layoutInflater;
    protected Context context;
    private int count;
    private int screenHeight = 0;

    // RX SUBSCRIPTIONS / SUBJECTS
    protected final PublishSubject<View> click = PublishSubject.create();

    public LiveGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<UserLive> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        LiveViewHolder liveViewHolder =
                new LiveViewHolder(layoutInflater.inflate(R.layout.item_live_grid, parent, false));

        return liveViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<UserLive> items, int position,
                                 @NonNull RecyclerView.ViewHolder holder) {
        LiveViewHolder vh = (LiveViewHolder) holder;
        UserLive userLive = items.get(position);

        if (count <= 4) {
            ViewGroup.LayoutParams params = vh.itemView.getLayoutParams();
            params.height = screenHeight >> 1;
            vh.itemView.setLayoutParams(params);
        } else if (count >= 5 && count <= 6) {
            ViewGroup.LayoutParams params = vh.itemView.getLayoutParams();
            params.height = screenHeight / 3;
            vh.itemView.setLayoutParams(params);
        } else if (count > 6 && count <= 8) {
            ViewGroup.LayoutParams params = vh.itemView.getLayoutParams();
            params.height = screenHeight / 4;
            vh.itemView.setLayoutParams(params);
        }

        UIUtils.setBackgroundGrid(screenUtils, vh.layoutContainer, position, false);

        vh.txtName.setText(userLive.getUser().getId());
    }

    @Override
    public void onBindViewHolder(@NonNull List<UserLive> items,
                                 @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Observable<View> onClick() {
        return click;
    }

    static class LiveViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layoutContainer)
        public ViewGroup layoutContainer;
        @BindView(R.id.txtName)
        public TextViewFont txtName;

        public LiveViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
