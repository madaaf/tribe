package com.tribe.app.presentation.view.notification;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jenzz.appstate.RxAppStateMonitor;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Singleton;
import timber.log.Timber;

@Singleton public class NotificationBuilder {

  private Application application;
  private NotificationManagerCompat notificationManager;
  private Gson gson;

  @Inject
  public NotificationBuilder(Application application, NotificationManagerCompat notificationManager,
      Gson gson) {
    this.application = application;
    this.notificationManager = notificationManager;
    this.gson = gson;
  }

  public void sendBundledNotification(RemoteMessage remoteMessage) {
    Notification notification = buildNotification(remoteMessage, "Tribos");
    if (notification != null) notificationManager.notify(getNotificationId(), notification);
    //Notification summary = buildSummary(message, GROUP_KEY);
    //notificationManager.notify(SUMMARY_ID, summary);
  }

  private Notification buildNotification(RemoteMessage remoteMessage, String groupKey) {
    if (remoteMessage != null && remoteMessage.getData() != null) {
      JsonElement jsonElement = gson.toJsonTree(remoteMessage.getData());
      NotificationPayload payload = gson.fromJson(jsonElement, NotificationPayload.class);

      NotificationCompat.Builder builder = new NotificationCompat.Builder(application).setContentTitle(
          application.getString(R.string.app_name))
          .setContentText(payload.getBody())
          .setWhen(new Date().getTime())
          .setSmallIcon(R.drawable.ic_notification)
          .setShowWhen(true)
          .setGroup(groupKey);

      if (StringUtils.isEmpty(payload.getSound())) {
        builder.setSound(Uri.parse(
            "android.resource://" + application.getPackageName() + "/raw/" + payload.getSound()));
      }

      return builder.build();
    }

    return null;
  }

  //private Notification buildSummary(Message message, String groupKey) {
  //  return new NotificationCompat.Builder(context).setContentTitle("Nougat Messenger")
  //      .setContentText("You have unread messages")
  //      .setWhen(message.timestamp())
  //      .setSmallIcon(R.drawable.ic_message)
  //      .setShowWhen(true)
  //      .setGroup(groupKey)
  //      .setGroupSummary(true)
  //      .build();
  //}

  private int getNotificationId() {
    return 0;
  }
}