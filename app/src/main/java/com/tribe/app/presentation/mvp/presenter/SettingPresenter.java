package com.tribe.app.presentation.mvp.presenter;

import android.content.Context;

import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.mvp.view.UpdateUserView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.facebook.RxFacebook;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class SettingPresenter extends UpdateUserPresenter {

    private final RemoveInstall removeInstall;

    private SettingView settingView;

    @Inject
    SettingPresenter(UpdateUser updateUser, RxFacebook rxFacebook, RemoveInstall removeInstall, Context context) {
        super(updateUser, rxFacebook, context);
        this.removeInstall = removeInstall;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void attachView(View v) {
        settingView = (SettingView) v;
    }

    @Override
    public void onCreate() {

    }

    public void logout() {
        removeInstall.execute(new RemoveInstallSubscriber());
    }

    public void goToLauncher() {
        this.settingView.goToLauncher();
    }

    @Override
    protected UpdateUserView getUpdateUserView() {
        return settingView;
    }

    private final class RemoveInstallSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(User user) {
            goToLauncher();
        }
    }
}
