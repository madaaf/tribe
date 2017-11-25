package com.tribe.app.presentation.view.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jenzz.appstate.AppState;
import com.tribe.app.R;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.FullscreenNotifications;
import com.tribe.app.presentation.utils.preferences.ImmersiveCallState;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.activity.LiveImmersiveNotificationActivity;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.chat.ChatActivity;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class NotificationBuilder {

  @Inject NotificationManagerCompat notificationManager;
  @Inject Gson gson;
  @Inject TribeApi tribeApi;
  @Inject UserCache userCache;
  @Inject @FullscreenNotifications Preference<Boolean> fullScreenNotifications;
  @Inject @FullscreenNotificationState Preference<Set<String>> fullScreenNotificationState;
  @Inject @ImmersiveCallState Preference<Boolean> immersiveCallState;
  @Inject JobManager jobManager;
  @Inject MissedCallManager missedCallManager;
  @Inject User user;

  @Inject Navigator navigator;
  private AndroidApplication application;

  @Inject public NotificationBuilder(AndroidApplication application) {
    this.application = application;
    this.application.getApplicationComponent().inject(this);
  }

  public void sendBundledNotification(RemoteMessage remoteMessage) {
    NotificationPayload notificationPayload = getPayload(remoteMessage);

    if (notificationPayload != null && !StringUtils.isEmpty(notificationPayload.getClickAction())) {
      if (notificationPayload.getBadge() > 0) {
        userCache.updateBadgeValue(notificationPayload.getBadge());
      }

      if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_END_LIVE)) {
        if (application.getAppState() != null) {
          missedCallManager.setMissedNotificationPlayload(notificationPayload);
        }

        PreferencesUtils.removeFromSet(fullScreenNotificationState,
            notificationPayload.getThread());
      }

      Notification notification = buildNotification(notificationPayload);

      if (application.getAppState() != null && application.getAppState()
          .equals(AppState.FOREGROUND)) {
        Intent intentUnique = new Intent(BroadcastUtils.BROADCAST_NOTIFICATIONS);
        intentUnique.putExtra(BroadcastUtils.NOTIFICATION_PAYLOAD, notificationPayload);
        application.sendBroadcast(intentUnique);

        if (notificationPayload.getClickAction()
            .equals(NotificationPayload.CLICK_ACTION_END_LIVE)) {
          notify(notificationPayload, notification);
        }
      } else {
        if (notification != null) {
          if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_LIVE)
              && fullScreenNotifications.get()
              && !StringUtils.isEmpty(notificationPayload.getSound())
              && !fullScreenNotificationState.get().contains(notificationPayload.getThread())) {
            notification.sound = null;
            sendFullScreenNotification(remoteMessage);
          }

          notify(notificationPayload, notification);
        }

        if (notificationPayload.getClickAction()
            .equals(NotificationPayload.CLICK_ACTION_FRIENDSHIP)) {
          this.tribeApi.getUserInfos(application.getString(R.string.user_infos_friendships,
              application.getString(R.string.userfragment_infos),
              application.getString(R.string.friendshipfragment_info)))
              .subscribe(userRealm -> userCache.put(userRealm));

          notify(notificationPayload, notification);
        }
      }
    }
  }

  private void notify(NotificationPayload notificationPayload, Notification notification) {
    notificationManager.notify(getNotificationId(notificationPayload), notification);
  }

  private NotificationPayload getPayload(RemoteMessage remoteMessage) {
    if (remoteMessage != null && remoteMessage.getData() != null) {
      JsonElement jsonElement = gson.toJsonTree(remoteMessage.getData());
      NotificationPayload payload = gson.fromJson(jsonElement, NotificationPayload.class);
      return payload;
    }

    return null;
  }

  private Notification buildNotification(NotificationPayload payload) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(application);

    if (!StringUtils.isEmpty(payload.getTitle())) {
      builder.setContentText(payload.getBody());
      builder.setContentTitle(payload.getTitle());
    } else {
      builder.setContentText(payload.getBody());
    }

    builder.setWhen(new Date().getTime())
        .setSmallIcon(R.drawable.ic_notification)
        .setShowWhen(true)
        .setAutoCancel(true);

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

  private boolean showMessageNotification(Context context,
      LiveNotificationView liveNotificationView, NotificationPayload notificationPayload) {
    if (liveNotificationView.getContainer() != null) {

      Shortcut notificationShortcut =
          ShortcutUtil.getRecipientFromId(notificationPayload.getUsers_ids(), user);

      if (notificationShortcut != null) {
        if (context instanceof ChatActivity) {
          List<User> memberInChat = null;
          if (((ChatActivity) context).getShortcut() != null
              && ((ChatActivity) context).getShortcut().getMembers() != null) {
            memberInChat = ((ChatActivity) context).getShortcut().getMembers();
          }
          boolean isSameChat =
              ShortcutUtil.equalShortcutMembers(memberInChat, notificationShortcut.getMembers(),
                  user);
          if (isSameChat) {
            return false;
          }
        } else if (context instanceof LiveActivity) {
          List<User> memberInlive = null;
          if (((LiveActivity) context).getShortcut() != null
              && ((LiveActivity) context).getShortcut().getMembers() != null) {
            memberInlive = ((LiveActivity) context).getShortcut().getMembers();
          }
          boolean isSameChat =
              ShortcutUtil.equalShortcutMembers(memberInlive, notificationShortcut.getMembers(),
                  user);
          if (isSameChat) {
            ((LiveActivity) context).notififyNewMessage();
            return false;
          }
        }
      }

      Shortcut finalNotificationShortcut = notificationShortcut;
      liveNotificationView.getContainer().setOnClickListener(view -> {
        if (finalNotificationShortcut != null) {
          if (context instanceof ChatActivity) {
            if (!((ChatActivity) context).getShortcut()
                .getId()
                .equals(finalNotificationShortcut.getId())) {
              navigator.navigateToChat((Activity) context, finalNotificationShortcut, null, null,
                  null, false);
            }
          } else if (context instanceof LiveActivity) {
            // TODO
          } else {
            navigator.navigateToChat((Activity) context, finalNotificationShortcut, null, null,
                null, false);
          }
        }
      });
    }
    return true;
  }

  private PendingIntent getIntentFromPayload(NotificationPayload payload) {
    Class pendingClass = getClassFromPayload(payload);
    if (pendingClass != null) {
      if (pendingClass.equals(LiveActivity.class)) {
        if (payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_JOIN_CALL)) {
          return getPendingIntentFromJoined(payload);
        } else {
          return getPendingIntentForLive(payload);
        }
      } else if (pendingClass.equals(HomeActivity.class) && payload.getClickAction()
          .equals(NotificationPayload.CLICK_ACTION_USER_REGISTERED)) {
        return getPendingIntentForUserRegistered(payload);
      } else if (pendingClass.equals(ChatActivity.class)) {
        return getPendingIntentForChat(payload);
      }
    }

    return getPendingIntentForHome(payload);
  }

  private Class getClassFromPayload(NotificationPayload payload) {
    if (payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_FRIENDSHIP)
        || payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_USER_REGISTERED)) {
      return HomeActivity.class;
    } else if (payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_LIVE)
        || payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_BUZZ)
        || payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_JOIN_CALL)) {
      return LiveActivity.class;
    } else if (payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_MESSAGE)
        || payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_ONLINE)) {
      return ChatActivity.class;
    }

    return HomeActivity.class;
  }

  private NotificationCompat.Builder addActionsForPayload(NotificationCompat.Builder builder,
      NotificationPayload payload) {
    if (!payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_USER_REGISTERED)) {
      return addCommonActions(builder, payload);
    } else {
      return addAddFriendAction(builder, payload);
    }
  }

  private NotificationCompat.Builder addAddFriendAction(NotificationCompat.Builder builder,
      NotificationPayload payload) {
    return builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_notification_grid,
        application.getString(R.string.live_notification_action_add_as_friend),
        getPendingIntentForUserRegistered(payload)).build());
  }

  private NotificationCompat.Builder addCommonActions(NotificationCompat.Builder builder,
      NotificationPayload payload) {
    return builder.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_notification_live,
        application.getString(R.string.live_notification_action_hang_live),
        payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_JOIN_CALL)
            ? getPendingIntentFromJoined(payload) : getPendingIntentForLive(payload)).build());
  }

  private PendingIntent getPendingIntentForLive(NotificationPayload payload) {
    Intent notificationIntent =
        NotificationUtils.getIntentForLive(application, payload, false, user);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    PendingIntent pendingIntent =
        PendingIntent.getActivity(application, (int) System.currentTimeMillis(), notificationIntent,
            PendingIntent.FLAG_ONE_SHOT);

    return pendingIntent;
  }

  private PendingIntent getPendingIntentForChat(NotificationPayload payload) {

    Shortcut notificationShortcut = ShortcutUtil.getRecipientFromId(payload.getUsers_ids(), user);

    Intent intent =
        ChatActivity.getCallingIntent(application, notificationShortcut, null, null, null);

    PendingIntent pendingIntent =
        PendingIntent.getActivity(application, (int) System.currentTimeMillis(), intent,
            PendingIntent.FLAG_ONE_SHOT);

    return pendingIntent;
  }

  private PendingIntent getPendingIntentFromJoined(NotificationPayload payload) {
    Intent notificationIntent = NotificationUtils.getIntentForLiveFromJoined(application, payload);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    PendingIntent pendingIntent =
        PendingIntent.getActivity(application, (int) System.currentTimeMillis(), notificationIntent,
            PendingIntent.FLAG_ONE_SHOT);

    return pendingIntent;
  }

  private PendingIntent getPendingIntentForHome(NotificationPayload payload) {
    Intent notificationIntent = NotificationUtils.getIntentForHome(application, payload);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    PendingIntent pendingIntent =
        PendingIntent.getActivity(application, (int) System.currentTimeMillis(), notificationIntent,
            PendingIntent.FLAG_ONE_SHOT);

    return pendingIntent;
  }

  private PendingIntent getPendingIntentForUserRegistered(NotificationPayload payload) {
    Intent notificationIntent = NotificationUtils.getIntentForHome(application, payload);
    notificationIntent.putExtra(IntentUtils.USER_REGISTERED, payload.getUserId());
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    PendingIntent pendingIntent =
        PendingIntent.getActivity(application, (int) System.currentTimeMillis(), notificationIntent,
            PendingIntent.FLAG_ONE_SHOT);

    return pendingIntent;
  }

  private int getNotificationId(NotificationPayload payload) {
    return !StringUtils.isEmpty(payload.getSessionId()) ? payload.getSessionId().hashCode()
        : (!StringUtils.isEmpty(payload.getThread()) ? payload.getThread().hashCode()
            : (int) System.currentTimeMillis());
  }

  private void sendFullScreenNotification(RemoteMessage remoteMessage) {
    Intent incomingCallIntent = new Intent(application, LiveImmersiveNotificationActivity.class);
    incomingCallIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    NotificationPayload notificationPayload = getPayload(remoteMessage);
    immersiveCallState.set(true);
    incomingCallIntent.putExtra(LiveImmersiveNotificationActivity.PLAYLOAD_VALUE,
        notificationPayload);
    application.startActivity(incomingCallIntent);
  }
}
