package org.docheinstein.minimotek.ui.base

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.docheinstein.minimotek.util.debug

/**
 * Extension of a [ListAdapter] that automatically keeps track of the
 * last item that has been selected with a long click.
 */
abstract class SelectableListAdapter <T, VH : RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(diffCallback) {

    var selectedPosition: Int? = null
        private set

    val selectedItem: T?
        get() = selectedPosition?.let { currentList[it] }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return doCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        doBindViewHolder(holder, position)
        holder.itemView.setOnLongClickListener {
            debug("Long click on item at position ${holder.adapterPosition}")
            selectedPosition = holder.adapterPosition
            false
        }
    }

    abstract fun doCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun doBindViewHolder(holder: VH, position: Int)
}