package com.tribe.app.presentation.view.activity;

import android.os.Bundle;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Tribe;

import javax.inject.Inject;

public class LauncherActivity extends BaseActivity {

    @Inject
    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);

        this.getApplicationComponent().inject(this);

        navigator.navigateToTribe(this, new Tribe("LOL"));
//        if (accessToken == null || accessToken.getAccessToken() == null) {
//            navigator.navigateToLogin(this);
//        } else {
//            navigator.navigateToHome(this);
//        }

        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}