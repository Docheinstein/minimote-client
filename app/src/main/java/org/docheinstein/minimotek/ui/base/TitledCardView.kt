package org.docheinstein.minimotek.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.res.getIntegerOrThrow
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.SliderBinding
import org.docheinstein.minimotek.databinding.TitledCardBinding
import org.docheinstein.minimotek.extensions.setOnSeekbarProgressListener
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.warn
import java.lang.Exception


class TitledCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : CardView(context, attrs, defStyle) {

    private val binding: TitledCardBinding

    var scaling: Int = 0
        set(value) {
            debug("SliderView.scaling setter")
            field = value
            updateUI()
        }

    var progress: Int = 0
        set(value) {
            debug("SliderView.progress setter")
            field = value
            updateUI()
        }

    val progressScaled: Int
        get() = progress * scaling

    init {
        debug("TitledCardView()")
        binding = TitledCardBinding.inflate(LayoutInflater.from(context), this)

        // https://developer.android.com/training/custom-views/create-view#customattr
        debug("Retrieving custom attributes")
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TitledCard, 0, 0).apply {

            cardElevation = getDimension(R.styleable.TitledCard_elevation, 0f)
            radius = getDimension(R.styleable.TitledCard_radius, 0f)
            binding.title.text = getString(R.styleable.TitledCard_title)

            debug("Parsed attributes: elevation=${elevation}, radius=${radius}, title=${binding.title.text}")

            recycle()
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        debug("TitledCardView.addView called for view: ${child?.id}")
        if (child?.id == R.id.content) {
            debug("Adding to root")
            super.addView(child, index, params)
        } else {
            debug("Adding to root")
            binding.content.addView(child, index, params)
        }
    }

    private fun updateUI() {
        debug("Updating TitledCardView UI")
    }
}