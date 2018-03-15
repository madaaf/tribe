package com.tribe.app.presentation.view.component.live.game.corona;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonHelper {

  public static Object toJSON(Object object) throws JSONException {
    if (object instanceof Map) {
      JSONObject json = new JSONObject();
      Map map = (Map) object;
      for (Object key : map.keySet()) {
        json.put(key.toString(), toJSON(map.get(key)));
      }
      return json;
    } else if (object instanceof Iterable) {
      JSONArray json = new JSONArray();
      for (Object value : ((Iterable) object)) {
        json.put(value);
      }
      return json;
    } else {
      return object;
    }
  }

  public static boolean isEmptyObject(JSONObject object) {
    return object.names() == null;
  }

  public static Hashtable<Object, Object> getHashtable(JSONObject object, String key)
      throws JSONException {
    return toHashtable(object.getJSONObject(key));
  }

  public static Hashtable<Object, Object> toHashtable(JSONObject object) throws JSONException {
    Hashtable<Object, Object> table = new Hashtable<>();
    Iterator keys = object.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      table.put(key, fromJson(object.get(key)));
    }
    return table;
  }

  public static Hashtable<Object, Object> toHashtableWithFakeIndex(JSONArray array)
      throws JSONException {
    Hashtable<Object, Object> table = new Hashtable<>();
    int j = 1;
    for (int i = 0; i < array.length(); i++) {
      table.put(j, fromJson(array.get(i)));
      j++;
    }
    return table;
  }

  public static List toList(JSONArray array) throws JSONException {
    List list = new ArrayList();
    for (int i = 0; i < array.length(); i++) {
      list.add(fromJson(array.get(i)));
    }
    return list;
  }

  private static Object fromJson(Object json) throws JSONException {
    if (json == JSONObject.NULL) {
      return null;
    } else if (json instanceof JSONObject) {
      return toHashtable((JSONObject) json);
    } else if (json instanceof JSONArray) {
      return toHashtableWithFakeIndex((JSONArray) json);
    } else {
      return json;
    }
  }
}