package com.tribe.app.presentation.mvp.presenter;

import android.content.Context;
import android.widget.Toast;

import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class SettingPresenter implements Presenter {

    private final UpdateUser updateUser;
    private final RemoveInstall removeInstall;
    private final Context context;

    private SettingView settingView;

    @Inject
    SettingPresenter(UpdateUser updateUser, RemoveInstall removeInstall, Context context) {
        this.context = context;
        this.updateUser = updateUser;
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

    public void changeUsername(String username) {
        this.settingView.changeUsername(username);
    }

    public void changeDisplayName(String displayName) {
        this.settingView.changeDisplayName(displayName);
    }


    public void updateUser(String key, String value) {
        updateUser.prepare(key, value);
        if (key == "username") updateUser.execute(new UpdateUsernameSubscriber());
        if (key == "display_name") updateUser.execute(new UpdateDisplayNameSubscriber());
        if (key == "picture") updateUser.execute(new UpdatePictureSubscriber());
    }

    public void logout() {
        removeInstall.execute(new RemoveInstallSubscriber());
    }

    public void goToLauncher() {
        this.settingView.goToLauncher();
    }

    private final class UpdateUsernameSubscriber extends DefaultSubscriber<User> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onNext(User user) {
            if (user != null) changeUsername(user.getUsername());
        }
    }

    private final class UpdateDisplayNameSubscriber extends DefaultSubscriber<User> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onNext(User user) {
            if (user != null) changeDisplayName(user.getDisplayName());
        }
    }

    private final class UpdatePictureSubscriber extends DefaultSubscriber<User> {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(User user) {

        }
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
