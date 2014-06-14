package net.leolink.android.twitter4a.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

/**
 * Created by leolink on 6/14/14.
 */
public class Util {
    public static final String TAG = "Twitter4A";

    /* Logging utilities */
    public static void log(Object obj) {
        Log.e(TAG, String.valueOf(obj));
    }
    public static void logd(Object obj) {
        Log.e(TAG, String.valueOf(obj));
    }

    /* SharedPreferences utilities */
    public static void spPutString(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Const.SP_TWITTER4A_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }
    public static String spGetString(Context context, String key, String defaultValue) {
        return context.getSharedPreferences(Const.SP_TWITTER4A_FILE_NAME, Context.MODE_PRIVATE)
                .getString(key, defaultValue);
    }
    public static void spRemove(Context context, String key) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                Const.SP_TWITTER4A_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(key);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }
}
