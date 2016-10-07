package com.tribe.app.presentation.view.utils;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.animation.LinearInterpolator;

import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by tiago on 23/08/2016.
 */
public class ScoreUtils {

    public enum Level implements Serializable {
        ROOKIE          (R.string.level_rookie_title, 0, R.drawable.picto_cool_kid),
        HEATING_UP      (R.string.level_heating_up_title, 2000, R.drawable.picto_heating_up),
        RISING_STAR     (R.string.level_rising_star_title, 10000, R.drawable.picto_rising),
        ON_FIRE         (R.string.level_on_fire_title, 25000, R.drawable.picto_fire),
        ROCKSTAR        (R.string.level_rockstar_title, 50000, R.drawable.picto_rockstar),
        CHAMP           (R.string.level_champ_title, 100000, R.drawable.picto_champ),
        ICON            (R.string.level_icon_title, 200000, R.drawable.picto_icon),
        GREATNESS       (R.string.level_greatness_title, 400000, R.drawable.picto_greatness),
        ROYALTY         (R.string.level_royalty_title, 700000, R.drawable.picto_royalty),
        UNICORN         (R.string.level_unicorn_title, 1000000, R.drawable.picto_unicorn);

        private final int stringId;
        private final int points;
        private final int drawableId;

        Level(int stringId, int points, int drawableId) {
            this.stringId = stringId;
            this.points = points;
            this.drawableId = drawableId;
        }

        public int getPoints() {
            return points;
        }

        public int getDrawableId() {
            return drawableId;
        }

        public int getStringId() {
            return stringId;
        }
    }

    public enum Point {
        SEND_RECEIVE_CHAT       ("TEXT", R.string.points_SendReceiveChat_title, R.string.points_SendReceiveChat_description, 1, R.drawable.picto_text),
        SEND_RECEIVE_TRIBE      ("TRIBE", R.string.points_SendReceiveTribe_title, R.string.points_SendReceiveTribe_description, 5, R.drawable.picto_camera),
        NEW_FRIENDSHIP          ("NEW_FRIENDSHIP", R.string.points_NewFriendship_title, R.string.points_NewFriendship_description, 15, R.drawable.picto_points_friend),
        INVITE                  ("INVITE_FRIEND", R.string.points_InviteTribe_title, R.string.points_InviteTribe_description, 30, R.drawable.picto_points_invite),
        CREATE_GROUP            ("CREATE_GROUP", R.string.points_CreateGroup_title, R.string.points_CreateGroup_description, 50, R.drawable.picto_group),
        CAMERA                  ("ENABLE_CAMERA", R.string.points_Camera_title, R.string.points_Camera_description, 100, R.drawable.picto_photo_white),
        RATE_APP                ("RATE_APP", R.string.points_RateApp_title, R.string.points_RateApp_description, 200, R.drawable.picto_points_rate),
        SHARE_PROFILE           ("SHARE_PROFILE", R.string.points_ShareProfile_title, R.string.points_ShareProfile_description, 300, R.drawable.picto_share),
        LOCATION                ("ENABLE_LOCATION", R.string.points_LocationPermission_title, R.string.points_LocationPermission_description, 350, R.drawable.picto_location),
        SYNCHRONIZE_FRIENDS     ("SYNC_FACEBOOK", R.string.points_ConnectFacebook_title, R.string.points_ConnectFacebook_description, 350, R.drawable.picto_points_facebook),
        INVITE_FACEBOOK         ("FB_INVITE_ALL", R.string.points_InviteFacebookFriends_title, R.string.points_InviteFacebookFriends_description, 1500, R.drawable.picto_points_facebook);

        private final String serverKey;
        private final int stringLabelId;
        private final int stringSubLabelId;
        private final int points;
        private final int drawableId;

        Point(String serverKey, int stringLabelId, int stringSubLabelId, int points, int drawableId) {
            this.serverKey = serverKey;
            this.stringLabelId = stringLabelId;
            this.stringSubLabelId = stringSubLabelId;
            this.points = points;
            this.drawableId = drawableId;
        }

        public String getServerKey() {
            return serverKey;
        }

        public int getPoints() {
            return points;
        }

        public int getDrawableId() {
            return drawableId;
        }

        public int getStringLabelId() {
            return stringLabelId;
        }

        public int getStringSubLabelId() {
            return stringSubLabelId;
        }
    }

    public static Level getLevelForScore(int score) {
        Level previousLevel = Level.values()[0];

        for (int i = 1; i < Level.values().length - 1; i++) {
            Level level = Level.values()[i];

            if (score < level.points)  return previousLevel;

            previousLevel = level;
        }

        return Level.values()[Level.values().length - 1];
    }

    public static Level getNextLevelForScore(int score) {
        for (Level level : Level.values()) {
            if (score < level.points) return level;
        }

        return null;
    }

    public static int getRestForNextLevel(int score) {
        Level level = getNextLevelForScore(score);
        return level.getPoints() - score;
    }

    // http://stackoverflow.com/a/4753866
    private static char[] c = new char[]{'K', 'M', 'B', 'T'};

    /**
     * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
     * @param n the number to format
     * @param iteration in fact this is the class from the array c
     * @return a String representing the number n formatted in a cool looking way.
     */
    public static String format(double n, int iteration) {
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) %10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || (!isRound && d > 9.99)? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration])
                : format(d, iteration+1));
    }

    public static void setScore(final Context context, final TextViewFont txtView, int from, int to, final int stringRes) {
        ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(from, to);
        animator.addUpdateListener(animation -> {
            String output = formatFloatingPoint(context, animation.getAnimatedValue());

            if (stringRes != -1)
                txtView.setText("" + context.getString(stringRes, output));
            else
                txtView.setText("" + output);
        });

        animator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });

        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(500 * (int) Math.ceil((float) Math.abs(to - from) / 10));
        animator.start();

        txtView.setTag(R.id.old_score, to);
    }

    public static String formatFloatingPoint(Context context, Object object) {
        NumberFormat nf = NumberFormat.getNumberInstance(context.getResources().getConfiguration().locale);
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("###,###");
        return df.format(object);
    }
}
