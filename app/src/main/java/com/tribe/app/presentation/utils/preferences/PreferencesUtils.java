package com.tribe.app.presentation.utils.preferences;

import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tiago on 04/07/2016.
 */
public class PreferencesUtils {

  public static String THEME = "THEME";
  public static String INVISIBLE_MODE = "INVISIBLE_MODE";
  public static String ADDRESS_BOOK = "ADDRESS_BOOK";
  public static String LAST_SYNC = "LAST_SYNC";
  public static String PREVIOUS_VERSION_CODE = "PREVIOUS_VERSION_CODE";
  public static String DEBUG_MODE = "DEBUG_MODE";
  public static String UI_SOUNDS = "UI_SOUNDS";
  public static String ROUTING_MODE = "ROUTED_MODE";
  public static String NEW_CONTACT_TOOLTIP = "NEW_CONTACT_TOOLTIP";
  public static String FULLSCREEN_NOTIFICATIONS = "FULLSCREEN_NOTIFICATIONS";
  public static String NUMBER_OF_CALLS = "NUMBER_OF_CALLS";
  public static String COUNTER_CALL_GRP_BTN = "COUNTER_CALL_GRP_BTN";
  public static String IS_GROUPE_CREATED = "IS_GROUPE_CREATED";
  public static String MINUTES_OF_CALLS = "MINUTES_OF_CALLS";
  public static String FULLSCREEN_NOTIFICATION_STATE = "FULLSCREEN_NOTIFICATION_STATE";
  public static String TRIBE_STATE = "TRIBE_STATE";
  public static String CALL_TAGS_MAP = "CALL_TAGS_MAP";

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
}
