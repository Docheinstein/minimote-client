package org.docheinstein.minimote.utils;

import android.view.View;

public class ViewUtils {
    public static void show(View view, boolean show) {
        show(view, show, View.GONE);
    }

    public static void show(View view, boolean show, int invisibleState) {
        if (view == null)
            return;
        view.setVisibility(show ? View.VISIBLE : invisibleState);
    }

    public static boolean isShown(View view) {
        return view.getVisibility() == View.VISIBLE;
    }
}
