package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.adapter.LiveGridAdapter;
import com.tribe.app.presentation.view.adapter.manager.LiveLayoutManager;
import com.tribe.app.presentation.view.adapter.viewmodel.UserLive;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.animators.ScaleInRightAnimator;
import rx.subscriptions.CompositeSubscription;

public class LiveTestActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context, Recipient recipient) {
        Intent intent = new Intent(context, LiveTestActivity.class);
        return intent;
    }

    @Inject
    ScreenUtils screenUtils;

    @Inject
    LiveGridAdapter liveGridAdapter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    // VARIABLES
    private Unbinder unbinder;
    private LiveLayoutManager layoutManager;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_test);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init();
        initResources();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        super.onDestroy();
    }

    private void init() {
        liveGridAdapter.setScreenHeight(getScreenHeight());
        layoutManager = new LiveLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new ScaleInRightAnimator());

        List<UserLive> userLiveList = new ArrayList<>();
        userLiveList.add(new UserLive(new User("0"), new View(this)));
        userLiveList.add(new UserLive(new User("1"), new View(this)));
        liveGridAdapter.setItems(userLiveList);

        recyclerView.setAdapter(liveGridAdapter);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 1);
        recyclerView.requestDisallowInterceptTouchEvent(true);
        layoutManager.setScrollEnabled(false);
    }

    private void initResources() {

    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private int getScreenHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }

        return screenUtils.getHeightPx() - result;
    }

    @OnClick(R.id.imgAdd)
    void onAdd() {
//        int position = liveGridAdapter.getItemCount();
        layoutManager.setSpanCount(2);
        layoutManager.requestLayout();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }
}