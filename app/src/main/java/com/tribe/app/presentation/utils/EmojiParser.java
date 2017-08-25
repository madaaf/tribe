package com.tribe.app.presentation.utils;

import android.os.Build;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiParser {

  private static final Map<String, String> IEmojiMap;

  static {
    IEmojiMap = new HashMap<>();
    IEmojiMap.put(":airplane:", "\u2708");
    IEmojiMap.put(":briefcase:", "\uD83D\uDCBC");
    IEmojiMap.put(":family:", "\uD83D\uDC6A");
    IEmojiMap.put(":fire:", "\uD83D\uDD25");
    IEmojiMap.put(":mortar_board:", "\uD83C\uDF93");
    IEmojiMap.put(":nail_care:", "\uD83D\uDC85");
    IEmojiMap.put(":pizza:", "\uD83C\uDF55");
    IEmojiMap.put(":punch:", "\uD83D\uDC4A");
    IEmojiMap.put(":trophy:", "\uD83C\uDFC6");
    IEmojiMap.put(":pensive:", "\uD83D\uDE14");
    IEmojiMap.put(":dizzy:", "\uD83D\uDCAB");
    IEmojiMap.put(":house_with_garden:", "\uD83C\uDFE1");
    IEmojiMap.put(":loudspeaker:", "\uD83D\uDCE2");
    IEmojiMap.put(":kimono:", "\uD83D\uDC58");
    IEmojiMap.put(":baseball:", "\u26BE");
    IEmojiMap.put(":football:", "\uD83C\uDFC8");
    IEmojiMap.put(":jack_o_lantern:", "\uD83C\uDF83");
    IEmojiMap.put(":muscle:", "\uD83D\uDCAA");
    IEmojiMap.put(":skull:", "\u2620");
    IEmojiMap.put(":key:", "\uD83D\uDD11");
    IEmojiMap.put(":bird:", "\uD83D\uDC26");
    IEmojiMap.put(":satellite:", "\uD83D\uDEF0");
    IEmojiMap.put(":clap:", "\uD83D\uDC4F");
    IEmojiMap.put(":lock:", "\uD83D\uDD12");
    IEmojiMap.put(":joy:", "\uD83D\uDE02");
    IEmojiMap.put(":paperclip:", "\uD83D\uDCCE");
    IEmojiMap.put(":closed_lock_with_key:", "\uD83D\uDD10");
    IEmojiMap.put(":tada:", "\uD83C\uDF89");
    IEmojiMap.put(":mag:", "\uD83D\uDD0D");
    IEmojiMap.put(":link:", "\uD83D\uDD17");
    IEmojiMap.put(":arrow_left:", "\u2B05");
    IEmojiMap.put(":wave:", "\uD83D\uDC4B");
    IEmojiMap.put(":heart:", "\u2764");
    IEmojiMap.put(":broken_heart:", "\uD83D\uDC94");
    IEmojiMap.put(":pencil2:", "\u270E");
    IEmojiMap.put(":ghost:", "\uD83D\uDC7B");
    IEmojiMap.put(":star2:", "\uD83C\uDF1F");
    IEmojiMap.put(":metal:",
        android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? "\uD83E\uDD18"
            : "\uD83D\uDC4F");
    IEmojiMap.put(":skull:", "\uD83D\uDC80");
    IEmojiMap.put(":purple_heart:", "\uD83D\uDC9C");
    IEmojiMap.put(":bell:", "\uD83D\uDD14");
    IEmojiMap.put(":no_bell:", "\uD83D\uDD15");
    IEmojiMap.put(":hearts:", "\uD83D\uDC95");
    IEmojiMap.put(":two_hearts:", "\uD83D\uDC95");
    IEmojiMap.put(":love_letter:", "\uD83D\uDC8C");
    IEmojiMap.put(":x:", "\u274C");
    IEmojiMap.put(":honeybee:", "\uD83D\uDC1D");
    IEmojiMap.put(":loud_sound:", "\uD83D\uDD0A");
    IEmojiMap.put(":smile_cat:", "\uD83E\uDD18");
    IEmojiMap.put(":joy_cat:", "\uD83D\uDC4A");
    IEmojiMap.put(":heart_eyes_cat:", "\uD83E\uDD19");
    IEmojiMap.put(":scream_cat:", "\u270C");

    IEmojiMap.put(":hand1:", "\uD83E\uDD18");
    IEmojiMap.put(":punch:", "\uD83D\uDC4A");
    IEmojiMap.put(":hand2:", "\uD83E\uDD19");
    IEmojiMap.put(":v:", "\u270C");

    IEmojiMap.put(":smile:", "\uD83D\uDE04");
    IEmojiMap.put(":kissing_smiling_eyes:", "\uD83D\uDE19");
    IEmojiMap.put(":joy:", "\uD83D\uDE02");
    IEmojiMap.put(":grin:", "\uD83D\uDE01");

    IEmojiMap.put(":panda_face:", "\uD83D\uDC3C");
    IEmojiMap.put(":tiger:", "\uD83D\uDC2F");
    IEmojiMap.put(":dog:", "\uD83D\uDC36");
    IEmojiMap.put(":monkey_face:", "\uD83D\uDC35");

    IEmojiMap.put(":lemon:", "\uD83C\uDF4B");
    IEmojiMap.put(":watermelon:", "\uD83C\uDF49");
    IEmojiMap.put(":peach:", "\uD83C\uDF51");
    IEmojiMap.put(":grapes:", "\uD83C\uDF47");

    IEmojiMap.put(":popcorn:", "\uD83C\uDF7F");
    IEmojiMap.put(":doughnut:", "\uD83C\uDF69");
    IEmojiMap.put(":cookie:", "\uD83C\uDF6A");
    IEmojiMap.put(":lollipop:", "\uD83C\uDF6D");
    IEmojiMap.put(":see_no_evil:", "\uD83D\uDE48");

    IEmojiMap.put(":clock10:", "\uD83D\uDD59");
    IEmojiMap.put(":electric_plug:", "\uD83D\uDD0C");
    IEmojiMap.put(":loud_sound:", "\uD83D\uDD0A");
    IEmojiMap.put(":vhs:", "\uD83D\uDCFC");
    IEmojiMap.put(":telephone_receiver:", "\uD83D\uDCDE");
    IEmojiMap.put(":camera:", "\uD83D\uDCF7");
    IEmojiMap.put(":star:", "\u2B50");
    IEmojiMap.put(":clock3:", "\uD83D\uDD52");
    IEmojiMap.put(":warning:", "\u26A0");
    IEmojiMap.put(":no_mobile_phones:", "\uD83D\uDCF5");
    IEmojiMap.put(":busts_in_silhouette:", "\uD83D\uDC65");
    IEmojiMap.put(":video_game:", "\uD83C\uDFAE");
    IEmojiMap.put(":telephone:", "\u260E");
    IEmojiMap.put(":bust_in_silhouette:", "\uD83D\uDC64");
    IEmojiMap.put(":hourglass:", "\u23F3");
    IEmojiMap.put(":spy:", "\uD83D\uDD75");
    IEmojiMap.put(":boom:", "\uD83D\uDCA5");
    IEmojiMap.put(":poop:", "\uD83D\uDCA9");
    IEmojiMap.put(":crown:", "\uD83D\uDC51");
  }

  /**
   * Return the text with emoticons changed to android code
   */
  public static String demojizedText(String text) {
    String returnTextString = text;
    // Pattern to match
    Pattern pattern = Pattern.compile("(\\:[^\\:]+\\:)");
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String found = matcher.group();
      if (IEmojiMap.get(found) == null) continue;
      returnTextString = returnTextString.replace(found, IEmojiMap.get(found));
    }
    // Returning text
    return returnTextString;
  }
}