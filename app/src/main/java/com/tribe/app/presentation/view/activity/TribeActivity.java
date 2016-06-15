package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.presentation.internal.di.components.DaggerTribeComponent;
import com.tribe.app.presentation.mvp.presenter.IntroPresenter;
import com.tribe.app.presentation.mvp.presenter.TribePresenter;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.view.component.TribeComponentView;
import com.tribe.app.presentation.view.widget.CustomViewPager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class TribeActivity extends BaseActivity implements TribeView {

    public static final String ID = "ID";

    public static Intent getCallingIntent(Context context, Tribe tribe) {
        Intent intent = new Intent(context, TribeActivity.class);
        intent.putExtra(ID, tribe);
        return intent;
    }

    @Inject TribePresenter tribePresenter;

    @Inject
    IntroPresenter introPresenter;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @BindView(R.id.viewTribe)
    TribeComponentView viewTribe;

    // PARAMS
    private Tribe tribe;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initParams();
        initializeDependencyInjector();
        initViewPager();
        initializeSubscriptions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePresenter();
    }

    @Override
    protected void onStop() {
        tribePresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        viewTribe.release();

        super.onDestroy();
    }

    private void initParams() {

    }

    private void initUi() {
        setContentView(R.layout.activity_tribe);
        unbinder = ButterKnife.bind(this);
    }

    private void initViewPager() {

    }

    private void initializeSubscriptions() {
        subscriptions = new CompositeSubscription();
    }

    private void initializeDependencyInjector() {
        DaggerTribeComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initializePresenter() {
        tribePresenter.onStart();
        tribePresenter.attachView(this);
    }
}