package com.tribe.app.presentation.view.utils;

import com.tribe.app.R;

/**
 * Created by tiago on 23/08/2016.
 */
public class ScoreUtils {

    public enum Level {
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
        APP_OPENED              (R.string.points_AppOpened_title, R.string.points_AppOpened_description, 1, R.drawable.picto_points_time),
        SEND_RECEIVE_CHAT       (R.string.points_SendReceiveChat_title, R.string.points_SendReceiveChat_description, 2, R.drawable.picto_text),
        SEND_RECEIVE_TRIBE      (R.string.points_SendReceiveTribe_title, R.string.points_SendReceiveTribe_description, 10, R.drawable.picto_camera),
        NEW_FRIENDSHIP          (R.string.points_NewFriendship_title, R.string.points_NewFriendship_description, 30, R.drawable.picto_points_friend),
        INVITE                  (R.string.points_InviteTribe_title, R.string.points_InviteTribe_description, 50, R.drawable.picto_points_invite),
        CREATE_GROUP            (R.string.points_CreateGroup_title, R.string.points_CreateGroup_description, 100, R.drawable.picto_group),
        RATE_APP                (R.string.points_RateApp_title, R.string.points_RateApp_description, 500, R.drawable.picto_points_rate),
        SHARE_PROFILE           (R.string.points_ShareProfile_title, R.string.points_ShareProfile_description, 1000, R.drawable.picto_share),
        INVITE_FACEBOOK         (R.string.points_InviteFacebookFriends_title, R.string.points_InviteFacebookFriends_description, 1500, R.drawable.picto_points_facebook),
        GROUP_100_MEMBERS       (R.string.points_HundredGroupMembers_title, R.string.points_HundredGroupMembers_description, 2000, R.drawable.picto_points_public_group);

        private final int stringLabelId;
        private final int stringSubLabelId;
        private final int points;
        private final int drawableId;

        Point(int stringLabelId, int stringSubLabelId, int points, int drawableId) {
            this.stringLabelId = stringLabelId;
            this.stringSubLabelId = stringSubLabelId;
            this.points = points;
            this.drawableId = drawableId;
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
        Level previousLevel = null;

        for (Level level : Level.values()) {
            if (score < level.points)  return previousLevel;
            else if (score == level.points) return level;

            previousLevel = level;
        }

        return null;
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
}
