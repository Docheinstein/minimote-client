package org.docheinstein.minimote.ui.servers

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
import org.docheinstein.minimote.R
import org.docheinstein.minimote.database.server.Server
import org.docheinstein.minimote.databinding.ServerListItemBinding
import org.docheinstein.minimote.databinding.ServerListBinding
import org.docheinstein.minimote.ui.base.SelectableListAdapter
import org.docheinstein.minimote.ui.discover.DiscoverDialogFragment
import org.docheinstein.minimote.util.debug
import org.docheinstein.minimote.util.verbose

/**
 * Fragment representing the server list.
 * The actions that can be performed on this screen are:
 * - Add a server (opens AddEdit screen)
 * - Edit a server (opens AddEdit screen)
 * - Delete a server
 * - Start the discover procedure
 */
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
             // UI based equality
            return oldItem.address == newItem.address &&
                    oldItem.name == newItem.name &&
                    oldItem.icon == newItem.icon
        }
    }

    class ServerListAdapter : SelectableListAdapter<Server, ServerListAdapter.ViewHolder>(ServerDiffCallback()) {
        class ViewHolder(val binding: ServerListItemBinding) :
                RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
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
                    menu.setHeaderTitle(view.context.getString(R.string.choose_an_action))
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

            // Texts
            holder.binding.address.text = server.address
            holder.binding.name.text = server.displayName

            // Icon
            holder.binding.icon.icon = server.icon

            // Click listener: open ControllerFragment
            holder.binding.root.setOnClickListener {
                debug("Click on server $server")
                holder.itemView.findNavController().navigate(
                    ServersFragmentDirections.actionController(
                        server.address,
                        server.port,
                        server.displayName
                ))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        verbose("ServersFragment.onCreate()")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        verbose("ServersFragment.onCreateView()")

        binding = ServerListBinding.inflate(inflater, container, false)

        // Server list adapter
        adapter = ServerListAdapter()
        binding.serverList.adapter = adapter

        // Discover button
        binding.discoverServersButton.setOnClickListener { handleDiscoverAction() }

        // Observe servers changes
        viewModel.servers.observe(viewLifecycleOwner) { servers ->
            debug("Servers update received in UI (size = ${servers.size})")
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
                handleAddAction()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_menu_item -> {
                handleEditAction()
                return true
            }
            R.id.delete_menu_item -> {
                handleDeleteAction()
                return true
            }
        }

        return super.onContextItemSelected(item)
    }

    private fun handleDiscoverAction() {
        verbose("ServersFragment.handleDiscoverServersAction()")

        // Open the discover dialog
        val discoveryFragment = DiscoverDialogFragment()
        discoveryFragment.show(childFragmentManager)
    }

    private fun handleAddAction() {
        verbose("ServersFragment.handleAddAction()")

        findNavController().navigate(
            ServersFragmentDirections.actionAddEditServer(
                AddEditServerViewModel.SERVER_ID_NONE,
                getString(R.string.toolbar_title_add_server)
            )
        )
    }

    private fun handleEditAction() {
        verbose("ServersFragment.handleEditAction()")

        val server = adapter.selectedItem
        debug("Going to edit server at position ${adapter.selectedPosition}: ${server?.id}")
        if (server != null) {
            findNavController().navigate(
                ServersFragmentDirections.actionAddEditServer(
                    server.id,
                    getString(R.string.toolbar_title_edit_server)
                )
            )
        }
    }

    private fun handleDeleteAction() {
        verbose("ServersFragment.handleDeleteAction()")

        val server = adapter.selectedItem
        debug("Going to delete server at position ${adapter.selectedPosition}: ${server?.id}")
        if (server != null) {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.delete_server_confirmation_dialog_title)
                .setMessage(getString(R.string.delete_server_confirmation_dialog_message, server.displayName))
                .setPositiveButton(R.string.ok) { _, _ ->
                    // actually delete
                    viewModel.delete(server.id)
                    Snackbar.make(
                        requireParentFragment().requireView(),
                        getString(R.string.removed, server.displayName),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
}