package org.docheinstein.minimotek.ui.server

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.databinding.AddEditServerBinding
import org.docheinstein.minimotek.extensions.addAfterTextChangedListener
import org.docheinstein.minimotek.util.NetUtils
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn

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

        // Fetch server details (only the first time)
        if (viewModel.server?.value == null &&
            viewModel.mode == AddEditServerViewModel.Mode.EDIT) {
            viewModel.server?.observe(viewLifecycleOwner) { server ->
                debug("LiveData sent update for server $server, eventually updating UI")
                if (server != null) {
                    debug("Fetched server is valid")
                    binding.address.setText(server.address)
                    binding.port.setText(server.port.toString())
                    binding.name.setText(server.name)
                } else {
                    warn("Invalid server")
                }
            }
        }

        return binding.root
    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putBoolean("new", false)
//        super.onSaveInstanceState(outState)
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_delete, menu)
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
            Snackbar.make(
                requireParentFragment().requireView(),
                getString(R.string.server_added, s.name ?: s.address),
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            val s = Server(viewModel.server?.value!!.id, address, portInt, name)
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
                Snackbar.make(
                    requireParentFragment().requireView(),
                    getString(R.string.server_removed, viewModel.server?.value?.displayName()),
                    Snackbar.LENGTH_LONG
                ).show()
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