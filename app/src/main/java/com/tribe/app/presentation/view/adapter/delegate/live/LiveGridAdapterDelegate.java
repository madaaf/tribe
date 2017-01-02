package com.tribe.app.presentation.view.adapter.delegate.live;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.adapter.viewmodel.UserLive;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
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
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<UserLive> items, int position) {
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        LiveViewHolder liveViewHolder = new LiveViewHolder(layoutInflater.inflate(R.layout.item_live_grid, parent, false));

        if (count <= 2) {
            ViewGroup.LayoutParams params = liveViewHolder.itemView.getLayoutParams();
            params.height = screenHeight >> 1;
            liveViewHolder.itemView.setLayoutParams(params);
        } else if (count <= 4) {
            ViewGroup.LayoutParams params = liveViewHolder.itemView.getLayoutParams();
            params.height = screenHeight >> 1;
            liveViewHolder.itemView.setLayoutParams(params);
        }

        return liveViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<UserLive> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        LiveViewHolder vh = (LiveViewHolder) holder;

        UIUtils.setBackgroundGrid(screenUtils, vh.layoutContainer, position, false);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Observable<View> onClick() { return click; }

    static class LiveViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layoutContainer) public ViewGroup layoutContainer;

        public LiveViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
