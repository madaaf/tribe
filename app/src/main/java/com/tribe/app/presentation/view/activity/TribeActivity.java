package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.presentation.internal.di.components.DaggerTribeComponent;
import com.tribe.app.presentation.mvp.presenter.TribePresenter;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.view.component.TribePagerView;
import com.tribe.app.presentation.view.utils.PaletteGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class TribeActivity extends BaseActivity implements TribeView {

    public static final String FRIEND_ID = "FRIEND_ID";
    public static final String POSITION = "POSITION";

    public static Intent getCallingIntent(Context context, int position, String friendId) {
        Intent intent = new Intent(context, TribeActivity.class);
        intent.putExtra(POSITION, position);
        intent.putExtra(FRIEND_ID, friendId);
        return intent;
    }

    @Inject
    TribePresenter tribePresenter;

    @BindView(R.id.viewTribePager)
    TribePagerView viewTribePager;

    // VARIABLES
    private String friendId;
    private int position;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initParams();
        initializeDependencyInjector();
        initTribePagerView();
        initializeSubscriptions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewTribePager.onResume();
    }

    @Override
    protected void onPause() {
        viewTribePager.onPause();
        super.onPause();
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

        super.onDestroy();
    }

    private void initParams() {
        friendId = getIntent().getStringExtra(FRIEND_ID);
        position = getIntent().getIntExtra(POSITION, 0);
    }

    private void initUi() {
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_tribe);
        unbinder = ButterKnife.bind(this);
    }

    private void initTribePagerView() {
        List<Tribe> tribeList = new ArrayList<>();
        tribeList.add(new Tribe("0"));
        tribeList.add(new Tribe("1"));
        tribeList.add(new Tribe("2"));
        tribeList.add(new Tribe("3"));
        tribeList.add(new Tribe("4"));
        viewTribePager.setItems(tribeList);
        viewTribePager.setBackgroundColor(PaletteGrid.get(position - 1));
    }

    private void initializeSubscriptions() {
        subscriptions = new CompositeSubscription();

        subscriptions.add(viewTribePager.onDismissHorizontal().delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));
        subscriptions.add(viewTribePager.onDismissVertical().delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));
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