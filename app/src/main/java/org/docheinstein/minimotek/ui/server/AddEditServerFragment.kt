package org.docheinstein.minimotek.ui.server

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.data.DB
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.databinding.AddEditServerBinding
import org.docheinstein.minimotek.util.NetUtils
import org.docheinstein.minimotek.util.addAfterTextChangedListener
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.debug

class AddEditServerFragment : Fragment() {

    private val viewModel: AddEditServerViewModel by viewModels()
    private lateinit var binding: AddEditServerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        debug("AddEditServerFragment.onCreateView()")

        val serverId = AddEditServerFragmentArgs.fromBundle(requireArguments()).serverId

        debug("ServerId = $serverId")

        binding = AddEditServerBinding.inflate(inflater, container, false)

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

        binding.address.addAfterTextChangedListener { validateAddress() }
        binding.port.addAfterTextChangedListener { validatePort()}

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
        val name = if (!binding.name.text.toString().isEmpty()) binding.name.text.toString() else null
        debug("Address: $address")
        debug("Port: $port")

        if (!NetUtils.isValidIPv4(address)) {
            updateUI()
            showInvalidAddressAlert()
            return
        }

        if (!NetUtils.isValidPort(port)) {
            updateUI()
            showInvalidPortAlert()
            return
        }

        val portInt = port.toInt()

        // Address and port are valid, actually add/update the server
        debug("Valid address and port, proceeding")
        val server = Server(address, portInt, name)
        runBlocking { lifecycleScope.launch {
            // TODO: use ServerRepository
            DB.getInstance(requireContext()).serverDao().add(server)
            requireActivity().runOnUiThread {
                findNavController().navigateUp()
            }
        }}

        return
    }

    private fun handleDeleteButton() {

    }

    private fun updateUI() {
        validateAddress()
        validatePort()
    }

    private fun validateAddress() {
        if (NetUtils.isValidIPv4(binding.address.text.toString())) {
            binding.addressInputLayout.isErrorEnabled = false
        } else {
            binding.addressInputLayout.isErrorEnabled = true
            binding.addressInputLayout.error = resources.getString(R.string.add_edit_server_invalid_address)
        }
    }

    private fun validatePort() {
        if (NetUtils.isValidPort(binding.port.text.toString())) {
            binding.portInputLayout.isErrorEnabled = false
        } else {
            binding.portInputLayout.isErrorEnabled = true
            binding.portInputLayout.error = resources.getString(R.string.add_edit_server_invalid_port)
        }
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