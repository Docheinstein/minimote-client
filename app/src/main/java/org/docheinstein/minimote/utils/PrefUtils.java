package org.docheinstein.minimote.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

public class PrefUtils {

    public static boolean getBoolean(Context ctx, @StringRes int prefKey, @BoolRes int defValueKey) {
        SharedPreferences prefs = getPreferences(ctx);
        Resources res = ctx.getResources();
        if (prefs == null)
            return res.getBoolean(defValueKey);
        return prefs.getBoolean(res.getString(prefKey), res.getBoolean(defValueKey));
    }

    public static String getString(Context ctx, @StringRes int prefKey, @StringRes int defValueKey) {
        SharedPreferences prefs = getPreferences(ctx);
        Resources res = ctx.getResources();
        if (prefs == null)
            return null;
        return prefs.getString(res.getString(prefKey), null);
    }

    public static @ColorInt int getColor(Context ctx, @StringRes int prefKey, @ColorRes int defValueKey) {
        SharedPreferences prefs = getPreferences(ctx);
        Resources res = ctx.getResources();
        if (prefs == null)
            return res.getColor(defValueKey, null);
        return prefs.getInt(res.getString(prefKey), res.getColor(defValueKey, null));
    }

    public static SharedPreferences getPreferences(Context ctx) {
        if (ctx == null)
            return null;
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}
