package org.docheinstein.minimotek.ui.base

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContentProviderCompat.requireContext
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.util.debug


class IconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private var defaultResourceId: Int? = null
    private var defaultColor: Int? = null

    init {
        debug("IconView()")

        // https://developer.android.com/training/custom-views/create-view#customattr
        debug("Retrieving custom attributes")
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Icon, 0, 0).apply {

            val resId = getResourceId(R.styleable.Icon_default_src, 0)
            if (resId != 0)
                defaultResourceId = resId

            val tint = getColor(R.styleable.Icon_default_color, 0)
            if (tint != 0)
                defaultColor = tint

            debug("Parsed attributes: resId=${resId}, tint=${tint}")

            recycle()
        }

        setIcon(null)
    }

    /**
     * Set the icon with the given uri if it's not null, otherwise use the default resource
     * given with the attributes 'default_src', using 'default_color' as tint
     */
    fun setIcon(uri: Uri?) {
        debug("IconView.setIcon() for uri = $uri")
        if (uri == null) {
            // Use default resource and color, if provided
            if (defaultResourceId != null) {
                defaultResourceId?.let { setImageResource(it) }
                defaultColor?.let { imageTintList = ColorStateList.valueOf(it) }
            } else {
                setImageURI(null) // clear, no image to provide
            }
        } else {
            setImageURI(uri)
            imageTintList = null
        }
    }
}