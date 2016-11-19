package com.tribe.app.presentation.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiParser {

    private static final Map<String, String> IEmojiMap;
    static
    {
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
    }

    /**
     * Return the text with emoticons changed to android code
     * @param text
     * @return
     */
    public static String demojizedText(String text){
    	String returnTextString = text;
    	//Pattern to match    	
    	Pattern pattern = Pattern.compile("(\\:[^\\:]+\\:)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
        	String found = matcher.group();
        	if (IEmojiMap.get(found) == null) continue;
        	returnTextString = returnTextString.replace(found, IEmojiMap.get(found));
        }
    	//Returning text
    	return returnTextString;
    }
}