package com.neo_lab.demotwilio.utils.share_preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sam_nguyen on 11/04/2017.
 */

public class SharedPreferencesUtils {

    private static final String EMPTY = "";

    public static void saveStringData(Context context, String key, String data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public static void saveBooleanData(Context context, String key, boolean data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, data);
        editor.commit();
    }

    public static void saveIntegerData(Context context, String key, int data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, data);
        editor.commit();
    }

    public static String getStringData(Context context, String key) {
        SharedPreferences sharePreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        return sharePreferences.getString(key, EMPTY);
    }

    public static boolean getBooleanData(Context context, String key) {
        SharedPreferences sharePreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        return sharePreferences.getBoolean(key, false);
    }

    public static int getIntegerData(Context context, String key) {
        SharedPreferences sharePreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        return sharePreferences.getInt(key, 0);
    }
}
