package com.tribe.app.presentation.view.notification;

import android.content.Context;
import android.content.Intent;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.activity.MissedCallDetailActivity;
import com.tribe.app.presentation.view.activity.ProfileActivity;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import java.util.List;

/**
 * Created by madaaflak on 20/02/2017.
 */

public class NotificationUtils {

  public static final String ACTION_ADD_AS_GUEST = "guest";
  public static final String ACTION_HANG_LIVE = "hang";
  public static final String ACTION_LEAVE = "leave";
  public static final String ACTION_MISSED_CALL_DETAIL = "missed";
  public static final String ACTION_DECLINE = "decline";

  public static LiveNotificationView getNotificationViewFromPayload(Context context,
      NotificationPayload notificationPayload, MissedCallManager missedCallManager) {
    boolean isContextNotLive =
        context instanceof HomeActivity || context instanceof ProfileActivity;

    if (notificationPayload == null) {
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      return builder.build();
    }

    if (notificationPayload.getClickAction() == null) return null;

    LiveNotificationView liveNotificationView = null;

    if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_END_LIVE)) {
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder = addMissedCallActions(context, builder, notificationPayload);
      liveNotificationView = builder.build();
      missedCallManager.reset();
    }

    if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_ONLINE)
        && !isContextNotLive) {
      // A friend opened the app and is online
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder = addLiveActions(context, builder, notificationPayload);
      builder.sound(SoundManager.FRIEND_ONLINE);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_LIVE)
        && StringUtils.isEmpty(notificationPayload.getGroupId())) {
      // A friend entered live - 1o1
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);

      //if (StringUtils.isEmpty(notificationPayload.getGroupId())) {
      if (isContextNotLive) {
        builder = addHangLiveAction(context, builder, notificationPayload);
      } else {
        builder = addLiveActions(context, builder, notificationPayload);
      }

      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_LIVE)
        && !StringUtils.isEmpty(notificationPayload.getGroupId())) {
      // A friend entered live - Group
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder = addHangLiveAction(context, builder, notificationPayload);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_BUZZ)) {
      // A friend buzzing you in a group
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);

      if (StringUtils.isEmpty(notificationPayload.getGroupId())) {
        if (isContextNotLive) {
          builder = addHangLiveAction(context, builder, notificationPayload);
        } else {
          builder = addLiveActions(context, builder, notificationPayload);
        }
      } else {
        builder = addHangLiveAction(context, builder, notificationPayload);
      }
      builder.sound(SoundManager.WIZZ);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_FRIENDSHIP)) {
      // A friend added you
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);

      if (isContextNotLive) {
        builder = addHangLiveAction(context, builder, notificationPayload);
      } else {
        builder = addLiveActions(context, builder, notificationPayload);
      }

      builder.sound(SoundManager.FRIEND_ONLINE);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_DECLINE)) {

      liveNotificationView =
          buildDeclinedCallNotification(context, liveNotificationView, notificationPayload);
    }

    return liveNotificationView;
  }

  private static LiveNotificationView.Builder getCommonBuilder(Context context,
      NotificationPayload notificationPayload) {
    return new LiveNotificationView.Builder(context,
        notificationPayload.isLive() ? LiveNotificationView.LIVE
            : LiveNotificationView.ONLINE).imgUrl(notificationPayload.getUserPicture())
        .title(notificationPayload.getBody());
  }

  private static LiveNotificationView.Builder addHangLiveAction(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {

    String title;
    if (StringUtils.isEmpty(notificationPayload.getGroupId())) {
      title = context.getString(R.string.live_notification_action_hang_live_friend,
          notificationPayload.getUserDisplayName());
    } else {
      title = context.getString(R.string.live_notification_action_hang_live_group,
          notificationPayload.getGroupName());
    }

    builder.addAction(ACTION_HANG_LIVE, title,
        getIntentForLive(context, notificationPayload, false));

    if (notificationPayload.isLive()) addDeclineCallActions(context, builder, notificationPayload);

    return builder;
  }

  private static LiveNotificationView.Builder addDeclineCallActions(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {

    builder.addAction(ACTION_DECLINE, context.getString(R.string.live_notification_action_decline),
        notificationPayload.getSessionId());

    return builder;
  }

  private static LiveNotificationView buildDeclinedCallNotification(Context context,
      LiveNotificationView liveNotifView, NotificationPayload notificationPayload) {
    String title;
    if (StringUtils.isEmpty(notificationPayload.getGroupId())) {
      title = EmojiParser.demojizedText(context.getString(R.string.live_notification_guest_declined,
          notificationPayload.getUserDisplayName()));
    } else {
      title = EmojiParser.demojizedText(
          context.getString(R.string.live_notification_action_hang_live_group,
              notificationPayload.getGroupName()));
    }
    notificationPayload.setBody(title);
    LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
    builder.sound(SoundManager.NO_SOUND);
    liveNotifView = builder.build();
    return liveNotifView;
  }

  private static LiveNotificationView.Builder addMissedCallActions(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {
    List<MissedCallAction> missedCallAction = notificationPayload.getMissedCallList();
    if (missedCallAction.size() < MissedCallManager.MAX_NBR_MISSED_USER_CALL) {
      for (MissedCallAction missedCall : missedCallAction) {
        builder.addAction(ACTION_HANG_LIVE, missedCall.getNotificationPayload().getBody(),
            getIntentForLive(context, missedCall.getNotificationPayload(), false));
      }
    } else {
      builder.addAction(ACTION_MISSED_CALL_DETAIL,
          context.getString(R.string.callback_notification_default_action),
          MissedCallDetailActivity.getIntentForMissedCallDetail(context, missedCallAction));
    }
    builder.sound(SoundManager.NO_SOUND);
    return builder;
  }

  private static LiveNotificationView.Builder addLiveActions(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {
    if (notificationPayload.isShouldDisplayDrag()) {
      builder.addAction(ACTION_ADD_AS_GUEST,
          context.getString(R.string.live_notification_add_as_guest,
              notificationPayload.getUserDisplayName()));
    }

    builder = addLeaveAction(context, builder, notificationPayload);
    if (notificationPayload.isLive()) {
      builder = addDeclineCallActions(context, builder, notificationPayload);
    }
    return builder;
  }

  private static LiveNotificationView.Builder addLeaveAction(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {

    builder.addAction(ACTION_LEAVE, context.getString(R.string.live_notification_leave,
        notificationPayload.getUserDisplayName()),
        getIntentForLive(context, notificationPayload, false));

    return builder;
  }

  public static Intent getIntentForLive(Context context, NotificationPayload payload,
      boolean isFromCallkit) {
    String recipientId =
        !StringUtils.isEmpty(payload.getGroupId()) ? payload.getGroupId() : payload.getUserId();
    boolean isGroup = !StringUtils.isEmpty(payload.getGroupId());
    String sessionId = payload.getSessionId();
    String name = isGroup ? payload.getGroupName() : payload.getUserDisplayName();
    String picture = isGroup ? payload.getGroupPicture() : payload.getUserPicture();

    String source = "";
    if (isFromCallkit) {
      source = LiveActivity.SOURCE_CALLKIT;
    } else {
      source = payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_ONLINE)
          ? LiveActivity.SOURCE_ONLINE_NOTIFICATION : LiveActivity.SOURCE_LIVE_NOTIFICATION;
    }

    Intent intent =
        LiveActivity.getCallingIntent(context, recipientId, isGroup, picture, name, sessionId,
            source);
    intent.putExtra(Constants.NOTIFICATION_LIVE, payload.getClickAction());
    return intent;
  }

  public static Intent getIntentForHome(Context context, NotificationPayload payload) {
    Intent intent = new Intent(context, HomeActivity.class);
    if (payload != null) intent.putExtra(Constants.NOTIFICATION_HOME, payload.getClickAction());
    return intent;
  }
}
