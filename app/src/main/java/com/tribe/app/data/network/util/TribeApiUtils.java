package com.tribe.app.data.network.util;

public class TribeApiUtils {

  public static String getUnixTimeStamp() {
    return String.valueOf(System.currentTimeMillis() / 1000L);
  }
}
