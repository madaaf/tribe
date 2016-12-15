package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;

import com.f2prateek.rx.preferences.Preference;
import com.github.jinatonic.confetti.CommonConfetti;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.AccessPresenter;
import com.tribe.app.presentation.mvp.view.AccessMVPView;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.component.onboarding.AccessView;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.tribe.app.R.id.viewAccess;

public class PickYourFriendsActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, PickYourFriendsActivity.class);
        return intent;
    }

    @Inject
    User user;

    @Inject
    AccessPresenter accessPresenter;

    @BindView(R.id.txtAction)
    TextViewFont txtAction;

    @BindView(R.id.progressView)
    CircularProgressView progressView;

    // VARIABLES
    private Unbinder unbinder;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_access);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init();
        initResources();
        manageDeepLink(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        accessPresenter.onViewAttached(this);
    }

    @Override
    protected void onStop() {
        accessPresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        if (lookupSubscription != null) lookupSubscription.unsubscribe();
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

    private void manageDeepLink(Intent intent) {
        if (intent != null && intent.getData() != null) {
            deepLink = intent.getData();
        }
    }

    @OnClick(R.id.txtAction)
    void onClickAction() {

    }


}