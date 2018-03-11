package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import bases.BaseApp;

/**
 * Created by HP on 2018-01-03.
 */

public class PreferenceUtil {

    private PreferenceUtil() {}

    public static String getString(String key) {
        String value = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            value = preferences.getString(key, null);
        }
        return value;
    }

    public static boolean setString(String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null && key != null && (!key.equals(""))) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            return editor.commit();
        }
        return false;
    }

    public static float getFloat(String key, float defaultValue) {
        float value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            value = preferences.getFloat(key, defaultValue);
        }
        return value;
    }

    public static boolean setFloat(String key, float value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(key, value);
            return editor.commit();
        }
        return false;
    }

    public static long getLong(String key, long defaultValue) {
        long value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            value = preferences.getLong(key, defaultValue);
        }
        return value;
    }

    public static boolean setLong(String key, long value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(key, value);
            return editor.commit();
        }
        return false;
    }

    public static int getInt(String key, int defaultValue) {
        int value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            value = preferences.getInt(key, defaultValue);
        }
        return value;
    }

    public static boolean setInt(String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            return editor.commit();
        }
        return false;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        boolean value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            value = preferences.getBoolean(key, defaultValue);
        }
        return value;
    }

    public static boolean setBoolean(String key, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApp.getContext());
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            return editor.commit();
        }
        return false;
    }
}
