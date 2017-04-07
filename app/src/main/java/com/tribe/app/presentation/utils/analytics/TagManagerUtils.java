package com.tribe.app.presentation.utils.analytics;

import android.os.Bundle;
import java.util.Map;

/**
 * Created by tiago on 22/09/2016.
 */
public class TagManagerUtils {

  public static final String EVENT = "event";

  /**
   * GENERAL
   */
  public static final String KPI_Onboarding_Start = "KPI_Onboarding_Start";
  public static final String KPI_Onboarding_SystemNotifications =
      "KPI_Onboarding_SystemNotifications";
  public static final String KPI_Onboarding_VideoSkipped = "KPI_Onboarding_VideoSkipped";
  public static final String KPI_Onboarding_VideoFinished = "KPI_Onboarding_VideoFinished";
  public static final String KPI_Onboarding_PinRequested = "KPI_Onboarding_PinRequested";
  public static final String KPI_Onboarding_PinModified = "KPI_Onboarding_PinModified";
  public static final String KPI_Onboarding_PinConfirmed = "KPI_Onboarding_PinConfirmed";
  public static final String KPI_Onboarding_PinSubmitted = "KPI_Onboarding_PinSubmitted";
  public static final String KPI_Onboarding_PinFailed = "KPI_Onboarding_PinFailed";
  public static final String KPI_Onboarding_PinSucceeded = "KPI_Onboarding_PinSucceeded";
  public static final String KPI_Onboarding_ProfileFilledWithFacebook =
      "KPI_Onboarding_ProfileFilledWithFacebook";
  public static final String KPI_Onboarding_ProfileConfigured = "KPI_Onboarding_ProfileConfigured";
  public static final String KPI_Onboarding_FindFriendsStart = "KPI_Onboarding_FindFriendsStart";
  public static final String KPI_Onboarding_SystemContacts = "KPI_Onboarding_SystemContacts";
  public static final String KPI_Onboarding_FindFriendsNext = "KPI_Onboarding_FindFriendsNext";
  public static final String KPI_Onboarding_FindFriendsSelectAll =
      "KPI_Onboarding_FindFriendsSelectAll";
  public static final String KPI_Onboarding_FindFriendsDone = "KPI_Onboarding_FindFriendsDone";
  public static final String KPI_Onboarding_HomeScreen = "KPI_Onboarding_HomeScreen";
  public static final String KPI_Onboarding_SystemCamera = "KPI_Onboarding_SystemCamera";
  public static final String KPI_Onboarding_SystemMicrophone = "KPI_Onboarding_SystemMicrophone";
  public static final String Groups_Creation = "Groups_Creation";
  public static final String Groups_Infos = "Groups_Infos";
  public static final String Groups_Members = "Groups_Members";
  public static final String Groups_Settings = "Groups_Settings";
  public static final String Invites = "Invites";
  public static final String Logout = "Logout";
  public static final String Calls = "Calls";

  /**
   * ERRORS
   */
  public static final String ERROR = "ERROR";
  public static final String TOKEN_DISCONNECT = "TOKEN_DISCONNECT";

  /**
   * EVENT PROPERTIES
   */
  public static final String ACCEPTED = "accepted";
  public static final String SUCCESS = "success";
  public static final String FAIL = "fail";
  public static final String MEMBERS_COUNT = "members_count";
  public static final String BUTTON = "button";
  public static final String SWIPE = "swipe";
  public static final String SCREEN = "screen";
  public static final String HOME = "home";
  public static final String LIVE = "live";
  public static final String SEARCH = "search";
  public static final String GROUP = "group";
  public static final String ACTION = "action";
  public static final String UNKNOWN = "unknown";
  public static final String CANCELLED = "cancelled";
  public static final String CREATED = "created";
  public static final String MEMBERS = "members";
  public static final String SETTINGS = "settings";
  public static final String MODIFIED = "modified";
  public static final String MEMBERS_ADDED_COUNT = "members_added_count";
  public static final String NOTIFICATIONS_ENABLED = "notifications_enabled";
  public static final String LEFT = "left";
  public static final String DURATION = "duration";
  public static final String TYPE = "type";
  public static final String DIRECT = "direct";
  public static final String AVERAGE_MEMBERS_COUNT = "average_members_count";
  public static final String STATE = "state";
  public static final String MISSED = "missed";
  public static final String ENDED = "ended";
  public static final String WIZZ_COUNT = "wizz_count";
  public static final String MEMBERS_INVITED = "members_invited";

  /**
   * USER PROPERTIES
   */

  public static final String USER_ADDRESS_BOOK_ENABLED = "user_address_book_enabled";
  public static final String USER_NOTIFICATIONS_ENABLED = "user_notifications_enabled";
  public static final String USER_MICROPHONE_ENABLED = "user_microphone_enabled";
  public static final String USER_CAMERA_ENABLED = "user_camera_enabled";
  public static final String USER_DISPLAY_NAME = "user_display_name";
  public static final String USER_EMAIL = "user_email";
  public static final String USER_USERNAME = "user_username";
  public static final String USER_FACEBOOK_CONNECTED = "user_facebook_connected";
  public static final String USER_FRIENDS_COUNT = "user_friends_count";
  public static final String USER_GROUPS_COUNT = "user_groups_count";
  public static final String USER_INVISIBLE_ENABLED = "user_invisible_enabled";
  public static final String USER_CALLS_COUNT = "user_calls_count";
  public static final String USER_CALLS_MINUTES = "user_calls_minutes";
  public static final String USER_CALLS_MISSED_COUNT = "user_calls_missed_count";
  public static final String USER_INVITES_SENT_COUNT = "user_invites_sent_count";
  public static final String USER_HAS_PROFILE_PICTURE = "user_has_profile_picture";
  public static final String USER_ONBOARDING_COMPLETED = "user_onboarding_completed";

  /**
   * MIXPANEL PROPERTIES
   */
  public static final String RATE = "rate";
  public static final String ROOM_ID = "room_id";

  public static void manageTags(TagManager tagManager, Map<String, Object> tagMap) {
    Bundle bundle = new Bundle();

    for (Map.Entry<String, Object> entry : tagMap.entrySet()) {
      if (!entry.getKey().equals(TagManagerUtils.EVENT)) {
        if (entry.getValue() instanceof Integer) {
          bundle.putInt(entry.getKey(), (int) entry.getValue());
        } else if (entry.getValue() instanceof Double) {
          bundle.putDouble(entry.getKey(), (double) entry.getValue());
        } else if (entry.getValue() instanceof String) {
          bundle.putString(entry.getKey(), (String) entry.getValue());
        } else if (entry.getValue() instanceof Boolean) {
          bundle.putBoolean(entry.getKey(), (boolean) entry.getValue());
        }
      }
    }

    tagManager.trackEvent((String) tagMap.get(TagManagerUtils.EVENT), bundle);
    tagMap.clear();
  }
}
