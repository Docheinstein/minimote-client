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
//                    binding.button.setSelection(hwHotkey.button.name)
//                    binding.alt.isChecked = hwHotkey.alt
//                    binding.altgr.isChecked = hwHotkey.altgr
//                    binding.ctrl.isChecked = hwHotkey.ctrl
//                    binding.meta.isChecked = hwHotkey.meta
//                    binding.shift.isChecked = hwHotkey.shift
//                    binding.key.setSelection(hwHotkey.key.name)
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
//
//        val hwHotkey = HwHotkey(
//            id = if (viewModel.mode == AddEditHwHotkeyViewModel.Mode.EDIT) viewModel.hwHotkey?.value!!.id else AUTO_ID,
//            button = ButtonType.valueOf(binding.button.selectedItem.toString()),
//            alt = binding.alt.isChecked,
//            altgr = binding.altgr.isChecked,
//            ctrl = binding.ctrl.isChecked,
//            meta = binding.meta.isChecked,
//            shift = binding.shift.isChecked,
//            key = MinimoteKeyType.valueOf(binding.key.selectedItem.toString())
//        )
//
//        debug("Saving hwHotkey = $hwHotkey")
//
//        if (viewModel.mode == AddEditHwHotkeyViewModel.Mode.ADD) {
//            viewModel.insert(hwHotkey)
//            Snackbar.make(
//                requireParentFragment().requireView(),
//                getString(R.string.hw_hotkey_added, hwHotkey.button.name),
//                Snackbar.LENGTH_LONG
//            ).show()
//        } else {
//            viewModel.update(hwHotkey)
//        }

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
                    getString(R.string.hw_hotkey_removed, viewModel.hotkey?.value?.button?.name),
                    Snackbar.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}