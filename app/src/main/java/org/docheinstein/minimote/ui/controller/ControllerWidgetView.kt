package org.docheinstein.minimote.ui.controller

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.getColorOrThrow
import org.docheinstein.minimote.R
import org.docheinstein.minimote.util.debug
import org.docheinstein.minimote.util.error
import org.docheinstein.minimote.util.verbose
import java.lang.Exception

/**
 * ImageView representing a widget of the controller's widget bar.
 * Actually this consists of an ImageView with two state: highlighted and unhighlighted,
 * that applies a tint to the image depending on the state.
 */
class ControllerWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val highlightedTint: ColorStateList
    private val unhighlightedTint: ColorStateList

    var highlighted: Boolean = false
        set(value) {
            field = value
            updateUI()
        }

    init {
        verbose("ControllerWidgetView.init()")

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ControllerWidget, 0, 0).apply {

            try {
                val highlightedColor = getColorOrThrow(R.styleable.ControllerWidget_highlight_color)
                val unhighlightedColor = getColorOrThrow(R.styleable.ControllerWidget_unhighlight_color)
                debug("Parsed attributes: (highlightedColor=${highlightedColor}, unhighlightedColor=${unhighlightedColor})")
                highlightedTint = ColorStateList.valueOf(highlightedColor)
                unhighlightedTint = ColorStateList.valueOf(unhighlightedColor)
            } catch (e: Exception) {
                error("Mandatory attributes of ControllerWidgetView are missing (highlightedColor, unhighlightedColor)")
                throw e
            }

            recycle()
        }

        updateUI()
    }

    private fun updateUI() {
        imageTintList = if (highlighted) highlightedTint else unhighlightedTint
    }
}