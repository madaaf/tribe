package com.tribe.app.presentation.view.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jenzz.appstate.AppState;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.activity.LiveImmersiveNotificationActivity;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class NotificationBuilder {

  private AndroidApplication application;
  private NotificationManagerCompat notificationManager;
  private Gson gson;

  @Inject public NotificationBuilder(AndroidApplication application,
      NotificationManagerCompat notificationManager, Gson gson) {
    this.application = application;
    this.notificationManager = notificationManager;
    this.gson = gson;
  }

  public void sendBundledNotification(RemoteMessage remoteMessage) {
    NotificationPayload notificationPayload = getPayload(remoteMessage);

    if (notificationPayload != null && !StringUtils.isEmpty(notificationPayload.getClickAction())) {
      if (application.getAppState() != null && application.getAppState()
          .equals(AppState.FOREGROUND)) {
        Intent intentUnique = new Intent(BroadcastUtils.BROADCAST_NOTIFICATIONS);
        intentUnique.putExtra(BroadcastUtils.NOTIFICATION_PAYLOAD, notificationPayload);
        application.sendBroadcast(intentUnique);
      } else {
        Notification notification = buildNotification(notificationPayload, "Tribos");
        if (notification != null) {
          sendFullScreenNotification(remoteMessage);
          //notificationManager.notify(getNotificationId(notificationPayload), notification);
        }
      }
    }
  }

  private NotificationPayload getPayload(RemoteMessage remoteMessage) {
    if (remoteMessage != null && remoteMessage.getData() != null) {
      JsonElement jsonElement = gson.toJsonTree(remoteMessage.getData());
      NotificationPayload payload = gson.fromJson(jsonElement, NotificationPayload.class);
      return payload;
    }

    return null;
  }

  private Notification buildNotification(NotificationPayload payload, String groupKey) {
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(application).setContentTitle(
            application.getString(R.string.app_name))
            .setContentText(payload.getBody())
            .setWhen(new Date().getTime())
            .setSmallIcon(R.drawable.ic_notification)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setGroup(groupKey);

    PendingIntent pendingIntent = getIntentFromPayload(payload);
    if (pendingIntent != null) builder.setContentIntent(pendingIntent);

    builder = addActionsForPayload(builder, payload);

    if (!StringUtils.isEmpty(payload.getSound())) {
      String[] soundSplit = payload.getSound().split("\\.");
      builder.setSound(Uri.parse(
          "android.resource://" + application.getPackageName() + "/raw/" + soundSplit[0]));
    }

    return builder.build();
  }

  private PendingIntent getIntentFromPayload(NotificationPayload payload) {
    Intent notificationIntent = new Intent(application, getClassFromPayload(payload));
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    PendingIntent pendingIntent = PendingIntent.getActivity(application, 0, notificationIntent, 0);

    return pendingIntent;
  }

  private Class getClassFromPayload(NotificationPayload payload) {
    if (payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_ONLINE)
        || payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_FRIENDSHIP)) {
      return HomeActivity.class;
    } else if (payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_LIVE)) {
      return LiveActivity.class;
    }

    return HomeActivity.class;
  }

  private NotificationCompat.Builder addActionsForPayload(NotificationCompat.Builder builder,
      NotificationPayload payload) {
    return addCommonActions(builder, payload);
  }

  private NotificationCompat.Builder addCommonActions(NotificationCompat.Builder builder,
      NotificationPayload payload) {
    return builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_notification_grid,
        application.getString(R.string.live_notification_action_see_online),
        getPendingIntentForHome(payload)).build())
        .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_notification_live,
            application.getString(R.string.live_notification_action_hang_live),
            getPendingIntentForLive(payload)).build());
  }

  private PendingIntent getPendingIntentForLive(NotificationPayload payload) {
    Intent notificationIntent = NotificationUtils.getIntentForLive(application, payload);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    PendingIntent pendingIntent = PendingIntent.getActivity(application, 0, notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    return pendingIntent;
  }

  private PendingIntent getPendingIntentForHome(NotificationPayload payload) {
    Intent notificationIntent = NotificationUtils.getIntentForHome(application, payload);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    PendingIntent pendingIntent = PendingIntent.getActivity(application, 0, notificationIntent, 0);

    return pendingIntent;
  }

  private int getNotificationId(NotificationPayload payload) {
    return !StringUtils.isEmpty(payload.getThread()) ? payload.getThread().hashCode()
        : (int) System.currentTimeMillis();
  }

  private void sendFullScreenNotification(RemoteMessage remoteMessage) {
    Intent incomingCallIntent = new Intent(application, LiveImmersiveNotificationActivity.class);
    incomingCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    NotificationPayload notificationPayload = getPayload(remoteMessage);

    incomingCallIntent.putExtra(LiveImmersiveNotificationActivity.PLAYLOAD_VALUE,
        notificationPayload);
    application.startActivity(incomingCallIntent);
  }
}
