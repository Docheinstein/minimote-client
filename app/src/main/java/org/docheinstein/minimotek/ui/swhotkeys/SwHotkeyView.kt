package org.docheinstein.minimotek.ui.swhotkeys

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey


class SwHotkeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    hotkey: SwHotkey? = null
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