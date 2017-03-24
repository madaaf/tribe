package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import io.branch.referral.Branch;
import javax.inject.Inject;

public class LauncherActivity extends BaseActivity {

  @Inject AccessToken accessToken;

  @Inject User currentUser;

  @Inject FileUtils fileUtils;

  @Inject @LastVersionCode Preference<Integer> lastVersion;

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, LauncherActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setBackgroundDrawable(null);

    Branch branch = Branch.getInstance();

    branch.initSession((referringParams, error) -> {
      if (error != null) {
        Log.i("Tribe", error.getMessage());
      }
    }, this.getIntent().getData(), this);

    this.getApplicationComponent().inject(this);

    Uri deepLink = getIntent().getData();
    if (currentUser == null || StringUtils.isEmpty(currentUser.getUsername())) {
      navigator.navigateToLogin(this, deepLink);
    } else {
      //navigator.navigateToAuthAccess(this, deepLink);
      navigator.navigateToHome(this, true, deepLink);
    }

    finish();
  }

  @Override protected void onNewIntent(Intent intent) {
    setIntent(intent);
  }

  @Override public void finish() {
    super.finish();
  }
}