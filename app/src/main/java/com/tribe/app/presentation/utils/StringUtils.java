package com.tribe.app.presentation.utils;

import android.content.Context;
import android.util.Patterns;
import com.tribe.app.R;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tiago on 09/08/2016.
 */
public class StringUtils {

  public static boolean isEmpty(String str) {
    return str == null || str.isEmpty();
  }

  public static boolean isOnlyEmoji(String str) {
    String messageStr = str;

    try {
      byte[] utf8Bytes = messageStr.getBytes("UTF-8");
      messageStr = new String(utf8Bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return false;
    }

    Pattern unicodeOutliers =
        Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

    Matcher unicodeOutlierMatcher = unicodeOutliers.matcher(messageStr);

    messageStr = unicodeOutlierMatcher.replaceAll("").trim();

    return messageStr.length() == 0;
  }

  public static boolean isUrl(String str) {
    try {
      URL url = new URL(str);
    } catch (MalformedURLException e) {
      return false;
    }

    return true;
  }

  public static String[] extractLinks(String text) {
    List<String> links = new ArrayList<String>();
    Matcher m = Patterns.WEB_URL.matcher(text);

    while (m.find()) {
      String url = m.group();
      links.add(url);
    }

    return links.toArray(new String[links.size()]);
  }

  public static String getFirstCharacter(String str) {
    if (!StringUtils.isEmpty(str) && str.length() >= 1) {
      String firstCharacter = str.substring(0, 1);
      return firstCharacter;
    }

    return "";
  }

  public static String millisecondsToHhMmSs(long millis) {
    return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(millis)),
        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(millis)));
  }

  public static String getLastBitFromUrl(final String url) {
    return url.replaceFirst(".*/([^/?]+).*", "$1");
  }

  public static String generateLinkId() {
    MessageDigest instance = null;
    try {
      instance = MessageDigest.getInstance("MD5");
      byte[] messageDigest = instance.digest(String.valueOf(System.nanoTime()).getBytes());
      StringBuilder hexString = new StringBuilder();
      for (int i = 0; i < messageDigest.length; i++) {
        String hex = Integer.toHexString(0xFF & messageDigest[i]);
        if (hex.length() == 1) {
          // could use a for loop, but we're only dealing with a single
          // byte
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return "" + String.valueOf(System.currentTimeMillis()).hashCode();
  }

  public static String getUrlFromLinkId(Context context, String linkId) {
    return "https://" + context.getString(R.string.web_host) + "/" + linkId;
  }
}
