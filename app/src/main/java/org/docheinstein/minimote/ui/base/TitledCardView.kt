package org.docheinstein.minimote.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import org.docheinstein.minimote.R
import org.docheinstein.minimote.databinding.TitledCardBinding
import org.docheinstein.minimote.util.verbose
import org.docheinstein.minimote.util.debug


/**
 * Layout consisting of [CardView] with a title.
 * Views nested in this view will be placed under the title,
 * in the "content area" of the card.
 */

class TitledCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : CardView(context, attrs, defStyle) {

    private val binding: TitledCardBinding

    init {
        verbose("TitledCardView.init()")
        binding = TitledCardBinding.inflate(LayoutInflater.from(context), this)

        // https://developer.android.com/training/custom-views/create-view#customattr
        debug("Retrieving custom attributes for TitledCardView")
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

    /*
     * This is pretty hackish but it's the best way I've found to add XML inflated views
     * to the nested LinearLayout (R.id.content) instead of add them to the CardView layout.
     * Nested view must be wrapped in a LinearLayout anyway because the LinearLayout params otherwise
     * are lost for some reason (probably because the CardView extends FrameLayout
     * and therefore the params specific for LinearLayout are dropped somewhere).
     */
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        debug("TitledCardView.addView called for view: ${child?.id}")
        if (child?.id == R.id.content) {
            debug("Adding view to root")
            super.addView(child, index, params)
        } else {
            debug("Adding view to content")
            binding.content.addView(child, index, params)
        }
    }
}