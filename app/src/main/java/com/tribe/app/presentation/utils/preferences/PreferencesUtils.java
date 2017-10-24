package com.tribe.app.presentation.utils.preferences;

import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.data.network.entity.LookupObject;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tiago on 04/07/2016.
 */
public class PreferencesUtils {

  public static String THEME = "THEME";
  public static String INVISIBLE_MODE = "INVISIBLE_MODE";
  public static String ADDRESS_BOOK = "ADDRESS_BOOK";
  public static String LAST_SYNC = "LAST_SYNC";
  public static String LAST_IM_ONLINE = "LAST_IM_ONLINE";
  public static String PREVIOUS_VERSION_CODE = "PREVIOUS_VERSION_CODE";
  public static String DEBUG_MODE = "DEBUG_MODE";
  public static String UI_SOUNDS = "UI_SOUNDS";
  public static String ROUTING_MODE = "ROUTED_MODE";
  public static String NEW_CONTACT_TOOLTIP = "NEW_CONTACT_TOOLTIP";
  public static String FULLSCREEN_NOTIFICATIONS = "FULLSCREEN_NOTIFICATIONS";
  public static String NUMBER_OF_CALLS = "NUMBER_OF_CALLS";
  public static String NUMBER_OF_MISSED_CALLS = "NUMBER_OF_MISSED_CALLS";
  public static String MISSED_PLAYLOAD_NOTIF = "MISSED_PLAYLOAD_NOTIF";
  public static String COUNTER_CALL_GRP_BTN = "COUNTER_CALL_GRP_BTN";
  public static String IS_GROUPE_CREATED = "IS_GROUPE_CREATED";
  public static String IMMERSIVE_CALL_STATE = "IMMERSIVE_CALL_STATE";
  public static String MINUTES_OF_CALLS = "MINUTES_OF_CALLS";
  public static String FULLSCREEN_NOTIFICATION_STATE = "FULLSCREEN_NOTIFICATION_STATE";
  public static String DATA_CHALLANGES_GAME = "DATA_CHALLANGES_GAME";
  public static String TRIBE_STATE = "TRIBE_STATE";
  public static String CALL_TAGS_MAP = "CALL_TAGS_MAP";
  public static String LOOKUP_RESULT = "LOOKUP_RESULT";
  public static String USER_PHONE_NUMBER = "USER_PHONE_NUMBER";
  public static String WALKTHROUGH = "WALKTHROUGH";

  public static void saveMapAsJson(Map<String, Object> map, Preference<String> preference) {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create();
    Type type = new TypeToken<HashMap<String, Object>>() {
    }.getType();
    preference.set(gson.toJson(map, type));
  }

  public static Map<String, Object> getMapFromJson(Preference<String> preference) {
    return new Gson().fromJson(preference.get(), new TypeToken<HashMap<String, Object>>() {
    }.getType());
  }

  public static void addToSet(Preference<Set<String>> preference, String value) {
    Set<String> newStrSet = new HashSet<>();
    newStrSet.add(value);
    newStrSet.addAll(preference.get());
    preference.set(newStrSet);
  }

  public static void addListToSet(Preference<Set<String>> preference, List<String> values) {
    Set<String> newStrSet = new HashSet<>();
    newStrSet.addAll(values);
    preference.set(newStrSet);
  }

  public static void removeFromSet(Preference<Set<String>> preference, String value) {
    Set<String> newStrSet = new HashSet<>();
    newStrSet.addAll(preference.get());
    newStrSet.remove(value);
    preference.set(newStrSet);
  }

  public static void saveLookupAsJson(List<LookupObject> lookupObjectList,
      Preference<String> preference) {
    Gson gson = new GsonBuilder().create();
    Type type = new TypeToken<List<LookupObject>>() {
    }.getType();
    preference.set(gson.toJson(lookupObjectList, type));
  }

  public static void savePlayloadNotificationAsJson(
      List<NotificationPayload> notificationPayloadList, Preference<String> preference) {
    Gson gson = new GsonBuilder().create();
    Type type = new TypeToken<List<NotificationPayload>>() {
    }.getType();
    preference.set(gson.toJson(notificationPayloadList, type));
  }

  public static List<NotificationPayload> getPlayloadNotificationList(
      Preference<String> preference) {
    return new Gson().fromJson(preference.get(), new TypeToken<List<NotificationPayload>>() {
    }.getType());
  }

  public static List<LookupObject> getLookup(Preference<String> preference) {
    return new Gson().fromJson(preference.get(), new TypeToken<List<LookupObject>>() {
    }.getType());
  }
}
