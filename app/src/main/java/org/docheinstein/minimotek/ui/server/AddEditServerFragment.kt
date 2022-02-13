package org.docheinstein.minimotek.ui.server

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.data.DB
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.databinding.AddEditServerBinding
import org.docheinstein.minimotek.util.NetUtils
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.debug

class AddEditServerFragment : Fragment() {

    private val viewModel: AddEditServerViewModel by viewModels()

    private var _binding: AddEditServerBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        debug("AddEditServerFragment.onCreateView()")

        val serverId = AddEditServerFragmentArgs.fromBundle(requireArguments()).serverId

        debug("ServerId = $serverId")

        // Title
        val toolbarTitle: String
        if (serverId != null) {
            toolbarTitle = "Edit server"
            viewModel.purpose = AddEditServerViewModel.Purpose.EDIT
        } else {
            toolbarTitle = "Add server"
            viewModel.purpose = AddEditServerViewModel.Purpose.ADD
        }
        requireActivity().findViewById<Toolbar>(R.id.toolbar).title = toolbarTitle
        _binding = AddEditServerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_server, menu)
        if (viewModel.purpose == AddEditServerViewModel.Purpose.ADD) {
            menu.removeItem(R.id.delete_menu_item)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_menu_item -> {
                handleSaveButton()
                return true
            }
            R.id.delete_menu_item -> {
                handleDeleteButton()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSaveButton() {
        debug("Clicked on uiSaveServerMenuItem")

        val address = binding.address.text.toString()
        val port = binding.port.text.toString()
        debug("Address: $address")
        debug("Port: $port")

        // Address validation
        if (!NetUtils.isValidIPv4(address)) {
            error("Invalid IPv4: $address")
            showInvalidAddressAlert()
            return
        }

        // Port validation
        val portInt: Int

        try {
            portInt = port.toInt()
        } catch (e: NumberFormatException) {
            error("Invalid port: ${port}")
            showInvalidPortAlert()
            return
        }

        if (!NetUtils.isValidPort(portInt)) {
            error("Invalid port: $portInt")
            showInvalidPortAlert()
            return
        }

        // Address and port are valid,
        // actually add the server
        debug("Valid address and port, proceeding")
        val server = Server(address, portInt)
        runBlocking { lifecycleScope.launch {
            debug("Saving...")
            DB.getInstance(requireContext()).serverDao().add(server)
            debug("Saved")
        }}
        debug("Saved")

        return
    }

    private fun handleDeleteButton() {

    }

    private fun showInvalidAddressAlert() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.add_server_failed_address_dialog_title)
            .setMessage(R.string.add_server_failed_address_dialog_message)
            .setPositiveButton(R.string.ok, null)
            .show();
    }

    private fun showInvalidPortAlert() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.add_server_failed_port_dialog_title)
            .setMessage(R.string.add_server_failed_port_dialog_message)
            .setPositiveButton(R.string.ok, null)
            .show();
    }
}