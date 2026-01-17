package com.shenma.tvlauncher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;


/**
 * @author joychang
 * @Description 全局共享
 */
public class SharePreferenceDataUtil {
    private static SharedPreferences.Editor editor;
    private static SharedPreferences sp;

    private static void init(Context context) {
        if (sp != null) {
            return;
        }
        // if (TextUtils.isEmpty("pd_ac")) {
        if (TextUtils.isEmpty("initData")) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sp = defaultSharedPreferences;
            editor = defaultSharedPreferences.edit();
            return;
        }
        //SharedPreferences sharedPreferences = context.getSharedPreferences("pd_ac", 0);
        SharedPreferences sharedPreferences = context.getSharedPreferences("initData", 0);
        sp = sharedPreferences;
        editor = sharedPreferences.edit();
    }

    public static void setSharedIntData(Context context, String key, int value) {
        if (sp == null) {
            init(context);
        }
        editor.putInt(key, value).commit();
    }

    public static int getSharedIntData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return sp.getInt(key, 0);
    }

    public static int getSharedIntData(Context context, String key, int val) {
        if (sp == null) {
            init(context);
        }
        return sp.getInt(key, val);
    }

    public static void setSharedlongData(Context context, String key, long value) {
        if (sp == null) {
            init(context);
        }
        editor.putLong(key, value).commit();
    }

    public static long getSharedlongData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return sp.getLong(key, 0L);
    }

    public static long getSharedlongData(Context context, String key, long time) {
        if (sp == null) {
            init(context);
        }
        return sp.getLong(key, time);
    }

    public static void setSharedFloatData(Context context, String key, float value) {
        if (sp == null) {
            init(context);
        }
        editor.putFloat(key, value).commit();
    }

    public static Float getSharedFloatData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return Float.valueOf(sp.getFloat(key, 0.0f));
    }

    public static Float getSharedFloatData(Context context, String key, float val) {
        if (sp == null) {
            init(context);
        }
        return Float.valueOf(sp.getFloat(key, val));
    }

    public static void setSharedBooleanData(Context context, String key, boolean value) {
        if (sp == null) {
            init(context);
        }
        editor.putBoolean(key, value).commit();
    }

    public static Boolean getSharedBooleanData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return Boolean.valueOf(sp.getBoolean(key, false));
    }

    public static Boolean getSharedBooleanData(Context context, String key, boolean val) {
        if (sp == null) {
            init(context);
        }
        return Boolean.valueOf(sp.getBoolean(key, val));
    }

    public static void setSharedStringData(Context context, String key, String value) {
        if (sp == null) {
            init(context);
        }
        editor.putString(key, value).commit();
    }

    public static String getSharedStringData(Context context, String key, String val) {
        if (sp == null) {
            init(context);
        }
        return sp.getString(key, val);
    }

    public static String getSharedStringData(Context context, String key) {
        if (sp == null) {
            init(context);
        }
        return sp.getString(key, "");
    }
}
