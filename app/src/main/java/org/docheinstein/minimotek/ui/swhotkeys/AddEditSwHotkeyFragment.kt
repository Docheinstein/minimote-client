package org.docheinstein.minimotek.ui.swhotkeys

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey
import org.docheinstein.minimotek.databinding.AddEditSwHotkeyBinding
import org.docheinstein.minimotek.extensions.addAfterTextChangedListener
import org.docheinstein.minimotek.extensions.setOnItemActuallySelectedListener
import org.docheinstein.minimotek.extensions.setOnSeekbarProgressListener
import org.docheinstein.minimotek.extensions.setSelection
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn


private const val SIZE_SLIDER_FACTOR = 2

@AndroidEntryPoint
class AddEditSwHotkeyFragment : Fragment() {

    private val viewModel: AddEditSwHotkeyViewModel by viewModels()
    private val sharedViewModel: SwHotkeysViewModel by navGraphViewModels(R.id.nav_sw_hotkeys)
//    private val sharedViewModel: SwHotkeysSharedViewModel by activityViewModels()
    private lateinit var binding: AddEditSwHotkeyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = AddEditSwHotkeyBinding.inflate(inflater, container, false)

        debug("AddEditSwHotkeyFragment.onCreateView()")

        // Fetch details (only the first time)
        if (viewModel.swHotkey == null &&
            viewModel.mode == AddEditSwHotkeyViewModel.Mode.EDIT) {
            debug("Fetching details for hotkey ${viewModel.swHotkeyId}")

            val hotkey = sharedViewModel.hotkey(viewModel.swHotkeyId)
            viewModel.swHotkey = hotkey
            if (hotkey != null) {
                binding.key.setSelection(viewModel.swHotkey!!.key.keyString)
                binding.alt.isChecked = hotkey.alt
                binding.altgr.isChecked = hotkey.altgr
                binding.ctrl.isChecked = hotkey.ctrl
                binding.meta.isChecked = hotkey.meta
                binding.shift.isChecked = hotkey.shift
                binding.textSize.progressScaled = hotkey.textSize
                binding.horizontalPadding.progressScaled = hotkey.horizontalPadding
                binding.verticalPadding.progressScaled = hotkey.verticalPadding
                if (hotkey.label != null)
                    binding.label.setText(hotkey.label)
            } else {
                warn("No hotkey with id ${viewModel.swHotkeyId} in EDIT mode")
            }
        }

        // Update preview on change
        binding.key.setOnItemActuallySelectedListener { _, -> updatePreview() }
        binding.alt.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.altgr.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.ctrl.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.meta.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.shift.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.label.addAfterTextChangedListener { _ -> updatePreview() }
        binding.textSize.setOnProgressListener { updatePreview() }
        binding.horizontalPadding.setOnProgressListener { updatePreview() }
        binding.verticalPadding.setOnProgressListener { updatePreview() }

        updatePreview()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_delete_alt, menu)
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
                label = binding.label.text.toString().ifEmpty { null },
                textSize = binding.textSize.progressScaled,
                horizontalPadding = binding.horizontalPadding.progressScaled,
                verticalPadding = binding.verticalPadding.progressScaled
            )
        } else if (viewModel.mode == AddEditSwHotkeyViewModel.Mode.EDIT) {
            sharedViewModel.edit(
                id = viewModel.swHotkeyId,
                key = key,
                alt = binding.alt.isChecked,
                altgr = binding.altgr.isChecked,
                ctrl = binding.ctrl.isChecked,
                meta = binding.meta.isChecked,
                shift = binding.shift.isChecked,
                label = binding.label.text.toString().ifEmpty { null },
                textSize = binding.textSize.progressScaled,
                horizontalPadding = binding.horizontalPadding.progressScaled,
                verticalPadding = binding.verticalPadding.progressScaled
            )
        }

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
        sharedViewModel.remove(viewModel.swHotkeyId)
        findNavController().navigateUp()
    }

    private fun updatePreview() {
        val key = MinimoteKeyType.byKeyString(binding.key.selectedItem.toString())
        if (key == null) {
            warn("Invalid key for keyString ${binding.key.selectedItem}")
            return
        }

        val uiHotkey = SwHotkeyView.Hotkey(
            key = key,
            alt = binding.alt.isChecked,
            altgr = binding.altgr.isChecked,
            ctrl = binding.ctrl.isChecked,
            meta = binding.meta.isChecked,
            shift = binding.shift.isChecked,
            label = binding.label.text.toString().ifEmpty { null },
            textSize = binding.textSize.progressScaled,
            horizontalPadding = binding.horizontalPadding.progressScaled,
            verticalPadding = binding.verticalPadding.progressScaled
        )

        debug("Updating hotkey preview")

        binding.preview.set(uiHotkey)
    }
}