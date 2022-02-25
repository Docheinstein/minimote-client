package org.docheinstein.minimotek.ui.base

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.util.debug


class IconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    companion object {
        private const val STATE_KEY_URI = "IconView.uri"
    }

    private var defaultResourceId: Int? = null
    private var defaultColor: Int? = null

    var icon: Uri? = null
        private set

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
        icon = uri
        if (uri != null) {
            // Valid URI, use it
            setImageURI(uri)
            imageTintList = null
        } else {
            // Use default resource and color, if provided
            if (defaultResourceId != null) {
                defaultResourceId?.let { setImageResource(it) }
                defaultColor?.let { imageTintList = ColorStateList.valueOf(it) }
            } else {
                setImageURI(null) // clear, no image to provide
            }
        }
    }

    // Handle orientation change internally

    // https://charlesharley.com/2012/programming/views-saving-instance-state-in-android
    // https://stackoverflow.com/questions/14891434/overriding-view-onsaveinstancestate-and-view-onrestoreinstancestate-using-vi

    class SavedState(superState: Parcelable?, val uri: Uri?) : BaseSavedState(superState) {
        override fun writeToParcel(out: Parcel?, flags: Int) {
            debug("SavedState.writeToParcel")
            super.writeToParcel(out, flags)
            out?.writeString(uri.toString())
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        debug("IconView.onSaveInstanceState()")
        debug("Saving uri = $icon")

        val superState = super.onSaveInstanceState()
        return SavedState(superState, icon)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        debug("IconView.onRestoreInstanceState()")
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        debug("Restored uri = ${savedState.uri}")
        setIcon(savedState.uri)
    }
}