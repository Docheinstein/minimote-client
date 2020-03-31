package org.docheinstein.minimote.utils;

import android.view.View;

public class ViewUtils {
    private static final int DEFAULT_INVISIBLE_STATE = View.GONE;

    public static void toggleVisibility(View view) {
        toggleVisibility(view, DEFAULT_INVISIBLE_STATE);
    }

    public static void toggleVisibility(View view, int invisibleState) {
        if (view == null)
            return;
        if (view.getVisibility() == View.VISIBLE) {
            hide(view, invisibleState);
        } else {
            show(view);
        }
    }

    public static void show(View view) {
        setVisibility(view, true);
    }

    public static void hide(View view) {
        setVisibility(view, false);
    }

    public static void hide(View view, int invisibleState) {
        setVisibility(view, false, invisibleState);
    }

    public static void setVisibility(View view, boolean show) {
        setVisibility(view, show, DEFAULT_INVISIBLE_STATE);
    }

    public static void setVisibility(View view, boolean show, int invisibleState) {
        if (view == null)
            return;
        view.setVisibility(show ? View.VISIBLE : invisibleState);
    }

    public static boolean isShown(View view) {
        return view.getVisibility() == View.VISIBLE;
    }
}
