package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DoBootstrapSupport;
import com.tribe.app.domain.interactor.user.DoRegister;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.ProfileInfoMVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import javax.inject.Inject;
import javax.inject.Named;

public class ProfileInfoPresenter extends UpdateUserPresenter {

    // VIEW ATTACHED
    private ProfileInfoMVPView profileInfoView;

    // USECASES
    private final JobManager jobManager;
    private final DoRegister doRegister;
    private final DoBootstrapSupport bootstrapSupport;
    private final GetCloudUserInfos cloudUserInfos;

    // SUBSCRIBERS
    private RegisterSubscriber registerSubscriber;
    private BootstrapSupportSubscriber bootstrapSupportSubscriber;
    private UserInfoSubscriber userInfoSubscriber;

    @Inject
    public ProfileInfoPresenter(JobManager jobManager,
                                RxFacebook rxFacebook,
                                @Named("lookupByUsername") LookupUsername lookupUsername,
                                DoRegister doRegister,
                                DoBootstrapSupport bootstrapSupport,
                                UpdateUser updateUser,
                                GetCloudUserInfos cloudUserInfos) {
        super(updateUser, lookupUsername, rxFacebook);
        this.jobManager = jobManager;
        this.doRegister = doRegister;
        this.bootstrapSupport = bootstrapSupport;
        this.cloudUserInfos = cloudUserInfos;
    }

    @Override
    public void onViewDetached() {
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        lookupUsername.unsubscribe();
        doRegister.unsubscribe();
        cloudUserInfos.unsubscribe();
        updateUser.unsubscribe();
        bootstrapSupport.unsubscribe();
    }

    @Override
    public void onViewAttached(MVPView v) {
        profileInfoView = (ProfileInfoMVPView) v;
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
    protected UpdateUserMVPView getUpdateUserView() {
        return profileInfoView;
    }

    public void loadFacebookInfos() {
        subscriptions.add(rxFacebook.requestInfos().subscribe(new FacebookInfosSubscriber()));
    }

    public void updateFacebookScoreLogin() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.SYNCHRONIZE_FRIENDS, 1));
    }

    public void register(String displayName, String username, LoginEntity loginEntity) {
        profileInfoView.showLoading();

        if (registerSubscriber != null)
            registerSubscriber.unsubscribe();

        registerSubscriber = new RegisterSubscriber();
        doRegister.prepare(displayName, username, loginEntity);
        doRegister.execute(registerSubscriber);
    }

    public void boostrapSupport() {
        if (bootstrapSupportSubscriber != null)
            bootstrapSupportSubscriber.unsubscribe();

        bootstrapSupportSubscriber = new BootstrapSupportSubscriber();
        bootstrapSupport.execute(bootstrapSupportSubscriber);
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
            if (accessToken != null) boostrapSupport();
        }
    }

    private class BootstrapSupportSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
            getUserInfo();
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
