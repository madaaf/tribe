package com.tribe.app.presentation.service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.notification.NotificationBuilder;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by tiago on 12/07/2016.
 */
public class TribeFirebaseMessagingService extends FirebaseMessagingService {

  @Inject NotificationBuilder notificationBuilder;

  @Override public void onCreate() {
    super.onCreate();
    ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
  }

  @Override public void onMessageReceived(RemoteMessage remoteMessage) {
    Timber.d("Received : " + remoteMessage.getData());
    notificationBuilder.sendBundledNotification(remoteMessage);
  }
}
