package com.tribe.tribelivesdk.util;

import android.util.Log;

/**
 * Created by tiago on 13/01/2017.
 */

public class LogUtil {

    public static void e(Class clazz, String log) {
        Log.e(clazz.getName(), log);
    }

    public static void d(Class clazz, String log) {
        Log.d(clazz.getName(), log);
    }

    public static void v(Class clazz, String log) {
        Log.v(clazz.getName(), log);
    }

    public static void i(Class clazz, String log) {
        Log.i(clazz.getName(), log);
    }
}
