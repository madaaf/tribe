package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.PointsPresenter;
import com.tribe.app.presentation.mvp.view.PointsView;
import com.tribe.app.presentation.view.adapter.manager.PointsLayoutManager;
import com.tribe.app.presentation.view.adapter.pager.PointsAdapter;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class PointsActivity extends BaseActivity implements PointsView {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, PointsActivity.class);
        return intent;
    }

    @Inject
    PointsPresenter pointsPresenter;

    @BindView(R.id.recyclerViewPoints)
    RecyclerView recyclerViewPoints;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    // LAYOUT VARIABLES
    private PointsLayoutManager pointsLayoutManager;
    private PointsAdapter pointsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initResources();
        initDependencyInjector();
        initUi();
        initSubscriptions();
        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPresenter();
    }

    @Override
    protected void onStop() {
        pointsPresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (pointsPresenter != null) pointsPresenter.onDestroy();
        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
        if (pointsAdapter != null) pointsAdapter.releaseSubscriptions();

        super.onDestroy();
    }

    private void initResources() {

    }

    private void initUi() {
        setContentView(R.layout.activity_points);
        unbinder = ButterKnife.bind(this);

    }

    private void initRecyclerView() {
        pointsLayoutManager = new PointsLayoutManager(this);
        recyclerViewPoints.setLayoutManager(pointsLayoutManager);
        pointsAdapter = new PointsAdapter(this);
        pointsAdapter.setItems(Arrays.asList(ScoreUtils.Point.values()));
        recyclerViewPoints.setAdapter(pointsAdapter);
        recyclerViewPoints.setHasFixedSize(true);
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initPresenter() {
        pointsPresenter.onStart();
        pointsPresenter.attachView(this);
    }

    @OnClick(R.id.btnBack)
    public void exit() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }
}