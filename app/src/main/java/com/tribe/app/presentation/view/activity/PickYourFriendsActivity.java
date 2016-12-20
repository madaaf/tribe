package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.FriendsPresenter;
import com.tribe.app.presentation.mvp.view.FriendsMVPView;
import com.tribe.app.presentation.view.adapter.UserListAdapter;
import com.tribe.app.presentation.view.adapter.decorator.DividerFirstLastItemDecoration;
import com.tribe.app.presentation.view.adapter.manager.UserListLayoutManager;
import com.tribe.app.presentation.view.component.common.LoadFriendsView;
import com.tribe.app.presentation.view.component.common.PickAllView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class PickYourFriendsActivity extends BaseActivity implements FriendsMVPView {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, PickYourFriendsActivity.class);
        return intent;
    }

    @Inject
    User user;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    FriendsPresenter friendsPresenter;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.txtAction)
    TextViewFont txtAction;

    @BindView(R.id.progressView)
    CircularProgressView progressView;

    @BindView(R.id.viewFriendsFBLoad)
    LoadFriendsView viewFriendsFBLoad;

    @BindView(R.id.viewPickAll)
    PickAllView viewPickAll;

    // VARIABLES
    private Unbinder unbinder;
    private Uri deepLink;
    private UserListLayoutManager layoutManager;
    private UserListAdapter adapter;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_friends);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init();
        initResources();
        manageDeepLink(getIntent());
        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        friendsPresenter.onViewAttached(this);
    }

    @Override
    protected void onStop() {
        friendsPresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        super.onDestroy();
    }

    private void init() {
        viewFriendsFBLoad.setOnClickListener(v -> {
            friendsPresenter.loginFacebook();
            viewFriendsFBLoad.showLoading();
        });
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

    private void initRecyclerView() {
        layoutManager = new UserListLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);

        adapter = new UserListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 50);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerFirstLastItemDecoration(screenUtils.dpToPx(5), screenUtils.dpToPx(5)));
    }

    @OnClick(R.id.txtAction)
    void onClickAction() {

    }

    @Override
    public void renderContactList(List<User> contactList) {
        user.computeUserFriends(contactList);
        adapter.setItems(contactList);

        if (contactList.size() > 1) {
            viewPickAll.setAvatars(contactList.get(0).getProfilePicture(), contactList.get(1).getProfilePicture());
            viewPickAll.setBody(getString(R.string.onboarding_friends_to_add_shortcut_subtitle, contactList.size()));
        } else {
            viewPickAll.setBody(getString(R.string.onboarding_friends_to_add_shortcut_subtitle_one, contactList.size()));
        }
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public void successFacebookLogin() {
        viewFriendsFBLoad.hideLoading();
        // TODO REMOVE VIEW / LAUNCH SYNC
    }

    @Override
    public void errorFacebookLogin() {
        viewFriendsFBLoad.hideLoading();
    }

    @Override
    public Context context() {
        return this;
    }
}