package com.tribe.tribelivesdk.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 17/01/2017.
 */

public class JsonUtils {

  public static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  public static String arrayToJson(String[] array) {
    String json = "\"";
    for (int i = 0; i < array.length; i++) {
      if (i == array.length - 1) {
        json += array[i] + "\"";
      } else {
        json += array[i] + "\", \"";
      }
    }
    if (array.length == 0) json += "\"";
    return json;
  }
}
