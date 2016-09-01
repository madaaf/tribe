package com.tribe.app.presentation.mvp.presenter;

import android.content.Context;
import android.widget.Toast;

import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.SetUsername;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class SettingPresenter implements Presenter {

    private final SetUsername setUsername;
    private final Context context;

    private SettingView settingView;

    @Inject
    SettingPresenter(SetUsername setUsername, Context context) {
        this.context = context;
        this.setUsername = setUsername;
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

    public void updateUsername(String username) {
        setUsername.prepare(username);
        setUsername.execute(new SetUsernameSubscriber());
    }

    private final class SetUsernameSubscriber extends DefaultSubscriber<User> {
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

}
