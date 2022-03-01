package org.docheinstein.minimotek.ui.servers

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.AddEditServerBinding
import org.docheinstein.minimotek.util.addAfterTextChangedListener
import org.docheinstein.minimotek.util.*

/**
 * Fragment that handles the addition of a new server or the editing of an existing one.
 * The mode of the fragment depends on the parameter "serverId" used to create this fragment.
 * The screen allows the modification of the address/port of the server (which are validated),
 * the display name, and the icon associated with the server, which could be picked from
 * the filesystem using the implicit intent ACTION_OPEN_DOCUMENT.
 *
 *  The actions that can be performed on this screen are:
 * - Save the server
 * - Delete the server (only in EDIT mode)
 */

@AndroidEntryPoint
class AddEditServerFragment : Fragment() {

    private val viewModel: AddEditServerViewModel by viewModels()
    private lateinit var binding: AddEditServerBinding

    /*
     * NOTE
     * using startActivityForResult is deprecated right now, this is
     * the recommended way to start an implicit intent right now.
     * (https://developer.android.com/training/basics/intents/result#launch)
     */
    private lateinit var iconPickerIntent: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        verbose("AddEditServerFragment.onCreate()")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        iconPickerIntent = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            debug("Icon picker result is: $uri")
            handleIconPicked(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        verbose("AddEditServerFragment.onCreateView()")

        binding = AddEditServerBinding.inflate(inflater, container, false)

        // Listen to address/ports changes for real-time validation
        binding.address.addAfterTextChangedListener { validateAddress() }
        binding.port.addAfterTextChangedListener { validatePort()}

        // Icon selection buttons
        binding.iconClearer.setOnClickListener { handleClearIconAction() }
        binding.iconChooser.setOnClickListener { handlePickIconAction() }
        binding.icon.setOnClickListener { handlePickIconAction() }

        // Fetch hw hotkey (only in EDIT mode and only the first time)
        if (viewModel.mode == AddEditServerViewModel.Mode.EDIT && !viewModel.fetched) {
            viewModel.server?.observe(viewLifecycleOwner) { server ->
                debug("LiveData sent update for server $server, eventually updating UI")
                if (server == null) {
                    error("Invalid server")
                    return@observe
                }

                if (viewModel.fetched) {
                    warn("Already fetched, ignoring update")
                    return@observe
                }

                viewModel.fetched = true

                binding.address.setText(server.address)
                binding.port.setText(server.port.toString())
                binding.name.setText(server.name)
                binding.icon.icon = server.icon
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (viewModel.mode) {
            AddEditServerViewModel.Mode.ADD -> inflater.inflate(R.menu.save, menu)
            AddEditServerViewModel.Mode.EDIT -> inflater.inflate(R.menu.save_delete, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_menu_item -> {
                handleSaveAction()
                return true
            }
            R.id.delete_menu_item -> {
                handleDeleteAction()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSaveAction() {
        verbose("AddEditServerFragment.handleSaveAction()")

        val address = binding.address.text.toString()
        val port = binding.port.text.toString()
        val name = binding.name.text.toString().ifEmpty { null }
        val icon = binding.icon.icon

        debug("Going to save server (address=$address, port=$port, name=$name, icon=$icon")

        // Check whether address and port are valid
        if (!validateAddress()) {
            showInvalidAddressAlert()
            return
        }

        if (!validatePort()) {
            showInvalidPortAlert()
            return
        }

        val portInt = port.toInt()

        // Address and port are valid, actually save the server
        val server = viewModel.save(address, portInt, name, icon)

        Snackbar.make(
            requireParentFragment().requireView(),
            getString(R.string.saved, server.displayName),
            Snackbar.LENGTH_LONG
        ).show()

        findNavController().navigateUp()
    }

    private fun handleDeleteAction() {
        verbose("AddEditServerFragment.handleDeleteAction()")

        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.delete_server_confirmation_dialog_title)
            .setMessage(getString(R.string.delete_server_confirmation_dialog_message, viewModel.server?.value?.displayName ?: ""))
            .setPositiveButton(R.string.ok) { _, _ ->
                // Actually delete
                viewModel.delete()
                Snackbar.make(
                    requireParentFragment().requireView(),
                    getString(R.string.removed, viewModel.server?.value?.displayName),
                    Snackbar.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }


    private fun handleClearIconAction() {
        verbose("AddEditServerFragment.handleClearIconAction()")
        binding.icon.icon = null
    }

    private fun handlePickIconAction() {
        verbose("AddEditServerFragment.handlePickIconAction()")

        try {
            debug("Opening file picker")
            iconPickerIntent.launch(arrayOf("image/*"))
        } catch (e: ActivityNotFoundException) {
            warn("Cannot find any application able to choose an image")
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.icon_picker_failed_dialog_title)
                .setMessage(R.string.icon_picker_failed_dialog_message)
                .setPositiveButton(R.string.ok, null)
                .show();
        }
    }

    private fun handleIconPicked(uri: Uri?) {
        verbose("AddEditServerFragment.handleIconPicked(uri=$uri)")
        if (uri != null) {
            // Must take a persistable uri permission for use the same uri across reboots
            debug("Taking a persistable uri")
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireActivity().contentResolver.takePersistableUriPermission(uri, takeFlags)
            binding.icon.icon = uri
        }
    }

    private fun validateAddress(): Boolean {
        return if (NetUtils.isValidIPv4(binding.address.text.toString())) {
            binding.addressInputLayout.isErrorEnabled = false
            true
        } else {
            binding.addressInputLayout.isErrorEnabled = true
            binding.addressInputLayout.error = resources.getString(R.string.add_edit_server_invalid_address_form_message)
            false
        }
    }

    private fun validatePort(): Boolean {
        return if (NetUtils.isValidPort(binding.port.text.toString())) {
            binding.portInputLayout.isErrorEnabled = false
            true
        } else {
            binding.portInputLayout.isErrorEnabled = true
            binding.portInputLayout.error = resources.getString(R.string.add_edit_server_invalid_port_form_message)
            false
        }
    }

    private fun showInvalidAddressAlert() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.add_edit_server_invalid_address_dialog_title)
            .setMessage(R.string.add_edit_server_invalid_address_dialog_message)
            .setPositiveButton(R.string.ok, null)
            .show();
    }

    private fun showInvalidPortAlert() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.add_edit_server_invalid_port_dialog_title)
            .setMessage(R.string.add_edit_server_invalid_port_dialog_message)
            .setPositiveButton(R.string.ok, null)
            .show();
    }
}