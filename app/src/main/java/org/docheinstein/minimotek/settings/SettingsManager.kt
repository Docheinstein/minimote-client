package org.docheinstein.minimotek.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.BoolRes
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import org.docheinstein.minimotek.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun getAutomaticallyOpenKeyboard() = getBoolean(context,
        R.string.pref_automatically_show_keyboard,
        R.bool.pref_automatically_show_keyboard_default
    )

    fun getAutomaticallyShowTouchpadButtons() = getBoolean(context,
        R.string.pref_automatically_show_touchpad_buttons,
        R.bool.pref_automatically_show_touchpad_buttons_default
    )

    fun getAutomaticallyShowHotkeys() = getBoolean(context,
        R.string.pref_automatically_show_hotkeys,
        R.bool.pref_automatically_show_hotkeys_default
    )

    private fun getBoolean(ctx: Context, @StringRes prefKey: Int, @BoolRes defValueKey: Int): Boolean {
        return getDefaultSharedPreferences(ctx).getBoolean(
            ctx.resources.getString(prefKey),
            ctx.resources.getBoolean(defValueKey)
        )
    }

    private fun getDefaultSharedPreferences(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(getDefaultSharedPreferencesName(ctx), Context.MODE_PRIVATE)
    }

    // Taken from androix.preference
    private fun getDefaultSharedPreferencesName(ctx: Context): String {
        return ctx.packageName + "_preferences"
    }
}