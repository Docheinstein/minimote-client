package org.docheinstein.minimotek.ui.servers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.databinding.AddEditServerBinding
import org.docheinstein.minimotek.databinding.ServerListItemBinding
import org.docheinstein.minimotek.databinding.ServerListBinding
import org.docheinstein.minimotek.util.warn
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.debug

class ServersFragment : Fragment() {

    private val viewModel: ServersViewModel by viewModels()
    private lateinit var binding: ServerListBinding
    private lateinit var adapter: ServerListAdapter

    class ServerListAdapter() : RecyclerView.Adapter<ServerListAdapter.ServerListItemViewHolder>() {
        private var servers: List<Server>? = null

        class ServerListItemViewHolder(val binding: ServerListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        }

        fun setServers(servers: List<Server>) {
            this.servers = servers
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerListItemViewHolder {
            debug("Creating serverViewHolder")
            return ServerListItemViewHolder(
                ServerListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ServerListItemViewHolder, position: Int) {
            debug("Binding serverViewHolder at pos $position")

            if (servers == null) {
                warn("Null server list")
                return
            }
            val server = servers!![position]
            holder.binding.address.text = server.address
            holder.binding.name.text = server.name ?: server.address
        }

        override fun getItemCount(): Int {
            debug("getItemCount called, now is = ${servers?.size}")
            if (servers == null) {
                warn("Null server list")
                return 0
            }

            return servers!!.size
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
        viewModel.servers.observe(viewLifecycleOwner) {
            debug("Server list update detected (new size = ${it.size}, changing UI accordingly")
            adapter.setServers(it)
        }

        return binding.root
    }

    private fun handleAddServerButtonClick() {
        findNavController().navigate(ServersFragmentDirections.actionAddEditServer(null))
    }

    private fun handleDiscoverServersButtonClick() {

    }
}