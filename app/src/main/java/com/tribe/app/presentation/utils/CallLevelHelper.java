package com.tribe.app.presentation.utils;

import android.content.Context;

import com.tribe.app.R;

import java.util.Date;

public class CallLevelHelper {

    // TODO : Check all this stuff, between minutes and seconds. There may be a mic-mac somewhere.

    private static final int LEVEL_1 =   2 * 60;
    private static final int LEVEL_2 =   5 * 60;
    private static final int LEVEL_3 =  10 * 60;
    private static final int LEVEL_4 =  30 * 60;
    private static final int LEVEL_5 =  60 * 60;
    private static final int LEVEL_6 = 120 * 60;

    private static int getTimeElapsed(Date startedAt) {
        return (int)((new Date().getTime() - startedAt.getTime()) / 1000);
    }

    public static String getCurrentLevel(Context context, Date startedAt) {

        int timeElapsed = getTimeElapsed(startedAt);
        int resource = 0;

        if (timeElapsed >= LEVEL_6) { resource = R.string.call_level_6; }
        else if (timeElapsed >= LEVEL_5) { resource = R.string.call_level_5; }
        else if (timeElapsed >= LEVEL_4) { resource = R.string.call_level_4; }
        else if (timeElapsed >= LEVEL_3) { resource = R.string.call_level_3; }
        else if (timeElapsed >= LEVEL_2) { resource = R.string.call_level_2; }
        else if (timeElapsed >= LEVEL_1) { resource = R.string.call_level_1; }
        else { resource = R.string.call_level_0; }

        return EmojiParser.demojizedText(context.getString(resource));
    }

    public static String getFormattedDuration(Date startedAt) {

        int timeElapsed = getTimeElapsed(startedAt);

        int minutes = timeElapsed / 60;
        int seconds = timeElapsed % 60;

        return (minutes > 9 ? minutes : "0" + minutes) + ":" + (seconds > 9 ? seconds : "0" + seconds);
    }
}
