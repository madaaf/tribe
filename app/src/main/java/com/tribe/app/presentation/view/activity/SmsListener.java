package com.tribe.app.presentation.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by madaaflak on 27/02/2017.
 */
@Singleton public class SmsListener extends BroadcastReceiver {
  private Context context;
  private IntentFilter filter;
  private SmsCallback callback;

  @Inject public SmsListener(Context context, IntentFilter filter) {
    this.context = context;
    this.filter = filter;
    this.filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
  }

  public void setSmsCallback(SmsCallback callback) {
    this.callback = callback;
  }

  public void register() {
    context.registerReceiver(this, filter);
  }

  public void unregister() {
    context.unregisterReceiver(this);
  }

  @Override public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {

      if (Build.VERSION.SDK_INT >= 19) { //KITKAT // TODO
        for (SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
          if (message != null && callback != null) {
            callback.onSmsReceived(message);
          }
        }
      }
    }
  }

  public interface SmsCallback {
    void onSmsReceived(SmsMessage message);
  }
}