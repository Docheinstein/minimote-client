package org.docheinstein.minimotek.ui.server

import android.app.AlertDialog
import android.content.DialogInterface
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
import dagger.hilt.android.AndroidEntryPoint
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

@AndroidEntryPoint
class AddEditServerFragment : Fragment() {

    private val viewModel: AddEditServerViewModel by viewModels()
    private lateinit var binding: AddEditServerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val serverId = AddEditServerFragmentArgs.fromBundle(requireArguments()).serverId
        debug("AddEditServerFragment.onCreateView() for serverId = $serverId")

        binding = AddEditServerBinding.inflate(inflater, container, false)

        // Listen to fields change for real-time validation
        binding.address.addAfterTextChangedListener { validateAddress() }
        binding.port.addAfterTextChangedListener { validatePort()}

        // Listen to server details retrieval (in edit mode)
        viewModel.server.observe(viewLifecycleOwner) { server ->
            if (server != null) {
                debug("LiveData sent update for valid server, eventually updating UI")
                if (binding.address.text?.toString()?.isEmpty() == true) {
                    binding.address.setText(server.address)
                    binding.port.setText(server.port.toString())
                    binding.name.setText(server.name)
                }
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_server, menu)
        if (viewModel.mode == AddEditServerViewModel.Mode.ADD) {
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
        val name = binding.name.text.toString().ifEmpty { null }
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
        if (viewModel.mode == AddEditServerViewModel.Mode.ADD) {
            val s = Server(address, portInt, name)
            viewModel.insert(s)
        } else {
            val s = Server(viewModel.server.value!!.id, address, portInt, name)
            viewModel.update(s)
        }
        
        findNavController().navigateUp()
    }

    private fun handleDeleteButton() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.delete_server_confirmation_title)
            .setMessage(R.string.delete_server_confirmation_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                // actually delete
                viewModel.delete()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
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