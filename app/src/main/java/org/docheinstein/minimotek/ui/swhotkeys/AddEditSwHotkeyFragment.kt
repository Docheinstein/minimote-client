package org.docheinstein.minimotek.ui.swhotkeys

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.AddEditHotkeyBinding
import org.docheinstein.minimotek.extensions.setSelection
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn


@AndroidEntryPoint
class AddEditSwHotkeyFragment : Fragment() {

    private val viewModel: AddEditSwHotkeyViewModel by viewModels()
    private val sharedViewModel: SwHotkeysSharedViewModel by viewModels({requireParentFragment()})
//    private val sharedViewModel: SwHotkeysSharedViewModel by activityViewModels()
    private lateinit var binding: AddEditHotkeyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = AddEditHotkeyBinding.inflate(inflater, container, false)
//
//        // Fetch server details (only the first time)
//        if (viewModel.swHotkey?.value == null &&
//            viewModel.mode == AddEditHotkeyViewModel.Mode.EDIT
//        ) {
//            viewModel.swHotkey?.observe(viewLifecycleOwner) { swHotkey ->
//                debug("LiveData sent update for hotkey $swHotkey, eventually updating UI")
//                if (swHotkey != null) {
//                    debug("Fetched hwHotkey is valid")
//                    binding.key.setSelection(swHotkey.key.name)
//                    binding.alt.isChecked = swHotkey.alt
//                    binding.altgr.isChecked = swHotkey.altgr
//                    binding.ctrl.isChecked = swHotkey.ctrl
//                    binding.meta.isChecked = swHotkey.meta
//                    binding.shift.isChecked = swHotkey.shift
//                    binding.label.setText(swHotkey.displayName())
//                } else {
//                    warn("Invalid server")
//                }
//            }
//        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_delete, menu)
        if (viewModel.mode == AddEditSwHotkeyViewModel.Mode.ADD) {
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

        val key = MinimoteKeyType.byKeyString(binding.key.selectedItem.toString())
        if (key == null) {
            warn("Invalid key for keyString ${binding.key.selectedItem}")
            return
        }

        if (viewModel.mode == AddEditSwHotkeyViewModel.Mode.ADD) {
            sharedViewModel.add(
                key = key,
                alt = binding.alt.isChecked,
                altgr = binding.altgr.isChecked,
                ctrl = binding.ctrl.isChecked,
                meta = binding.meta.isChecked,
                shift = binding.shift.isChecked,
                label = binding.label.text.toString().ifEmpty { null }
            )
        } else if (viewModel.mode == AddEditSwHotkeyViewModel.Mode.EDIT) {
            sharedViewModel.edit(
                id = viewModel.swHotkey!!.value!!.id,
                key = key,
                alt = binding.alt.isChecked,
                altgr = binding.altgr.isChecked,
                ctrl = binding.ctrl.isChecked,
                meta = binding.meta.isChecked,
                shift = binding.shift.isChecked,
                label = binding.label.text.toString().ifEmpty { null }
            )
        }


//        if (viewModel.mode == AddEditHotkeyViewModel.Mode.ADD) {
//            Snackbar.make(
//                requireParentFragment().requireView(),
//                getString(R.string.hw_hotkey_added, hotkey.displayName()),
//                Snackbar.LENGTH_LONG
//            ).show()
//        } else {
//            // TODO
//        }

        findNavController().navigateUp()
    }

    private fun handleDeleteButton() {
//        AlertDialog.Builder(requireActivity())
//            .setTitle(R.string.delete_hw_hotkey_confirmation_title)
//            .setMessage(R.string.delete_hw_hotkey_confirmation_message)
//            .setPositiveButton(R.string.ok) { _, _ ->
//                // actually delete
//                viewModel.delete()
//                Snackbar.make(
//                    requireParentFragment().requireView(),
//                    getString(R.string.hw_hotkey_removed, viewModel.swHotkey?.value?.displayName()),
//                    Snackbar.LENGTH_LONG
//                ).show()
//                findNavController().navigateUp()
//            }
//            .setNegativeButton(R.string.cancel, null)
//            .show()
    }
}