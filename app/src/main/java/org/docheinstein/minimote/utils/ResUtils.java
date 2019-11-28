package org.docheinstein.minimote.utils;

import android.content.Context;

import androidx.annotation.StringRes;

public class ResUtils {
    public static String getString(@StringRes int key, Context ctx) {
        if (ctx == null)
            return null;
        return ctx.getString(key);
    }
}
