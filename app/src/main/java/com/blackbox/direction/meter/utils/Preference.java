package com.blackbox.direction.meter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;
import java.util.Set;

/**
 * SharedPreferences Utilities
 * 
 */
public final class Preference {

    /**
     * The name of SharedPreferences
     */
    private static final String PREFERENCE_NAME = "sp_cwal";

    /**
     * Get the instance of SharedPreferences
     *
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save Single String preference
     *
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean save(Context context, String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * Get string value through key
     *
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(key, "");
    }

    /**
     * Save map String preference
     *
     * @param context
     * @param valuesMap
     * @return
     */
    public static boolean save(Context context, HashMap<String, String> valuesMap) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        String value = "";
        for (String key : valuesMap.keySet()) {
            value = valuesMap.get(key);
            editor.putString(key, value);
        }
        return editor.commit();
    }

    /**
     * Save single boolean preference
     *
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean save(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * Get boolean value through key
     *
     * @param context
     * @param key
     * @param defFlag
     * @return
     */
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Clean the SharedPreferences
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * remove the value by key
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * Save int value
     *
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean save(Context context, String key, int value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * Get int value
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Save float value
     *
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean save(Context context, String key, float value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    /**
     * Get float value
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static float getFloat(Context context, String key, float defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getFloat(key, defaultValue);
    }

    public static boolean save(Context context, String key, Set<String> value) {
        if(value == null) return false;
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, value);
        return editor.commit();
    }

    public static Set<String> getStringSet(Context context, String key, Set<String> defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getStringSet(key, defaultValue);
    }

}
