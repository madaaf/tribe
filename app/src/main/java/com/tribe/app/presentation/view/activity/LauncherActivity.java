package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.mixpanel.android.mpmetrics.TribeGCMReceiver;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import io.branch.referral.Branch;
import javax.inject.Inject;

public class LauncherActivity extends BaseActivity {

  @Inject User currentUser;

  Uri deepLink;

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

    deepLink = getIntent().getData();
    String action = getIntent().getStringExtra(TribeGCMReceiver.EXTRA_ACTION);

    if (currentUser == null || StringUtils.isEmpty(currentUser.getUsername())) {
      navigator.navigateToLogin(this, deepLink);
    } else if (TribeGCMReceiver.ACTION_CALLROULETTE.equals(action)) {
      navigator.navigateToNewCall(this, LiveActivity.SOURCE_CALL_ROULETTE, null);
    } else {
      navigator.navigateToHomeFromStart(this, deepLink);
    }

    finish();
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override protected void onNewIntent(Intent intent) {
    setIntent(intent);
  }

  @Override public void finish() {
    super.finish();
  }
}