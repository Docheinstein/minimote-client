package org.docheinstein.minimotek.ui.swhotkeys

import android.os.Bundle
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.AddEditSwHotkeyBinding
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.ui.base.SliderView
import org.docheinstein.minimotek.util.*


/**
 * Fragment that handles the addition of a new software hotkey or the editing of an existing one.
 * The mode of the fragment depends on the parameter "swHotkeyId" used to create this fragment.
 * Note that, as opposed to the other AddEdit fragments, this Fragment performs actions in-memory
 * through the shared view model SwHotkeysViewModel, instead of committing them to the DB.
 *
 *  The actions that can be performed on this screen are:
 * - Save the software hotkey [in-memory]
 * - Delete the software hotkey (only in EDIT mode) [in-memory]
 */

@AndroidEntryPoint
class AddEditSwHotkeyFragment : Fragment() {

    /* In order to share a view model between SwHotkeysFragment and AddEditSwHotkeyFragment
     * it is necessary to use hiltNavGraphViewModels (navGraphViewModels with Hilt injection support)
     * for a nested nav graph, in this way the lifetime of the view model is bound to the lifetime
     * of the nav graph on the navigation stack.
     * NOTE: by androidViewModels is not ideal because the lifetime of the view model would be
     * bound to the lifetime of the activity, therefore when reentering this fragment we would
     * still have the old data in the view model. */
    private val sharedViewModel: SwHotkeysViewModel by hiltNavGraphViewModels(R.id.nav_sw_hotkeys)

    // In addition to the shared view model, use a traditional fragment-bound view model too
    private val viewModel: AddEditSwHotkeyViewModel by viewModels()

    private lateinit var binding: AddEditSwHotkeyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        verbose("AddEditSwHotkeyFragment.onCreate()")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        verbose("AddEditSwHotkeyFragment.onCreateView()")
        binding = AddEditSwHotkeyBinding.inflate(inflater, container, false)

        // Fetch details from the shared view model, in-memory (only the first time)
        if (viewModel.mode == AddEditSwHotkeyViewModel.Mode.EDIT &&
                viewModel.swHotkey == null) {
            debug("Fetching details for hotkey ${viewModel.swHotkeyId}")

            val hotkey = sharedViewModel.hotkey(viewModel.swHotkeyId)
            viewModel.swHotkey = hotkey // set the hotkey in the fragment view model
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

        // Update preview when something changes
        binding.key.setOnItemActuallySelectedListener { _, -> updatePreview() }
        binding.alt.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.altgr.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.ctrl.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.meta.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.shift.setOnCheckedChangeListener { _, _ -> updatePreview() }
        binding.label.addAfterTextChangedListener { _ -> updatePreview() }
        binding.textSize.callback = { _ -> updatePreview() }
        binding.horizontalPadding.callback = { updatePreview() }
        binding.verticalPadding.callback = { updatePreview() }

        updatePreview()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (viewModel.mode) {
            AddEditSwHotkeyViewModel.Mode.ADD -> inflater.inflate(R.menu.save_alt, menu)
            AddEditSwHotkeyViewModel.Mode.EDIT -> inflater.inflate(R.menu.save_delete_alt, menu)
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
        verbose("AddEditSwHotkeyFragment.handleSaveAction()")

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

    private fun handleDeleteAction() {
        verbose("AddEditSwHotkeyFragment.handleDeleteAction()")

        // Alert is probably too verbose, since this is an in-memory change

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

        binding.preview.hotkey = uiHotkey
    }
}