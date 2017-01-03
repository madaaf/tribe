package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public class LiveActivity extends BaseActivity {

    private static final int MAX_NUM_SUBSCRIBERS = 3;

    public static Intent getCallingIntent(Context context, Recipient recipient) {
        Intent intent = new Intent(context, LiveActivity.class);
        return intent;
    }

    @Inject
    User user;

    @BindView(R.id.layoutTop)
    ViewGroup layoutTop;

    @BindView(R.id.layoutBottom)
    ViewGroup layoutBottom;

    // VARIABLES
    private Unbinder unbinder;
    private List<Subscriber> subscriberList = new ArrayList<>();
    private Map<Stream, Subscriber> subscriberStreamList = new HashMap<>();

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init();
        initResources();
        initPermissions();
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

    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initResources() {

    }

    private void initPermissions() {
        subscriptions.add(RxPermissions.getInstance(LiveActivity.this)
                .request(PermissionUtils.PERMISSIONS_LIVE)
                .subscribe(granted -> {

                }));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }
}