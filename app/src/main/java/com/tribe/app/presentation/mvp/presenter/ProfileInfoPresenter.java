package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DoRegister;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.ProfileInfoView;
import com.tribe.app.presentation.mvp.view.UpdateUserView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import javax.inject.Inject;
import javax.inject.Named;

public class ProfileInfoPresenter extends UpdateUserPresenter {

    // VIEW ATTACHED
    private ProfileInfoView profileInfoView;

    // USECASES
    private final DoRegister doRegister;
    private final GetCloudUserInfos cloudUserInfos;

    // SUBSCRIBERS
    private RegisterSubscriber registerSubscriber;
    private UserInfoSubscriber userInfoSubscriber;

    @Inject
    public ProfileInfoPresenter(RxFacebook rxFacebook,
                                @Named("lookupByUsername") LookupUsername lookupUsername,
                                DoRegister doRegister,
                                UpdateUser updateUser,
                                GetCloudUserInfos cloudUserInfos) {
        super(updateUser, lookupUsername, rxFacebook);
        this.doRegister = doRegister;
        this.cloudUserInfos = cloudUserInfos;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        // Unused
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        lookupUsername.unsubscribe();
        doRegister.unsubscribe();
        cloudUserInfos.unsubscribe();
        updateUser.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        profileInfoView = (ProfileInfoView) v;
    }

    public void loginFacebook() {
        if (!FacebookUtils.isLoggedIn()) {
            subscriptions.add(rxFacebook.requestLogin().subscribe(loginResult -> {
                if (FacebookUtils.isLoggedIn()) {
                    profileInfoView.successFacebookLogin();
                } else {
                    profileInfoView.errorFacebookLogin();
                }
            }));
        } else {
            profileInfoView.successFacebookLogin();
        }
    }

    @Override
    protected UpdateUserView getUpdateUserView() {
        return profileInfoView;
    }

    public void loadFacebookInfos() {
        subscriptions.add(rxFacebook.requestInfos().subscribe(new FacebookInfosSubscriber()));
    }

    public void register(String displayName, String username, LoginEntity loginEntity) {
        profileInfoView.showLoading();

        if (registerSubscriber != null)
            registerSubscriber.unsubscribe();

        registerSubscriber = new RegisterSubscriber();
        doRegister.prepare(displayName, username, loginEntity);
        doRegister.execute(registerSubscriber);
    }

    public void getUserInfo() {
        if (userInfoSubscriber != null)
            userInfoSubscriber.unsubscribe();

        userInfoSubscriber = new UserInfoSubscriber();
        cloudUserInfos.execute(new UserInfoSubscriber());
    }

    private class FacebookInfosSubscriber extends DefaultSubscriber<FacebookEntity> {

        @Override
        public void onCompleted() { }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(FacebookEntity facebookEntity) {
            if (facebookEntity != null) profileInfoView.loadFacebookInfos(facebookEntity);
        }
    }

    private class RegisterSubscriber extends DefaultSubscriber<AccessToken> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            profileInfoView.hideLoading();
        }

        @Override
        public void onNext(AccessToken accessToken) {
            if (accessToken != null) getUserInfo();
        }
    }

    private final class UserInfoSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            profileInfoView.hideLoading();
        }

        @Override
        public void onNext(User user) {
            if (user != null) profileInfoView.userRegistered(user);
        }
    }
}
