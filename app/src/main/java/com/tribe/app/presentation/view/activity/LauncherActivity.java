package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tribe.app.data.realm.AccessToken;

import javax.inject.Inject;

public class LauncherActivity extends BaseActivity {

    @Inject
    AccessToken accessToken;

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
            if (accessToken == null || accessToken.getAccessToken() == null) {
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