package org.docheinstein.minimotek.ui.hotkeys

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.hotkey.Hotkey


class HotkeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    hotkey: Hotkey? = null
) : AppCompatTextView(context, attrs, defStyle) {

    init {
        background = ResourcesCompat.getDrawable(context.resources,
            R.drawable.hotkey_button_selector, null)
        textSize = 28f
        setPadding(18, 10, 18, 10)
        if (hotkey != null)
            text = hotkey.displayName()
    }
}