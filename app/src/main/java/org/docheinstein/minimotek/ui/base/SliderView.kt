package org.docheinstein.minimotek.ui.base

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.res.getIntegerOrThrow
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.SliderBinding
import org.docheinstein.minimotek.util.*
import java.lang.Exception


/**
 * Layout consisting of a [SeekBar] and a [TextView] representing the current progress of the bar.
 * Furthermore, this class allows a [scaling] parameters that defines the factor for which
 * the text shown in the [TextView] is multiplied in relation to the progress of the [SeekBar].
 * (e.g. with scaling = 2, if the progress of the seek bar is 10, the text shown 20).
 */
class SliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val binding: SliderBinding

    private val scaling: Int

    var progress: Int = 0
        set(value) {
            verbose("SliderView.progress.set($value)")
            field = value
            updateUI()
        }

    var progressScaled: Int
        get() = progress * scaling
        set(value) {
            verbose("SliderView.progressScaled.set($value)")
            progress = value / scaling
            if (value % scaling != 0)
                warn("progressScaled % scaling should be == 0")
        }

    var callback: ((progress: Int) -> Unit?)? = null

    init {
        verbose("SliverView.init()")
        binding = SliderBinding.inflate(LayoutInflater.from(context), this)

        // https://developer.android.com/training/custom-views/create-view#customattr
        debug("Retrieving custom attributes for SliderView")
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Slider, 0, 0).apply {

            val min: Int
            val max: Int
            val s: Int
            val p: Int
            try {
                min = getIntegerOrThrow(R.styleable.Slider_min)
                max = getIntegerOrThrow(R.styleable.Slider_max)
                p = getIntegerOrThrow(R.styleable.Slider_progress)
                s = getInteger(R.styleable.Slider_scaling, 1)
                debug("Parsed attributes: (min=${min}, max=${max}, progress=${p}, scaling=${s})")
            } catch (e: Exception) {
                error("Mandatory attributes of SliderView are missing (min, max, progress)")
                throw e
            }

            binding.seek.min = min
            binding.seek.max = max
            scaling = s
            progress = p

            recycle()
        }

        binding.seek.setOnSeekbarProgressListener {
            handleProgressChanged(it)
        }
    }

    private fun handleProgressChanged(p: Int) {
        debug("SliderView progress changed: $p")
        progress = p
        callback?.let { it(p) }
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

    // These two calls are needed in order to have multiple compound views in the same layout,
    // otherwise there would be multiple views with the same ids and the save/restore
    // mechanism would fail
    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
        dispatchThawSelfOnly(container)
    }

    class SavedState : BaseSavedState {
        var progress: Int = -1

        constructor(source: Parcel) : super(source) {
            verbose("SavedState(source: Parcel)")
            progress = source.readInt()
        }

        constructor(source: Parcel?, loader: ClassLoader?) : super(source, loader) {
            verbose("SavedState(source: Parcel?, loader: ClassLoader?)")
        }

        constructor(superState: Parcelable?) : super(superState) {
            verbose("SavedState(superState: Parcelable?)")
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            debug("SavedState.writeToParcel")
            super.writeToParcel(out, flags)
            out?.writeInt(progress)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.ClassLoaderCreator<SavedState> = object :
                Parcelable.ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState  {
                    verbose("SavedState.CREATOR.createFromParcel(source: Parcel)")
                    return SavedState(source, null)
                }
                override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState {
                    verbose("SavedState.CREATOR.createFromParcel(source: Parcel, loader: ClassLoader)")
                    return SavedState(source, loader)
                }
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)

        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        verbose("SliderView.onRestoreInstanceState()")

        // TODO: progress = 0 should be legal
        if (savedState.progress > 0) {
            debug("Restored progress = ${savedState.progress}")
            progress = savedState.progress
        }

    }

    override fun onSaveInstanceState(): Parcelable? {
        verbose("SliderView.onSaveInstanceState()")

        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        debug("Saving progress = $progress")
        savedState.progress = progress
        return savedState
    }

    private fun updateUI() {
        binding.seek.progress = progress
        binding.text.text = progressScaled.toString()
    }
}