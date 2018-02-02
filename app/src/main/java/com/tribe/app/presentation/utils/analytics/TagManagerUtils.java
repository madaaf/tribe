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
  public static final String KPI_Onboarding_AuthenticationSuccess =
      "KPI_Onboarding_AuthenticationSuccess";
  public static final String KPI_Onboarding_AuthenticationError =
      "KPI_Onboarding_AuthenticationError";
  public static final String KPI_Onboarding_OpenNewCallFacebook =
      "KPI_Onboarding_OpenNewCallFacebook";
  public static final String KPI_Onboarding_PinFailed = "KPI_Onboarding_PinFailed";
  public static final String KPI_Onboarding_PinSucceeded = "KPI_Onboarding_PinSucceeded";
  public static final String KPI_Onboarding_ProfileFilledWithFacebook =
      "KPI_Onboarding_ProfileFilledWithFacebook";
  public static final String KPI_Onboarding_ProfileConfigured = "KPI_Onboarding_ProfileConfigured";
  public static final String KPI_Onboarding_SystemContacts = "KPI_Onboarding_SystemContacts";
  public static final String KPI_Onboarding_HomeScreen = "KPI_Onboarding_HomeScreen";
  public static final String KPI_Onboarding_SystemCamera = "KPI_Onboarding_SystemCamera";
  public static final String KPI_Onboarding_SystemMicrophone = "KPI_Onboarding_SystemMicrophone";
  public static final String KPI_Onboarding_Phone_Button = "KPI_Onboarding_Phone_Button";
  public static final String KPI_Onboarding_Facebook_Button = "KPI_Onboarding_Facebook_Button";
  public static final String Invites = "Invites";
  public static final String Logout = "Logout";
  public static final String Calls = "Calls";
  public static final String Notification_AppOpen = "Notification_AppOpen";
  public static final String FacebookGate = "FacebookGate";
  public static final String AddFriend = "AddFriend";
  public static final String NewChat = "NewChat";
  public static final String EditGroupName = "EditGroupName";
  public static final String Shortcut = "Shortcut";
  public static final String Chats = "Chats";
  public static final String NewGame = "NewGame";
  public static final String Searched = "Searched";
  public static final String KPI_Onboarding_WalkthroughCompleted =
      "KPI_Onboarding_WalkthroughCompleted";
  public static final String Technical_Error = "Technical_Error";
  public static final String Games = "Games";

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
  public static final String MEMBERS = "members";
  public static final String SAVE = "save";
  public static final String BUTTON = "button";
  public static final String SWIPE = "swipe";
  public static final String SCREEN = "screen";
  public static final String HOME = "home";
  public static final String NEW_GAME = "new_game";
  public static final String LAUNCHED = "launched";
  public static final String SUGGESTED = "suggested";
  public static final String LIVE = "live";
  public static final String CALL = "call";
  public static final String SEARCH = "search";
  public static final String INVITE = "invite";
  public static final String CHAT = "chat";
  public static final String ONBOARDING = "onboarding";
  public static final String PROFILE = "profile";
  public static final String ACTION = "action";
  public static final String NAME = "name";
  public static final String GAME = "game";
  public static final String UNKNOWN = "unknown";
  public static final String CANCELLED = "cancelled";
  public static final String CANCEL = "cancel";
  public static final String DURATION = "duration";
  public static final String IS_CALL_ROULETTE = "is_call_roulette";
  public static final String TYPE = "type";
  public static final String PLATFORM = "platform";
  public static final String PLATFORM_FACEBOOK = "facebook";
  public static final String PLATFORM_PHONE = "phone";
  public static final String DIRECT = "direct";
  public static final String AVERAGE_MEMBERS_COUNT = "average_members_count";
  public static final String STATE = "state";
  public static final String MISSED = "missed";
  public static final String ENDED = "ended";
  public static final String WIZZ_COUNT = "wizz_count";
  public static final String SCREENSHOT_COUNT = "screenshot_count";
  public static final String MEMBERS_INVITED = "members_invited";
  public static final String POST_IT_GAME_COUNT = "post_it_game_count";
  public static final String CATEGORY = "category";
  public static final String SOURCE = "source";
  public static final String GESTURE = "gesture";
  public static final String GESTURE_TAP = "tap";
  public static final String GESTURE_SWIPE = "swipe";
  public static final String SECTION = "section";
  public static final String SECTION_ONLINE = "online";
  public static final String SECTION_RECENT = "recent";
  public static final String SECTION_ONGOING = "ongoing";
  public static final String SECTION_SHORTCUT = "shortcut";
  public static final String HEART_COUNT = "heart_count";
  public static final String TEXT_COUNT = "text_count";
  public static final String AUDIO_COUNT = "audio_count";
  public static final String IMAGE_COUNT = "image_count";
  public static final String GAME_COUNT = "game_count";
  public static final String GAME_DURATION = "game_duration";
  public static final String SIGNUP = "signup";
  public static final String CONTENT_TYPE = "contentType";
  public static final String SEARCH_STRING = "searchString";
  public static final String TYPE_SOLO = "solo";
  public static final String TYPE_MULTI = "multi";

  public static final String tagGameCountSuffix = "_game_count";
  public static final String tagGameDurationSuffix = "_game_duration";
  public static final String tagGameAverageMembersSuffix = "_members_count";

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
  public static final String USER_INVISIBLE_ENABLED = "user_invisible_enabled";
  public static final String USER_CALLS_COUNT = "user_calls_count";
  public static final String USER_CALLS_MINUTES = "user_calls_minutes";
  public static final String USER_CALLS_MISSED_COUNT = "user_calls_missed_count";
  public static final String USER_INVITES_SENT_COUNT = "user_invites_sent_count";
  public static final String USER_HAS_PROFILE_PICTURE = "user_has_profile_picture";
  public static final String USER_ONBOARDING_COMPLETED = "user_onboarding_completed";
  public static final String USER_LANGUAGE = "user_language";
  public static final String FB_ACTION_CANCELLED = "cancelled";
  public static final String FB_ACTION_FAILED = "failed";
  public static final String FB_ACTION_SUCCESS = "succeeded";
  public static final String FB_ACTION = "action";

  /**
   * ERRORS
   */

  public static final String DESCRIPTION = "Description";
  public static final String FEATURE = "Feature";
  public static final String REASON = "Reason";
  public static final String SERVER = "Server";
  public static final String SIGNALING_ERROR_CODE = "SignalingErrorCode";
  public static final String SIGNALING_ERROR = "SignalingError";

  public static final String ROOM_STATUS = "Room Status";
  public static final String ONBOARDING_FB = "Onboarding - Facebook";
  public static final String ONBOARDING_PHONE = "Onboarding - Phone";
  public static final String NO_SOCKET = "No Socket";
  public static final String NOT_URL = "Not an URL";
  public static final String WEB_SIGNALING = "WebSignaling";

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
