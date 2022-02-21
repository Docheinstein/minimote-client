package org.docheinstein.minimotek.ui.controller.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.util.debug

class ControllerWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    init {
        debug("ControllerWidgetView()")
        unhighlight()
    }

    fun setHighlight(yes: Boolean) {
        if (yes)
            highlight()
        else
            unhighlight()
    }

    fun highlight() {
        imageTintList = context.getColorStateList(R.color.controller_widget_highlighted)
    }

    fun unhighlight() {
        imageTintList = context.getColorStateList(R.color.controller_widget_unhighlighted)
    }
}