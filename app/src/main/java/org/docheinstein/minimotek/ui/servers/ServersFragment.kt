package org.docheinstein.minimotek.ui.servers

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.databinding.ServerListItemBinding
import org.docheinstein.minimotek.databinding.ServerListBinding
import org.docheinstein.minimotek.ui.controller.base.SelectableListAdapter
import org.docheinstein.minimotek.ui.discover.DiscoverDialogFragment
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
            return oldItem.address == newItem.address && oldItem.name == newItem.name // UI based equality
        }
    }

    class ServerListAdapter : SelectableListAdapter<Server, ServerListAdapter.ViewHolder>(ServerDiffCallback()) {
        class ViewHolder(
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
                    menuInflater.inflate(R.menu.edit_delete, menu)
                    menu.setHeaderTitle(view.context.getString(R.string.server_list_item_context_menu_title))
                }
            }
        }

        override fun doCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ServerListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false),

            )
        }

        override fun doBindViewHolder(holder: ViewHolder, position: Int) {
            val server = getItem(position)
            holder.binding.address.text = server.address
            holder.binding.name.text = server.displayName()
            holder.binding.root.setOnClickListener {
                debug("Click on server $server")
                holder.itemView.findNavController().navigate(
                    ServersFragmentDirections.actionController(
                        server.address,
                        server.port,
                        server.displayName()
                ))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        debug("ServersFragment.onCreateView()")

        binding = ServerListBinding.inflate(inflater, container, false)

        // Discovery button
        binding.discoverServersButton.setOnClickListener {
            debug("uiDiscoverServersButton.onClick()")
            handleDiscoverServersButtonClick()
        }

        // Server list
        adapter = ServerListAdapter()
        binding.serverList.adapter = adapter

        // Observe server list changes
        viewModel.servers.observe(viewLifecycleOwner) { servers ->
            debug("Server list update detected (new size = ${servers.size}, changing UI accordingly)")
            adapter.submitList(servers)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_menu_item -> {
                handleAddServerButton()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit_menu_item -> {
                val server = adapter.selected()
                debug("Going to edit server at position ${adapter.selection}: ${server?.id}")
                if (server != null)
                    findNavController().navigate(
                        ServersFragmentDirections.actionAddEditServer(
                            server.id,
                            getString(R.string.toolbar_title_edit_server))
                    )
            }
            R.id.delete_menu_item -> {
                val server = adapter.selected()
                debug("Going to delete server at position ${adapter.selection}: ${server?.id}")
                if (server != null) {
                    AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.delete_server_confirmation_title)
                        .setMessage(R.string.delete_server_confirmation_message)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            // actually delete
                            viewModel.delete(server)
                            Snackbar.make(
                                requireParentFragment().requireView(),
                                getString(R.string.server_removed, server.displayName()),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun handleAddServerButton() {
        findNavController().navigate(
            ServersFragmentDirections.actionAddEditServer(
                AddEditServerViewModel.SERVER_ID_NONE,
                getString(R.string.toolbar_title_add_server)
            )
        )
    }

    private fun handleDiscoverServersButtonClick() {
        val discoveryFragment = DiscoverDialogFragment()
        discoveryFragment.show(childFragmentManager)
    }
}