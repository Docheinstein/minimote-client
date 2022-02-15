package org.docheinstein.minimotek.ui.discover

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.data.discover.DiscoveredServer
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.databinding.DiscoverDialogBinding
import org.docheinstein.minimotek.databinding.ServerListItemBinding
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.info
import org.docheinstein.minimotek.util.warn

@AndroidEntryPoint
class DiscoverDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "DiscoveryDialogFragment"
    }

    private val viewModel: DiscoverDialogViewModel by viewModels()
    private lateinit var binding: DiscoverDialogBinding
    private lateinit var adapter: DiscoveredServerListAdapter

    private class DiscoveredServerListDiffCallback : DiffUtil.ItemCallback<DiscoveredServer>() {
        override fun areItemsTheSame(oldItem: DiscoveredServer, newItem: DiscoveredServer): Boolean {
            return  oldItem.address == newItem.address &&
                    oldItem.port == newItem.port &&
                    oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: DiscoveredServer, newItem: DiscoveredServer): Boolean {
            return oldItem == newItem
        }
    }

    private interface DiscoveredServerListListener {
        fun onAddServer(server: DiscoveredServer)
    }

    private class DiscoveredServerListAdapter(private val addServerCallback: (DiscoveredServer) -> Unit) :
        ListAdapter<DiscoveredServer, DiscoveredServerListAdapter.DiscoveredServerListItemViewHolder>
            (DiscoveredServerListDiffCallback()) {

        class DiscoveredServerListItemViewHolder(val binding: ServerListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoveredServerListItemViewHolder {
            return DiscoveredServerListItemViewHolder(
                ServerListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: DiscoveredServerListItemViewHolder, position: Int) {
            val server = getItem(position)
            holder.binding.address.text = server.address
            holder.binding.name.text = server.name ?: server.address
            holder.binding.root.setOnClickListener {
                debug("Click on server $server")
                addServerCallback.invoke(server)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.StandardDialog);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        debug("DiscoveryDialogFragment.onCreateView()")
        binding = DiscoverDialogBinding.inflate(inflater, container, false)
        adapter = DiscoveredServerListAdapter { server ->
            debug("Server to add $server")
            viewModel.insert(server)
            dismiss()
        }
        binding.discoveredServerList.adapter = adapter

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        viewModel.discoveredServers.observe(viewLifecycleOwner) { servers ->
            debug("New discovered servers list: $servers")
            adapter.submitList(servers.toList())
            debug("Current size = ${adapter.currentList.size} ${adapter.itemCount}")
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        debug("DiscoveryDialogFragment.onAttach()")
    }

    fun show(manager: FragmentManager) {
        debug("DiscoveryDialogFragment.show()")
        show(manager, TAG)
    }
}