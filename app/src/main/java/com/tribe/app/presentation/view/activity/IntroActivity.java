package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.Button;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.HasComponent;
import com.tribe.app.presentation.internal.di.components.DaggerAvengersComponent;
import com.tribe.app.presentation.internal.di.components.DaggerFriendshipComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.FriendshipComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.modules.UserModule;
import com.tribe.app.presentation.mvp.presenter.IntroPresenter;
import com.tribe.app.presentation.mvp.view.IntroView;
import com.tribe.app.presentation.view.fragment.DiscoverGridFragment;
import com.tribe.app.presentation.view.fragment.HomeGridFragment;
import com.tribe.app.presentation.view.fragment.MediaGridFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends BaseActivity implements IntroView {

    @Inject
    IntroPresenter introPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initializeDependencyInjector();
        initializePresenter();
    }

    private void initUi() {
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
    }

    private void initializeDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    private void initializePresenter() {
        introPresenter.attachView(this);
    }

    @OnClick(R.id.btnLogin)
    public void doLogin() {
        introPresenter.login("sofiane@tribe.pm", "okayokay");
    }

    @Override
    public void goToHome() {
        navigator.navigateToHome(context());
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