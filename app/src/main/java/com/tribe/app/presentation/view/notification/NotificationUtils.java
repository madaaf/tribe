package com.tribe.app.presentation.view.notification;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.IntentUtils;
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
  public static final String ACTION_JOIN = "join";
  public static final String ACTION_LEAVE = "leave";
  public static final String ACTION_MISSED_CALL_DETAIL = "missed";
  public static final String ACTION_DECLINE = "decline";
  public static final String ACTION_ADD_FRIEND = "add_friend";

  public static LiveNotificationView getNotificationViewFromPayload(Context context,
      NotificationPayload notificationPayload, MissedCallManager missedCallManager) {
    boolean isContextNotLive =
        context instanceof HomeActivity || context instanceof ProfileActivity;

    if (notificationPayload == null) {
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      return builder.build();
    }

    if (notificationPayload.getClickAction() == null &&
        StringUtils.isEmpty(notificationPayload.getBody())) {
      return null;
    } else if (notificationPayload.getClickAction() == null &&
        !StringUtils.isEmpty(notificationPayload.getBody())) {
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      return builder.build();
    }

    LiveNotificationView liveNotificationView = null;

    if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_END_LIVE)) {
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder = addMissedCallActions(context, builder, notificationPayload);
      liveNotificationView = builder.build();
      missedCallManager.reset();
    }

    if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_ONLINE) &&
        !isContextNotLive) {
      // A friend opened the app and is online
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder.sound(SoundManager.FRIEND_ONLINE);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_LIVE)) {
      // A friend entered live - 1o1
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_BUZZ)) {
      // A friend buzzing you in a group
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder.sound(SoundManager.WIZZ);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_JOIN_CALL)) {
      // A joined a call you initiated via a link shared
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder.sound(SoundManager.JOIN_CALL);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_FRIENDSHIP)) {
      // A friend added you
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      builder.sound(SoundManager.FRIEND_ONLINE);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_DECLINE)) {

      liveNotificationView =
          buildDeclinedCallNotification(context, liveNotificationView, notificationPayload);
    } else if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_USER_REGISTERED)) {

      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      liveNotificationView = builder.build();
    } else if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_MESSAGE)) {
      // A friend added you
      LiveNotificationView.Builder builder = getCommonBuilder(context, notificationPayload);
      liveNotificationView = builder.build();
    }

    return liveNotificationView;
  }

  private static LiveNotificationView.Builder getCommonBuilder(Context context,
      NotificationPayload notificationPayload) {
    return new LiveNotificationView.Builder(context,
        notificationPayload.isLive() ? LiveNotificationView.LIVE
            : LiveNotificationView.ONLINE).userImgUrl(notificationPayload.getUserPicture())
        .title(notificationPayload.getTitle())
        .body(notificationPayload.getBody())
        .messagePictureUrl(notificationPayload.getMessagePicture())
        .actionClick(notificationPayload.getClickAction());
  }

  private static LiveNotificationView.Builder addDeclineCallActions(Context context,
      LiveNotificationView.Builder builder, NotificationPayload notificationPayload) {

    builder.addAction(ACTION_DECLINE, context.getString(R.string.live_notification_action_decline),
        notificationPayload.getSessionId());

    return builder;
  }

  private static LiveNotificationView buildDeclinedCallNotification(Context context,
      LiveNotificationView liveNotifView, NotificationPayload notificationPayload) {
    String title = EmojiParser.demojizedText(
        context.getString(R.string.live_notification_guest_declined,
            notificationPayload.getUserDisplayName()));
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
    builder.actionClick(notificationPayload.getClickAction()).sound(SoundManager.NO_SOUND);
    return builder;
  }

  public static Intent getIntentForLive(Context context, NotificationPayload payload,
      boolean isFromCallkit) {
    String recipientId = payload.getUserId();
    String sessionId = payload.getSessionId();
    String name = payload.getUserDisplayName();
    String picture = payload.getUserPicture();

    String source = "";
    if (isFromCallkit) {
      source = LiveActivity.SOURCE_CALLKIT;
    } else {
      source = payload.getClickAction().equals(NotificationPayload.CLICK_ACTION_ONLINE)
          ? LiveActivity.SOURCE_ONLINE_NOTIFICATION : LiveActivity.SOURCE_LIVE_NOTIFICATION;
    }

    Intent intent =
        LiveActivity.getCallingIntent(context, recipientId, picture, name, sessionId, source);
    intent.putExtra(Constants.NOTIFICATION_LIVE, payload.getClickAction());
    return intent;
  }

  public static Intent getIntentForLiveFromJoined(Context context, NotificationPayload payload) {
    Intent intent = IntentUtils.getLiveIntentFromURI(context, Uri.parse(payload.getRoomLink()),
        LiveActivity.SOURCE_JOIN_LIVE);
    intent.putExtra(Constants.NOTIFICATION_LIVE, payload.getClickAction());
    return intent;
  }

  public static Intent getIntentForHome(Context context, NotificationPayload payload) {
    Intent intent = new Intent(context, HomeActivity.class);
    if (payload != null) intent.putExtra(Constants.NOTIFICATION_HOME, payload.getClickAction());
    return intent;
  }
}
