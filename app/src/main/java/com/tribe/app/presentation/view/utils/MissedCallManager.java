package com.tribe.app.presentation.view.utils;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.preferences.MissedPlayloadNotification;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.notification.MissedCallAction;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by madaaflak on 26/04/2017.
 */

@Singleton public class MissedCallManager {

  public static final int MAX_NBR_MISSED_USER_CALL = 1;

  private Preference<String> missedNotificationPlayloadPreference;
  private List<NotificationPayload> notificationPayloadList = new ArrayList<>();
  private Context context;

  @Inject public MissedCallManager(Context context,
      @MissedPlayloadNotification Preference<String> missedNotificationPlayloadPreference) {
    this.missedNotificationPlayloadPreference = missedNotificationPlayloadPreference;
    this.context = context;
    this.notificationPayloadList = getNotificationPayloadList();
  }

  public void setMissedNotificationPlayload(NotificationPayload playload) {
    playload.setTime(System.currentTimeMillis());
    notificationPayloadList.add(playload);
    PreferencesUtils.savePlayloadNotificationAsJson(notificationPayloadList,
        missedNotificationPlayloadPreference);
  }

  public List<NotificationPayload> getNotificationPayloadList() {
    if (PreferencesUtils.getPlayloadNotificationList(missedNotificationPlayloadPreference)
        == null) {
      return new ArrayList<>();
    }
    return PreferencesUtils.getPlayloadNotificationList(missedNotificationPlayloadPreference);
  }

  public int getNbrOfMissedCall() {
    return getNotificationPayloadList().size();
  }

  public List<MissedCallAction> getMissedCallAction() {
    List<String> missedCallUserIdList = new ArrayList<>();
    Map<String, MissedCallAction> missedCallsActionList = new HashMap<>();

    MissedCallAction missedCallAction;

    for (NotificationPayload playload : notificationPayloadList) {
      missedCallAction = new MissedCallAction(playload.getUserId(), playload, 1);
      if (!missedCallUserIdList.contains(playload.getUserId())) {
        missedCallUserIdList.add(playload.getUserId());
        missedCallsActionList.put(missedCallAction.getUserId(), missedCallAction);
      } else {
        missedCallAction = missedCallsActionList.get(playload.getUserId());
        missedCallAction.setNbrMissedCall(missedCallAction.getNbrMissedCall() + 1);
      }
    }
    List<MissedCallAction> formatedMissedCallList = new ArrayList<>(missedCallsActionList.values());

    for (MissedCallAction missedCall : formatedMissedCallList) {
      String notificationActionTitle = context.getString(R.string.callback_notification_action,
          missedCall.getNotificationPayload().getUserDisplayName(),
          Integer.toString(missedCall.getNbrMissedCall()));
      missedCall.getNotificationPayload().setClickAction(NotificationPayload.CLICK_ACTION_LIVE);
      missedCall.getNotificationPayload().setBody(notificationActionTitle);
      missedCall.getNotificationPayload()
          .setUserDisplayName(missedCall.getNotificationPayload().getTitle());
    }

    return formatedMissedCallList;
  }

  public NotificationPayload buildNotificationBuilderFromMissedCallList() {
    NotificationPayload notificationPayload = new NotificationPayload();

    List<String> missedCallUserIdList = new ArrayList<>();
    Map<String, MissedCallAction> missedCallsActionList = new HashMap<>();

    MissedCallAction missedCallAction;

    for (NotificationPayload playload : notificationPayloadList) {
      missedCallAction = new MissedCallAction(playload.getUserId(), playload, 1);
      if (!missedCallUserIdList.contains(playload.getUserId())) {
        missedCallUserIdList.add(playload.getUserId());
        missedCallsActionList.put(missedCallAction.getUserId(), missedCallAction);
      } else {
        missedCallAction = missedCallsActionList.get(playload.getUserId());
        missedCallAction.setNbrMissedCall(missedCallAction.getNbrMissedCall() + 1);
      }
    }

    List<MissedCallAction> formatedMissedCallList = new ArrayList<>(missedCallsActionList.values());

    for (MissedCallAction missedCall : formatedMissedCallList) {
      String notificationActionTitle = context.getString(R.string.callback_notification_action,
          missedCall.getNotificationPayload().getUserDisplayName(),
          Integer.toString(missedCall.getNbrMissedCall()));
      /*missedCallActionTitleList.add(notificationActionTitle);*/
      missedCall.getNotificationPayload().setClickAction(NotificationPayload.CLICK_ACTION_LIVE);
      missedCall.getNotificationPayload().setBody(notificationActionTitle);
    }

    String titleNotif = context.getString(R.string.callback_notification_title_one);
    if (getNbrOfMissedCall() > 1) {
      titleNotif =
          context.getString(R.string.callback_notification_title_multiple, getNbrOfMissedCall());
    }
    notificationPayload.setBody(titleNotif);
    notificationPayload.setMissedCallActionList(formatedMissedCallList);
    notificationPayload.setClickAction(NotificationPayload.CLICK_ACTION_END_LIVE);
    return notificationPayload;
  }

  public void reset() {
    notificationPayloadList.clear();
    PreferencesUtils.savePlayloadNotificationAsJson(notificationPayloadList,
        missedNotificationPlayloadPreference);
  }
}
