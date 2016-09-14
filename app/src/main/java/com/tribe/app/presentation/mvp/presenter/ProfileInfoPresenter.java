package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DoRegister;
import com.tribe.app.domain.interactor.user.FindByUsername;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.ProfileInfoView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import javax.inject.Inject;
import javax.inject.Named;

import rx.subscriptions.CompositeSubscription;

public class ProfileInfoPresenter implements Presenter {

    // VIEW ATTACHED
    private ProfileInfoView profileInfoView;

    // USECASES
    private final RxFacebook rxFacebook;
    private final FindByUsername findByUsername;
    private final DoRegister doRegister;
    private final UpdateUser updateUser;
    private final GetCloudUserInfos cloudUserInfos;

    // SUBSCRIBERS
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private FindByUserNameSubscriber findByUsernameSubscriber;
    private RegisterSubscriber registerSubscriber;
    private UpdateUserSubscriber updateUserSubscriber;
    private UserInfoSubscriber userInfoSubscriber;

    @Inject
    public ProfileInfoPresenter(RxFacebook rxFacebook,
                                @Named("cloudFindByUsername") FindByUsername findByUsername,
                                DoRegister doRegister,
                                UpdateUser updateUser,
                                GetCloudUserInfos cloudUserInfos) {
        super();
        this.rxFacebook = rxFacebook;
        this.findByUsername = findByUsername;
        this.doRegister = doRegister;
        this.updateUser = updateUser;
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
        findByUsername.unsubscribe();
        doRegister.unsubscribe();
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

    public void loadFacebookInfos() {
        subscriptions.add(rxFacebook.requestInfos().subscribe(new FacebookInfosSubscriber()));
    }

    public void findByUsername(String username) {
        if (findByUsernameSubscriber != null)
            findByUsernameSubscriber.unsubscribe();

        findByUsernameSubscriber = new FindByUserNameSubscriber();
        findByUsername.setUsername(username);
        findByUsername.execute(findByUsernameSubscriber);
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

    public void updateUser(String username, String displayName, String pictureUri) {
        if (updateUserSubscriber != null)
            updateUserSubscriber.unsubscribe();

        updateUserSubscriber = new UpdateUserSubscriber();
        updateUser.prepare(username, displayName, pictureUri);
        updateUser.execute(new UpdateUserSubscriber());
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

    private class FindByUserNameSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() { }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(User user) {
            profileInfoView.usernameResult(user);
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

    private final class UpdateUserSubscriber extends DefaultSubscriber<User> {

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
            profileInfoView.hideLoading();
            if (user != null) profileInfoView.goToAccess();
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
            if (user != null) profileInfoView.userRegistered();
        }
    }
}
