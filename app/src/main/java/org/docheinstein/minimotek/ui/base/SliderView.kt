package org.docheinstein.minimotek.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.getIntegerOrThrow
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.SliderBinding
import org.docheinstein.minimotek.extensions.setOnSeekbarProgressListener
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.warn
import java.lang.Exception


class SliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val binding: SliderBinding

    var scaling: Int = 0
        set(value) {
            debug("SliderView.scaling setter")
            field = value
            updateUI()
        }

    var progress: Int = 0
        set(value) {
            debug("SliderView.progress setter for value = $value")
            field = value
            updateUI()
        }

    var progressScaled: Int
        get() = progress * scaling
        set(value) {
            progress = value / scaling
            if (value % scaling != 0) {
                warn("progressScaled % scaling should be == 0")
            }
        }

    init {
        debug("SliderView()")
        binding = SliderBinding.inflate(LayoutInflater.from(context), this)

        // https://developer.android.com/training/custom-views/create-view#customattr
        debug("Retrieving custom attributes")
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
                s = getIntegerOrThrow(R.styleable.Slider_scaling)
                p = getInteger(R.styleable.Slider_progress, 1)
                debug("Parsed attributes: min=${min}, max=${max}, progress=${p}, scaling=${s}")
            } catch (e: Exception) {
                error("Mandatory attributes of slider are missing (min, max, progress)")
                throw e
            }

            binding.seek.min = min
            binding.seek.max = max
            scaling = s
            progress = p

            recycle()
        }
    }


    fun setOnProgressListener(cb: (progress: Int) -> Unit) {
        binding.seek.setOnSeekbarProgressListener {
            debug("Slider notified about change")
            progress = it
            cb(progress)
        }
    }

    private fun updateUI() {
        debug("Updating Slider UI")
        binding.seek.progress = progress
        binding.text.text = (progress * scaling).toString()
    }
}