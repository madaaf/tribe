package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerTribeComponent;
import com.tribe.app.presentation.mvp.presenter.TribePresenter;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.component.TribePagerView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class TribeActivity extends BaseActivity implements TribeView {

    public static final String FRIENDSHIP = "FRIENDSHIP";
    public static final String POSITION = "POSITION";

    public static Intent getCallingIntent(Context context, int position, Friendship friendship) {
        Intent intent = new Intent(context, TribeActivity.class);
        intent.putExtra(POSITION, position);
        intent.putExtra(FRIENDSHIP, friendship);
        return intent;
    }

    @Inject
    TribePresenter tribePresenter;

    @BindView(R.id.viewTribePager)
    TribePagerView viewTribePager;

    // VARIABLES
    private Friendship friendship;
    private int position;
    private User currentUser;
    private Tribe currentTribe;

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
        initSubscriptions();
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
        friendship = (Friendship) getIntent().getSerializableExtra(FRIENDSHIP);
        position = getIntent().getIntExtra(POSITION, 0);
        currentUser = getCurrentUser();
    }

    private void initUi() {
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_tribe);
        unbinder = ButterKnife.bind(this);
    }

    private void initTribePagerView() {
//        List<Tribe> tribeList = new ArrayList<>();
//        tribeList.add(new Tribe());
//        tribeList.add(new Tribe());
//        tribeList.add(new Tribe());
//        tribeList.add(new Tribe());
//        tribeList.add(new Tribe());
//        viewTribePager.setItems(tribeList);
//        viewTribePager.setBackgroundColor(PaletteGrid.get(position - 1));
//        viewTribePager.initWithInfo(friendship);
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();

        subscriptions.add(viewTribePager.onDismissHorizontal().delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));
        subscriptions.add(viewTribePager.onDismissVertical().delay(300, TimeUnit.MILLISECONDS).subscribe(aVoid -> finish()));

        subscriptions.add(viewTribePager.onRecordStart()
                .map(view -> tribePresenter.createTribe(currentUser, friendship, viewTribePager.getTribeMode()))
                .subscribe(id -> viewTribePager.startRecording(id)));

        subscriptions.add(viewTribePager.onRecordEnd()
                .subscribe(view -> {
                    viewTribePager.stopRecording(currentTribe.getLocalId());
                    viewTribePager.showTapToCancel(currentTribe);

                }));

        subscriptions.add(viewTribePager.onClickTapToCancel()
                .subscribe(friendship -> {
                    FileUtils.deleteTribe(currentTribe.getLocalId());
                    tribePresenter.deleteTribe(currentTribe);
                    currentTribe = null;
                }));

        subscriptions.add(viewTribePager.onNotCancel()
                .subscribe(friendship -> {
                    tribePresenter.sendTribe(currentTribe);
                    currentTribe = null;
                }));
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

    @Override
    public void setCurrentTribe(Tribe tribe) {
        currentTribe = tribe;
        friendship.setTribe(tribe);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return this;
    }
}