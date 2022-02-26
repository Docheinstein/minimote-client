package org.docheinstein.minimotek.ui.swhotkeys

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.hotkey.sw.*
import org.docheinstein.minimotek.keys.MinimoteKeyType


class SwHotkeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    var hotkey: Hotkey? = null
) : AppCompatTextView(context, attrs, defStyle) {

    data class Hotkey(
        var key: MinimoteKeyType,
        var shift: Boolean,
        var ctrl: Boolean,
        var alt: Boolean,
        var altgr: Boolean,
        var meta: Boolean,
        var label: String?,
        var textSize: Int,
        var horizontalPadding: Int,
        var verticalPadding: Int,
    ) {
        companion object {
            fun fromSwHotkey(swHotkey: SwHotkey): Hotkey {
                return Hotkey(
                    key = swHotkey.key,
                    shift = swHotkey.shift,
                    ctrl = swHotkey.ctrl,
                    alt = swHotkey.alt,
                    altgr = swHotkey.altgr,
                    meta = swHotkey.meta,
                    label = swHotkey.label,
                    textSize = swHotkey.textSize,
                    horizontalPadding = swHotkey.horizontalPadding,
                    verticalPadding = swHotkey.verticalPadding,
                )
            }
        }
        fun displayName(): String {
            if (label != null)
                return label!!

            val tokens = mutableListOf<String>()
            if (ctrl)
                tokens.add("CTRL")
            if (alt)
                tokens.add("ALT")
            if (altgr)
                tokens.add("ALT GR")
            if (meta)
                tokens.add("META")
            if (shift)
                tokens.add("SHIFT")
            tokens.add(key.keyString)

            return tokens.joinToString(separator = "+")
        }
    }

    init {
        background = ResourcesCompat.getDrawable(context.resources,
            R.drawable.hotkey_button_selector, null)
        updateUI()
    }

    fun set(hotkey: SwHotkey) = set(Hotkey.fromSwHotkey(hotkey))

    fun set(hotkey: Hotkey) {
        this.hotkey = hotkey
        updateUI()
    }

    private fun updateUI() {
        hotkey?.let {
            textSize = it.textSize.toFloat()
            setPadding(it.horizontalPadding, it.verticalPadding, it.horizontalPadding, it.verticalPadding)
            text = it.displayName()
        }
    }
}