package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.onboarding.AuthVideoView;
import com.tribe.app.presentation.view.utils.AnimationUtils;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class AuthActivity extends BaseActivity {

    private static int DURATION = 200;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, AuthActivity.class);
        return intent;
    }

    @Inject
    User user;

    @BindView(R.id.viewVideoAuth)
    AuthVideoView authVideoView;

    @BindView(R.id.imgSkip)
    ImageView imgSkip;

    // VARIABLES

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init();
        initPresenter();
    }

    @Override
    protected void onPause() {
        authVideoView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        super.onDestroy();
    }

    private void init() {
        subscriptions.add(Observable.timer(5000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    AnimationUtils.fadeIn(imgSkip, DURATION);
                })
        );
    }

    private void initPresenter() {

    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }
}