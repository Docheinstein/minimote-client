package org.docheinstein.minimote.utils;

import android.view.View;

public class ViewUtils {
    public static void show(View view, boolean show) {
        if (view == null)
            return;
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public static boolean isShown(View view) {
        return view.getVisibility() == View.VISIBLE;
    }
}
