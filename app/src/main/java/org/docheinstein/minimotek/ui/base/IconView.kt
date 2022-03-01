package org.docheinstein.minimotek.ui.base

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.verbose

/**
 * ImageView that allows the image to be changed with an image given by an [Uri]
 * (i.e. an image on the storage). Compared to the standard [AppCompatImageView],
 * this class keeps a default resource image (and eventually a default tint to apply to the
 * resource image) as fallback in the case the Uri is null or not given at all.
 * Furthermore this view handles the orientation changes internally with [onSaveInstanceState]
 * and [onRestoreInstanceState] for remember the uri that has been set.
 */

class IconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val defaultResourceId: Int?
    private val defaultColor: Int?

    var icon: Uri? = null
        set(value) {
            verbose("IconView.icon.set($value)")
            field = value
            updateUI()
        }

    init {
        verbose("IconView.init()")

        // https://developer.android.com/training/custom-views/create-view#customattr
        debug("Retrieving custom attributes for IconView")
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Icon, 0, 0).apply {

            val resId = getResourceId(R.styleable.Icon_default_src, 0)
            defaultResourceId = if (resId != 0) resId else null

            val tint = getColor(R.styleable.Icon_default_color, 0)
            defaultColor = if (tint != 0) tint else null

            debug("Parsed attributes: resId=${resId}, tint=${tint}")

            recycle()
        }

        updateUI()
    }

    private fun updateUI() {
        /*
         * Set the icon with the given uri if it's not null, otherwise use the default resource
         * given with the attributes 'default_src', using 'default_color' as tint.
         */
        if (icon != null) {
            // Valid URI, use it
            // Note: setImageURI might fail if the image referred by the uri does not
            // exists anymore, but unfortunately it does not throw any exception because
            // it is caught inside the library, therefore, unless using some magic trick,
            // we have to tolerate that the IconView will show an empty image instead
            // of the default one, as desired and expected
            setImageURI(icon)
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
    // https://mateuszteteruk.pl/how-to-implement-saved-state-of-android-custom-view-in-kotlin/
    // https://medium.com/android-news/keddit-part-8-orientation-change-with-kotlin-parcelable-data-classes-f28136e8a6a8
    // https://www.netguru.com/blog/how-to-correctly-save-the-state-of-a-custom-view-in-android

    // NOTE
    // this ain't easy to test since writeToParcel usually is not even called on
    // rotation changes due to optimizations, therefore the save/restore works independently
    // on what SavedState actually does, because it's kept in memory and the serialization
    // is not taken into account
    class SavedState : BaseSavedState {
        var uri: Uri? = null

        constructor(source: Parcel) : super(source) {
            verbose("SavedState(source: Parcel)")
            val uriStr = source.readString()
            uri = if (uriStr != null) Uri.parse(uriStr) else uriStr
            debug("Restored uri = $uri")
        }

        constructor(source: Parcel?, loader: ClassLoader?) : super(source, loader) {
            verbose("SavedState(source: Parcel?, loader: ClassLoader?)")
        }
        constructor(superState: Parcelable?) : super(superState) {
            verbose("SavedState(superState: Parcelable?)")
        }


        override fun writeToParcel(out: Parcel?, flags: Int) {
            debug("SavedState.writeToParcel, saving uri = $uri")
            super.writeToParcel(out, flags)
            out?.writeString(uri?.toString())
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.ClassLoaderCreator<SavedState> = object :
                Parcelable.ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source, null)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
                override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState =
                    SavedState(source, loader)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        verbose("IconView.onSaveInstanceState()")
        debug("Saving uri = $icon")

        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.uri = icon
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        verbose("IconView.onRestoreInstanceState()")
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        debug("Restored uri = ${savedState.uri}")
        icon = savedState.uri
    }
}