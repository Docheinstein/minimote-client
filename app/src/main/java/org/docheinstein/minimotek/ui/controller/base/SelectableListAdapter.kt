package org.docheinstein.minimotek.ui.controller.base

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.docheinstein.minimotek.util.debug

abstract class SelectableListAdapter <T, VH : RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(diffCallback) {

    var selection: Int? = null
        private set

    var selected: T? = if (selection != null) currentList[selection!!] else null
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return doCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        doBindViewHolder(holder, position)
        holder.itemView.setOnLongClickListener {
            debug("Long click on item at position ${holder.adapterPosition}")
            selection = holder.adapterPosition
            false
        }
    }

    abstract fun doCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun doBindViewHolder(holder: VH, position: Int)
}