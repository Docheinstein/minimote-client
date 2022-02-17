package org.docheinstein.minimotek.ui.discover

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.data.discover.DiscoveredServer
import org.docheinstein.minimotek.databinding.DiscoverDialogBinding
import org.docheinstein.minimotek.databinding.ServerListItemBinding
import org.docheinstein.minimotek.util.TAG
import org.docheinstein.minimotek.util.debug

@AndroidEntryPoint
class DiscoverDialogFragment : DialogFragment() {

    private val viewModel: DiscoverDialogViewModel by viewModels()
    private lateinit var binding: DiscoverDialogBinding
    private lateinit var adapter: DiscoveredServerListAdapter

    private class DiscoveredServerListDiffCallback : DiffUtil.ItemCallback<DiscoveredServer>() {
        override fun areItemsTheSame(oldItem: DiscoveredServer, newItem: DiscoveredServer): Boolean {
            return  oldItem.address == newItem.address &&
                    oldItem.port == newItem.port &&
                    oldItem.hostname == newItem.hostname
        }

        override fun areContentsTheSame(oldItem: DiscoveredServer, newItem: DiscoveredServer): Boolean {
            return oldItem == newItem
        }
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
            holder.binding.name.text = server.hostname
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
            Snackbar.make(
                requireParentFragment().requireView(),
                "${server.hostname} has been added", Snackbar.LENGTH_LONG
            ).show()
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

        viewModel.discoverError.observe(viewLifecycleOwner) { errorString ->
            if (errorString != null) {
                debug("Error occurred while discovering: $errorString")
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.discover_failed)
                    .setMessage("Failure reason: $errorString")
                    .setPositiveButton(R.string.ok, null)
                    .show()
                dismiss()
            }
        }

        viewModel.isDiscovering.observe(viewLifecycleOwner) { isDiscovering ->
            // Update UI when discover is completed
            binding.progressText.setText(if (isDiscovering) R.string.discover_discovering else R.string.discover_completed)
            binding.progressBar.isVisible = isDiscovering
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