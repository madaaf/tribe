package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.scope.LastVersionCode;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.DeviceUtils;

import javax.inject.Inject;

import io.branch.referral.Branch;

public class LauncherActivity extends BaseActivity {

    @Inject
    AccessToken accessToken;

    @Inject
    User currentUser;

    @Inject
    FileUtils fileUtils;

    @Inject
    @LastVersionCode
    Preference<Integer> lastVersion;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, LauncherActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);

        Branch branch = Branch.getInstance();

        branch.initSession((referringParams, error) -> {
            if (error != null) {
                System.out.println("HEY ERROR");
                Log.i("Tribe", error.getMessage());
            }
        }, this.getIntent().getData(), this);

        this.getApplicationComponent().inject(this);

        Uri deepLink = getIntent().getData();
        if (currentUser == null || StringUtils.isEmpty(currentUser.getUsername())
                || currentUser.getFriendshipList().size() == 0) {
            navigator.navigateToLogin(this, deepLink);
        } else {
            if (currentUser != null && currentUser.hasOnlySupport())
                navigator.navigateToLogin(this, deepLink);
            else if (lastVersion.get().equals(DeviceUtils.getVersionCode(this))) {
                navigator.navigateToHome(this, true, deepLink);
            } else {
                navigator.computeActions(this, false, null);
            }
        }

        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void finish() {
        super.finish();
    }
}