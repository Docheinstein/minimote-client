package org.docheinstein.minimote.settings;

import android.content.Context;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.utils.PrefUtils;

public class SettingsManager {
    public static boolean automaticallyOpenKeyboard(Context ctx) {
        return PrefUtils.getBoolean(ctx,
                R.string.pref_automatically_open_keyboard,
                R.bool.pref_automatically_open_keyboard_default
        );
    }

    public static boolean automaticallyOpenHotkeys(Context ctx) {
        return PrefUtils.getBoolean(ctx,
                R.string.pref_automatically_open_hotkeys,
                R.bool.pref_automatically_open_hotkeys_default
        );
    }

    public static boolean showTouchpadButtons(Context ctx) {
        return PrefUtils.getBoolean(ctx,
                R.string.pref_show_touchpad_buttons,
                R.bool.pref_show_touchpad_buttons_default
        );
    }

    public static int touchpadColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_touchpad_color,
                R.color.pref_touchpad_color_default
        );
    }

    public static int touchpadButtonsContainerColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_touchpad_buttons_container_color,
                R.color.pref_touchpad_buttons_container_color_default
        );
    }

    public static int touchpadButtonUnpressedColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_touchpad_button_unpressed_color,
                R.color.pref_touchpad_button_unpressed_color_default
        );
    }

    public static int touchpadButtonPressedColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_touchpad_button_pressed_color,
                R.color.pref_touchpad_button_pressed_color_default
        );
    }


    public static int touchpadButtonBorderColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_touchpad_button_border_color,
                R.color.pref_touchpad_button_border_color_default
        );
    }

    public static int hotkeysOverlayColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_hotkeys_overlay_color,
                R.color.pref_hotkeys_overlay_color_default
        );
    }

    public static int hotkeysOverlayBorderColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_hotkeys_overlay_border_color,
                R.color.pref_hotkeys_overlay_border_color_default
        );
    }

    public static int hotkeyUnpressedColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_hotkey_unpressed_color,
                R.color.pref_hotkey_unpressed_color_default
        );
    }

    public static int hotkeyPressedColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_hotkey_pressed_color,
                R.color.pref_hotkey_pressed_color_default
        );
    }

    public static int hotkeyBorderColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_hotkey_border_color,
                R.color.pref_hotkey_border_color_default
        );
    }

    public static int hotkeyTextColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_hotkey_text_color,
                R.color.pref_hotkey_text_color_default
        );
    }

    public static int buttonOpenHotkeysColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_button_hotkeys_color,
                R.color.pref_button_hotkeys_color_default
        );
    }

    public static int buttonOpenKeyboardColor(Context ctx) {
        return PrefUtils.getColor(ctx,
                R.string.pref_button_keyboard_color,
                R.color.pref_button_keyboard_color_default
        );
    }
}
