package com.tribe.app.presentation.view.activity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.tribe.app.presentation.TribeBroadcastReceiver;
import com.tribe.app.presentation.service.BroadcastUtils;

/**
 * Base {@link android.app.Activity} class for every Activity with in-app notifications in this
 * application.
 */
public abstract class BaseBroadcastReceiverActivity extends BaseActivity {

  protected boolean receiverRegistered = false;
  protected TribeBroadcastReceiver notificationReceiver;

  @Override protected void onStart() {
    super.onStart();
  }

  @Override protected void onStop() {
    super.onStop();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void onPause() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      receiverRegistered = false;
    }

    super.onPause();
  }

  @Override protected void onResume() {
    super.onResume();

    if (!receiverRegistered) {
      if (notificationReceiver == null) notificationReceiver = new TribeBroadcastReceiver(this);

      registerReceiver(notificationReceiver,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));

      receiverRegistered = true;
    }
  }

  public TribeBroadcastReceiver getBroadcastReceiver() {
    return notificationReceiver;
  }
}
