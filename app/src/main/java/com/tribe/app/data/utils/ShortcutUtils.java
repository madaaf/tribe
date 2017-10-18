package com.tribe.app.data.utils;

/**
 * Created by tiago on 18/10/2017.
 */

public class ShortcutUtils {

  public static String hashShortcut(String excludeId, String[] memberIds) {
    long result = 17;
    for (String id : memberIds) if (!id.equals(excludeId)) result = 37 * result + id.hashCode();
    return String.valueOf(result);
  }
}
