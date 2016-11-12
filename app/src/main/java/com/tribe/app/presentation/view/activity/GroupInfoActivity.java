package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/10/16.
 */
public class GroupInfoActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, GroupInfoActivity.class);
        return intent;
    }

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    ScreenUtils screenUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDependencyInjector();
        initUi();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }

    private void initUi() {
        setContentView(R.layout.activity_group_info);
        unbinder = ButterKnife.bind(this);
    }

    private void goToMain() {
        screenUtils.hideKeyboard(this);
    }

    /**
     * Dagger Setup
     */
    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }
}
