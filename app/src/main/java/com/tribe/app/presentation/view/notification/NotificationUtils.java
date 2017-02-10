package com.tribe.app.presentation.view.notification;

import android.content.Context;
import android.content.Intent;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;

/**
 * Created by tiago on 01/02/2017.
 */

public class NotificationUtils {

  public static final String ACTION_ADD_AS_GUEST = "guest";
  public static final String ACTION_HANG_LIVE = "hang";
  public static final String ACTION_LEAVE = "leave";

  public static LiveNotificationView getNotificationViewFromPayload(Context context,
      NotificationPayload notificationPayload) {
    boolean isGrid = context instanceof HomeActivity;

    LiveNotificationView notificationView = null;

    if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_ONLINE)
        && !isGrid) {
      // A friend opened the app and is online
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder = addLiveActions(context, builder, notificationPayload);
      builder.sound(SoundManager.FRIEND_ONLINE);
      notificationView = builder.build();
    } else if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_LIVE)
        && StringUtils.isEmpty(notificationPayload.getGroupId())) {
      // A friend entered live - 1o1
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);

      //if (StringUtils.isEmpty(notificationPayload.getGroupId())) {
      if (isGrid) {
        builder = addHangLiveAction(context, builder, notificationPayload);
      } else {
        builder = addLiveActions(context, builder, notificationPayload);
      }
      //} else {
      //  if (isGrid) {
      //    builder = addHangLiveAction(context, builder, notificationPayload);
      //  } else {
      //    builder = addLeaveAction(context, builder, notificationPayload);
      //  }
      //}
      notificationView = builder.build();
    } else if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_LIVE)
        && !StringUtils.isEmpty(notificationPayload.getGroupId())) {
      // A friend entered live - Group
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder = addHangLiveAction(context, builder, notificationPayload);
      notificationView = builder.build();
    } else if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_BUZZ)) {
      // A friend buzzing you in a group
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);

      if (StringUtils.isEmpty(notificationPayload.getGroupId())) {
        if (isGrid) {
          builder = addHangLiveAction(context, builder, notificationPayload);
        } else {
          builder = addLiveActions(context, builder, notificationPayload);
        }
      } else {
        builder = addHangLiveAction(context, builder, notificationPayload);
      }
      builder.sound(SoundManager.WIZZ);
      notificationView = builder.build();
    } else if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_FRIENDSHIP)) {
      // A friend added you
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);

      if (isGrid) {
        builder = addHangLiveAction(context, builder, notificationPayload);
      } else {
        builder = addLiveActions(context, builder, notificationPayload);
      }

      builder.sound(SoundManager.FRIEND_ONLINE);
      notificationView = builder.build();
    }

    return notificationView;
  }

  private static LiveNotificationView.Builder getCommonBuilder(Context context,
      NotificationPayload notificationPayload) {
    return new LiveNotificationView.Builder(context, LiveNotificationView.LIVE).imgUrl(
        notificationPayload.getUserPicture()).title(notificationPayload.getBody());
  }

  private static LiveNotificationView.Builder addHangLiveAction(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {
    if (StringUtils.isEmpty(notificationPayload.getGroupId())) {
      //if (StringUtils.isEmpty(notificationPayload.getOtherUserDisplayNames())) {
      return builder.addAction(ACTION_HANG_LIVE,
          context.getString(R.string.live_notification_action_hang_live_friend,
              notificationPayload.getUserDisplayName()),
          getIntentForLive(context, notificationPayload));
      //} else {
      //  return builder.addAction(ACTION_HANG_LIVE,
      //      context.getString(R.string.live_notification_action_hang_live_guests,
      //          notificationPayload.getUserDisplayName()),
      //      getIntentForLive(context, notificationPayload));
      //}
    } else {
      return builder.addAction(ACTION_HANG_LIVE,
          context.getString(R.string.live_notification_action_hang_live_group,
              notificationPayload.getGroupName()), getIntentForLive(context, notificationPayload));
    }
  }

  private static LiveNotificationView.Builder addLiveActions(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {
    if (notificationPayload.isShouldDisplayDrag()) {
      builder.addAction(ACTION_ADD_AS_GUEST,
          context.getString(R.string.live_notification_add_as_guest,
              notificationPayload.getUserDisplayName()), null);
    }

    builder = addLeaveAction(context, builder, notificationPayload);

    return builder;
  }

  private static LiveNotificationView.Builder addLeaveAction(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {
    //if (StringUtils.isEmpty(notificationPayload.getOtherUserDisplayNames())) {
    return builder.addAction(ACTION_LEAVE, context.getString(R.string.live_notification_leave,
        notificationPayload.getUserDisplayName()), getIntentForLive(context, notificationPayload));
    //} else {
    //  return builder.addAction(ACTION_LEAVE,
    //      context.getString(R.string.live_notification_leave_guests,
    //          notificationPayload.getUserDisplayName()),
    //      getIntentForLive(context, notificationPayload));
    //}
  }

  public static Intent getIntentForLive(Context context, NotificationPayload payload) {
    String recipientId =
        !StringUtils.isEmpty(payload.getGroupId()) ? payload.getGroupId() : payload.getUserId();
    boolean isGroup = !StringUtils.isEmpty(payload.getGroupId());
    String sessionId = payload.getSessionId();
    return LiveActivity.getCallingIntent(context, recipientId, isGroup, sessionId);
  }

  public static Intent getIntentForHome(Context context, NotificationPayload payload) {
    return new Intent(context, HomeActivity.class);
  }
}
