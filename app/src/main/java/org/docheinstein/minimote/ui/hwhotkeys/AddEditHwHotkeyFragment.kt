package org.docheinstein.minimote.ui.hwhotkeys

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimote.R
import org.docheinstein.minimote.databinding.AddEditHwHotkeyBinding
import org.docheinstein.minimote.buttons.ButtonType
import org.docheinstein.minimote.keys.MinimoteKeyType
import org.docheinstein.minimote.util.*

/**
 * Fragment that handles the addition of a new hardware hotkey or the editing of an existing one.
 * The mode of the fragment depends on the parameter "hwHotkeyId" used to create this fragment.
 *
 *  The actions that can be performed on this screen are:
 * - Save the hardware hotkey
 * - Delete the hardware hotkey (only in EDIT mode)
 */

@AndroidEntryPoint
class AddEditHwHotkeyFragment : Fragment() {

    private val viewModel: AddEditHwHotkeyViewModel by viewModels()
    private lateinit var binding: AddEditHwHotkeyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        verbose("AddEditHwHotkeyFragment.onCreate()")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        verbose("AddEditHwHotkeyFragment.onCreateView()")
        binding = AddEditHwHotkeyBinding.inflate(inflater, container, false)

        // Fetch hw hotkey (only in EDIT mode and only the first time)
        if (viewModel.mode == AddEditHwHotkeyViewModel.Mode.EDIT && !viewModel.fetched) {
            viewModel.hwHotkey?.observe(viewLifecycleOwner) { hwHotkey ->
                debug("Hardware hotkey update received in UI: $hwHotkey")

                if (hwHotkey == null) {
                    error("Invalid hw hotkey")
                    return@observe
                }

                if (viewModel.fetched) {
                    warn("Already fetched, ignoring update")
                    return@observe
                }

                viewModel.fetched = true

                binding.button.setSelection(hwHotkey.button.keyString)
                binding.alt.isChecked = hwHotkey.alt
                binding.altgr.isChecked = hwHotkey.altgr
                binding.ctrl.isChecked = hwHotkey.ctrl
                binding.meta.isChecked = hwHotkey.meta
                binding.shift.isChecked = hwHotkey.shift
                binding.key.setSelection(hwHotkey.key.keyString)
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (viewModel.mode) {
            AddEditHwHotkeyViewModel.Mode.ADD -> inflater.inflate(R.menu.save, menu)
            AddEditHwHotkeyViewModel.Mode.EDIT -> inflater.inflate(R.menu.save_delete, menu)
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
        verbose("AddEditHwHotkeyFragment.handleSaveAction()")

        val key = MinimoteKeyType.byKeyString(binding.key.selectedItem.toString())
        if (key == null) {
            error("Invalid key for keyString ${binding.key.selectedItem}")
            return
        }

        val button = ButtonType.byKeyString(binding.button.selectedItem.toString())
        if (button == null) {
            error("Invalid button for keyString ${binding.button.selectedItem}")
            return
        }

        val hwHotkey = viewModel.save(
            button = button,
            alt = binding.alt.isChecked,
            altgr = binding.altgr.isChecked,
            ctrl = binding.ctrl.isChecked,
            meta = binding.meta.isChecked,
            shift = binding.shift.isChecked,
            key = key
        )

        Snackbar.make(
            requireParentFragment().requireView(),
            getString(R.string.saved, hwHotkey.button.keyString),
            Snackbar.LENGTH_LONG
        ).show()

        findNavController().navigateUp()
    }

    private fun handleDeleteAction() {
        verbose("AddEditHwHotkeyFragment.handleDeleteAction()")

        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.delete_hw_hotkey_confirmation_title)
            .setMessage(getString(R.string.delete_hw_hotkey_confirmation_message, viewModel.hwHotkey?.value?.button?.name ?: ""))
            .setPositiveButton(R.string.ok) { _, _ ->
                // Actually delete
                viewModel.delete()
                Snackbar.make(
                    requireParentFragment().requireView(),
                    getString(R.string.removed, viewModel.hwHotkey?.value?.button?.name ?: ""),
                    Snackbar.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}