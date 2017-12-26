package com.tribe.app.presentation.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Patterns;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tiago on 09/08/2016.
 */
public class StringUtils {

  private static final String characters =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijiklmnopqrstuvxyz0123456789";
  private static final int LENGHT_LINK_ID = 6;

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

  public static String usernameFromEmail(String email) {
    return email.split("@")[0];
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
        TimeUnit.MILLISECONDS.toMinutes(millis) -
            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
        TimeUnit.MILLISECONDS.toSeconds(millis) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
  }

  public static String getLastBitFromUrl(final String url) {
    return url.replaceFirst(".*/([^/?]+).*", "$1");
  }

  public static String generateLinkId() {
    Random random = new SecureRandom();

    if (LENGHT_LINK_ID <= 0) {
      throw new IllegalArgumentException("String length must be a positive integer");
    }

    StringBuilder sb = new StringBuilder(LENGHT_LINK_ID);
    for (int i = 0; i < LENGHT_LINK_ID; i++) {
      sb.append(characters.charAt(random.nextInt(characters.length())));
    }

    return sb.toString();
  }

  public static String getUrlFromLinkId(Context context, String linkId) {
    return "https://" + context.getString(R.string.web_host) + "/" + linkId;
  }

  public static String getLinkIdFromUrl(String url) {
    Uri uri = Uri.parse(url);
    String path = uri.getPath();
    String linkId = path.substring(path.lastIndexOf('/') + 1);
    return linkId;
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

  public static String constrainUsersStr(List<User> users, int availableWidth,
      boolean isDisplayName) {
    StringBuffer buffer = new StringBuffer();
    int count = 0;
    for (int i = 0; i < users.size() && buffer.length() <= availableWidth; i++) {
      User user = users.get(i);
      String label = isDisplayName ? user.getDisplayName() : user.getUsername();

      if (buffer.length() + label.length() <= availableWidth) {
        buffer.append(label);
        count++;
        if (i < users.size() - 1 && buffer.length() <= availableWidth) buffer.append(", ");
      } else if (buffer.length() > 0) {
        buffer.replace(buffer.length() - 2, buffer.length() - 1, "");
        break;
      }
    }

    if (buffer.length() >= availableWidth) {
      String str = buffer.subSequence(0, availableWidth).toString();
      buffer = new StringBuffer();
      buffer.append(str);
      buffer.append("... +" + (users.size() - count));
    } else if (count < users.size()) {
      buffer.append("... +" + (users.size() - count));
    }

    return buffer.toString();
  }

  public static int stringWithPrefix(Context context, String prefix, String name) {
    return context.getResources().getIdentifier(prefix + name, "string", context.getPackageName());
  }

  public static String listToJson(List<String> list) {
    String json = "\"";
    for (int i = 0; i < list.size(); i++) {
      if (i == list.size() - 1) {
        json += list.get(i) + "\"";
      } else {
        json += list.get(i) + "\", \"";
      }
    }
    if (list.size() == 0) json += "\"";
    return json;
  }
}
