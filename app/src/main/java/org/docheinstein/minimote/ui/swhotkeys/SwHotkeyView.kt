package org.docheinstein.minimote.ui.swhotkeys

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import org.docheinstein.minimote.R
import org.docheinstein.minimote.database.hotkey.sw.*
import org.docheinstein.minimote.keys.MinimoteKeyType
import org.docheinstein.minimote.util.verbose

/**
 * View similar to a button representing a software hotkey.
 * Exposes the attribute [hotkey] which can be changed to update
 * the aspect of this view automatically.
 */

class SwHotkeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    hotkey: Hotkey? = null
) : AppCompatTextView(context, attrs, defStyle) {

    // UI hotkey class
    // contains only the UI relevant information of SwHotkey
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

        val displayName: String
            get() {
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

    var hotkey: Hotkey? = null
        set(value) {
            verbose("SwHotkeyView.hotkey.set($value)")
            field = value
            updateUI()
        }

    init {
        verbose("SwHotkeyView.init()")
        background = ResourcesCompat.getDrawable(context.resources,
            R.drawable.hotkey_button_selector, null)
        setTextColor(context.resources.getColor(R.color.dark_gray, null))
        this.hotkey = hotkey
    }

    private fun updateUI() {
        hotkey?.let {
            textSize = it.textSize.toFloat()
            setPadding(it.horizontalPadding, it.verticalPadding, it.horizontalPadding, it.verticalPadding)
            text = it.displayName
        }
    }
}