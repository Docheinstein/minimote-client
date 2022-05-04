package org.docheinstein.minimote.ui.discover

import android.app.AlertDialog
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
import org.docheinstein.minimote.R
import org.docheinstein.minimote.discover.DiscoveredServer
import org.docheinstein.minimote.databinding.DiscoverDialogBinding
import org.docheinstein.minimote.databinding.ServerListItemBinding
import org.docheinstein.minimote.util.TAG
import org.docheinstein.minimote.util.debug
import org.docheinstein.minimote.util.verbose

/**
 * Fragment representing the discover procedure.
 * It contains the discovered server list.
 */

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
             // UI based equality
            return oldItem.address == newItem.address &&
                    oldItem.port == newItem.port &&
                    oldItem.hostname == newItem.hostname
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

            // Texts
            holder.binding.address.text = server.address
            holder.binding.name.text = server.hostname

            // Click listener: invoke callback (which adds server to the server list)
            holder.binding.root.setOnClickListener {
                debug("Click on server $server")
                addServerCallback.invoke(server)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        verbose("DiscoverDialogFragment.onCreate()")
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.StandardDialog);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        verbose("DiscoveryDialogFragment.onCreateView()")
        binding = DiscoverDialogBinding.inflate(inflater, container, false)

        // Discovered server list adapter
        adapter = DiscoveredServerListAdapter { server ->
            debug("Server to add $server")
            viewModel.insert(server)
            Snackbar.make(
                requireParentFragment().requireView(),
                getString(R.string.added, server.hostname),
                Snackbar.LENGTH_LONG
            ).show()
            dismiss()
        }
        binding.discoveredServerList.adapter = adapter

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        // Observe new discovered servers
        viewModel.discoveredServers.observe(viewLifecycleOwner) { servers ->
            debug("New discovered servers list (size = ${servers.size})")
            adapter.submitList(servers.toList()) // must make a copy for perform a diff
        }

        // Observe discover errors
        viewModel.discoverError.observe(viewLifecycleOwner) { errorString ->
            if (errorString != null) {
                debug("Error occurred while discovering: $errorString")
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.discover_failed_dialog_title)
                    .setMessage(getString(R.string.discover_failed_dialog_message, errorString))
                    .setPositiveButton(R.string.ok, null)
                    .show()
                dismiss()
            }
        }

        // Observe discover procedure state
        viewModel.isDiscovering.observe(viewLifecycleOwner) { isDiscovering ->
            binding.progressText.setText(if (isDiscovering) R.string.discover_discovering else R.string.discover_completed)
            binding.progressBar.isVisible = isDiscovering
        }

        return binding.root
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }
}