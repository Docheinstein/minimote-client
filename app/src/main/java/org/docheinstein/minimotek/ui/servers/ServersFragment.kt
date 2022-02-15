package org.docheinstein.minimotek.ui.servers

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.databinding.ServerListItemBinding
import org.docheinstein.minimotek.databinding.ServerListBinding
import org.docheinstein.minimotek.ui.discover.DiscoverDialogFragment
import org.docheinstein.minimotek.ui.server.AddEditServerViewModel
import org.docheinstein.minimotek.util.warn
import org.docheinstein.minimotek.util.debug

@AndroidEntryPoint
class ServersFragment : Fragment() {

    private val viewModel: ServersViewModel by viewModels()
    private lateinit var binding: ServerListBinding
    private lateinit var adapter: ServerListAdapter


    private class ServerDiffCallback : DiffUtil.ItemCallback<Server>() {
        override fun areItemsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem == newItem
        }
    }

    class ServerListAdapter() : ListAdapter<Server, ServerListAdapter.ServerListItemViewHolder>(ServerDiffCallback()) {
        var selection: Int? = null
            private set

        class ServerListItemViewHolder(
            val binding: ServerListItemBinding) :
            RecyclerView.ViewHolder(binding.root),
            View.OnCreateContextMenuListener {
                init {
                    binding.root.setOnCreateContextMenuListener(this)
                }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                view: View?,
                context_info: ContextMenu.ContextMenuInfo?
            ) {
                if (view != null && menu != null) {
                    val menuInflater = MenuInflater(view.context)
                    menuInflater.inflate(R.menu.server_context_menu, menu)
                    menu.setHeaderTitle(view.context.resources.getString(R.string.server_list_item_context_menu_title))
                }
            }
        }

        fun getSelectedServer(): Server? {
            return if (selection != null) currentList[selection!!] else null
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerListItemViewHolder {
            return ServerListItemViewHolder(
                ServerListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false),

            )
        }

        override fun onBindViewHolder(holder: ServerListItemViewHolder, position: Int) {
            val server = getItem(position)
            holder.binding.address.text = server.address
            holder.binding.name.text = server.name ?: server.address
            holder.binding.root.setOnClickListener {
                debug("Click on server $server")
            }
            holder.binding.root.setOnLongClickListener {
                selection = holder.adapterPosition
                debug("Selection changed to position $selection")
                false
            }
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        debug("ServersFragment.onCreateView()")

        binding = ServerListBinding.inflate(inflater, container, false)

        // Buttons
        binding.addServerButton.setOnClickListener {
            debug("uiAddServerButton.onClick()")
            handleAddServerButtonClick();
        }
        binding.discoverServersButton.setOnClickListener {
            debug("uiDiscoverServersButton.onClick()")
            handleDiscoverServersButtonClick()
        }

        // Server list
        adapter = ServerListAdapter()
        binding.serverList.adapter = adapter

        // Observe server list changes
        viewModel.servers.observe(viewLifecycleOwner) { servers ->
            debug("Server list update detected (new size = ${servers.size}, changing UI accordingly")
            adapter.submitList(servers)
        }

        return binding.root
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit_menu_item -> {
                val server = adapter.getSelectedServer()
                debug("Going to edit server at position ${adapter.selection}: ${server?.id}")
                if (server != null)
                    findNavController().navigate(
                        ServersFragmentDirections.actionAddEditServer(
                            server.id,
                            resources.getString(R.string.toolbar_title_edit_server))
                    )
            }
            R.id.delete_menu_item -> {
                val server = adapter.getSelectedServer()
                debug("Going to delete server at position ${adapter.selection}: ${server?.id}")
                if (server != null) {
                    AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.delete_server_confirmation_title)
                        .setMessage(R.string.delete_server_confirmation_message)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            // actually delete
                            viewModel.delete(server)
                            findNavController().navigateUp()
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun handleAddServerButtonClick() {
        findNavController().navigate(
            ServersFragmentDirections.actionAddEditServer(
                AddEditServerViewModel.SERVER_ID_NONE,
                resources.getString(R.string.toolbar_title_add_server)
            )
        )
    }

    private fun handleDiscoverServersButtonClick() {
        val discoveryFragment = DiscoverDialogFragment()
        discoveryFragment.show(childFragmentManager)
//        discoveryFragment.startDiscovery()
    }
}