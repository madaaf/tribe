package com.tribe.app.presentation.utils;

import android.util.Patterns;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

        Pattern unicodeOutliers = Pattern.compile(
                "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE |
                        Pattern.CASE_INSENSITIVE
        );

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
}
