package org.docheinstein.minimotek.ui.hotkey

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.database.hwhotkey.HwHotkey
import org.docheinstein.minimotek.databinding.AddEditHotkeyBinding
import org.docheinstein.minimotek.databinding.AddEditHwHotkeyBinding
import org.docheinstein.minimotek.extensions.setSelection
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.ui.hwhotkey.AddEditHwHotkeyViewModel
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn


@AndroidEntryPoint
class AddEditHotkeyFragment : Fragment() {

    private val viewModel: AddEditHotkeyViewModel by viewModels()
    private lateinit var binding: AddEditHotkeyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = AddEditHotkeyBinding.inflate(inflater, container, false)

        // Fetch server details (only the first time)
        if (viewModel.hotkey?.value == null &&
            viewModel.mode == AddEditHotkeyViewModel.Mode.EDIT) {
            viewModel.hotkey?.observe(viewLifecycleOwner) { hotkey ->
                debug("LiveData sent update for hotkey $hotkey, eventually updating UI")
                if (hotkey != null) {
                    debug("Fetched hwHotkey is valid")
                    binding.key.setSelection(hotkey.key.name)
                    binding.alt.isChecked = hotkey.alt
                    binding.altgr.isChecked = hotkey.altgr
                    binding.ctrl.isChecked = hotkey.ctrl
                    binding.meta.isChecked = hotkey.meta
                    binding.shift.isChecked = hotkey.shift
                    binding.label.setText(hotkey.displayName())
                } else {
                    warn("Invalid server")
                }
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_delete, menu)
        if (viewModel.mode == AddEditHotkeyViewModel.Mode.ADD) {
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
        debug("Handling save button")

        val hotkey = viewModel.save(
            key = MinimoteKeyType.valueOf(binding.key.selectedItem.toString()),
            alt = binding.alt.isChecked,
            altgr = binding.altgr.isChecked,
            ctrl = binding.ctrl.isChecked,
            meta = binding.meta.isChecked,
            shift = binding.shift.isChecked,
            label = binding.label.text.toString().ifEmpty { null }
        )

        if (viewModel.mode == AddEditHotkeyViewModel.Mode.ADD) {
            Snackbar.make(
                requireParentFragment().requireView(),
                getString(R.string.hw_hotkey_added, hotkey.displayName()),
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            // TODO
        }

        findNavController().navigateUp()
    }

    private fun handleDeleteButton() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.delete_hw_hotkey_confirmation_title)
            .setMessage(R.string.delete_hw_hotkey_confirmation_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                // actually delete
                viewModel.delete()
                Snackbar.make(
                    requireParentFragment().requireView(),
                    getString(R.string.hw_hotkey_removed, viewModel.hotkey?.value?.displayName()),
                    Snackbar.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}