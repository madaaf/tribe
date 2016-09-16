package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.StringUtils;

import javax.inject.Inject;

public class LauncherActivity extends BaseActivity {

    @Inject
    AccessToken accessToken;

    @Inject
    User currentUser;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, LauncherActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);

        this.getApplicationComponent().inject(this);

        if (IntroActivity.uiOnlyMode) {
            navigator.navigateToLogin(this);
        } else {
            if (currentUser == null || StringUtils.isEmpty(currentUser.getUsername())
                    || currentUser.getFriendshipList().size() > 0) {
                navigator.navigateToLogin(this);
            } else {
                navigator.navigateToHome(this);
            }
        }

        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}