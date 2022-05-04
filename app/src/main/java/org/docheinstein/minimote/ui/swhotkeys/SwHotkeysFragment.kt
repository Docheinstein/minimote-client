package org.docheinstein.minimote.ui.swhotkeys

import android.app.AlertDialog
import android.content.ClipData
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimote.orientation.Orientation
import org.docheinstein.minimote.R
import org.docheinstein.minimote.database.hotkey.sw.SwHotkey
import org.docheinstein.minimote.databinding.HotkeysBinding
import org.docheinstein.minimote.util.debug
import org.docheinstein.minimote.util.verbose
import org.docheinstein.minimote.util.warn

/**
 * Fragment representing the software hotkeys
 * (hotkeys triggered with the pressure of graphical buttons).
 * The actions that can be performed on this screen are:
 * - Drag & Drop software hotkey around the screen, as desired [in-memory]
 * - Add a software hotkey (opens AddEdit screen) [in-memory]
 * - Edit a software hotkey (opens AddEdit screen) [in-memory]
 * - Clear all the software hotkeys [in-memory]
 * - Import the hotkeys from hotkeys of the opposite orientation [in-memory]
 * - Save all the hotkeys [to the db]
 *
 * This screen has several differences from the other fragments.
 * First of all, the hotkeys can be customized independently for each orientation,
 * therefore the Portrait hotkeys could be different from the Landscape hotkeys.
 * Furthermore, when the hotkeys are added/edited/deleted, the changes are not committed
 * instantly to the DB, instead all the changes are transitory and are committed to the
 * DB only when the user saves the changes with the appropriate save button
 * (this lets the user to eventually discard changes).
 */
@AndroidEntryPoint
class SwHotkeysFragment : Fragment() {

    /* In order to share a view model between SwHotkeysFragment and AddEditSwHotkeyFragment
     * it is necessary to use hiltNavGraphViewModels (navGraphViewModels with Hilt injection support)
     * for a nested nav graph, in this way the lifetime of the view model is bound to the lifetime
     * of the nav graph on the navigation stack.
     * NOTE: by androidViewModels is not ideal because the lifetime of the view model would be
     * bound to the lifetime of the activity, therefore when reentering this fragment we would
     * still have the old data in the view model. */
    private val viewModel: SwHotkeysViewModel by hiltNavGraphViewModels(R.id.nav_sw_hotkeys)
    private lateinit var binding: HotkeysBinding

    private lateinit var clearButton: MenuItem
    private lateinit var saveButton: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        verbose("SwHotkeysFragment.onCreate()")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        verbose("SwHotkeysFragment.onCreateView()")

        binding = HotkeysBinding.inflate(inflater, container, false)

        // Drag listener: move the hotkey around the screen
        binding.hotkeys.setOnDragListener { _, e ->
            handleHotkeyDrag(e)
            true
        }

        // Observe orientation change
        viewModel.orientation.observe(viewLifecycleOwner) { orientation ->
            debug("UI notified about orientation change, new orientation is $orientation")
            binding.orientation.setText(if (orientation == Orientation.Landscape) R.string.landscape else R.string.portrait)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        verbose("SwHotkeysFragment.onCreateOptionsMenu()")

        inflater.inflate(R.menu.hotkeys, menu)

        // Keep a reference to the clear and the save buttons in order
        // to enable/disable them programmatically as needed
        clearButton = menu.findItem(R.id.clear_menu_item)
        saveButton = menu.findItem(R.id.save_menu_item)

        // Do not share the icons with other fragments
        // (otherwise other fragments sharing these icons ends up with
        // an icon with the same enabled/disabled state of the icons in
        // this fragment, which doesn't make sense).
        clearButton.icon.mutate()
        saveButton.icon.mutate()

        // Observe pending changes
        viewModel.hasPendingChanges().observe(viewLifecycleOwner) { yes ->
            debug("Pending changes = $yes")

            // Enable the save button if there are pending changes to the hotkeys
            setMenuItemEnabled(saveButton, yes)
        }

        // Observe hotkeys changes
        viewModel.hotkeys().observe(viewLifecycleOwner) { hotkeys ->
            debug("Software hotkeys update received in UI (size = ${hotkeys.size})")
            // Enable the clear button if there are actually hotkeys
            setMenuItemEnabled(clearButton, hotkeys.isNotEmpty())

            if (hotkeys != null)
                handleHotkeysUpdate(hotkeys)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.import_menu_item -> {
                handleImportAction()
                return true
            }
            R.id.add_menu_item -> {
                handleAddAction()
                return true
            }
            R.id.save_menu_item -> {
                handleSaveAction()
                return true
            }
            R.id.clear_menu_item -> {
                handleClearAction()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun handleHotkeysUpdate(hotkeys: List<SwHotkey>) {
        verbose("SwHotkeysFragment.handleHotkeysUpdate(size = ${hotkeys.size})")

        // TODO: update only the changed views, instead of invalidate everything

        binding.hotkeys.removeAllViews()

        for (h in hotkeys) {
            val hotkeyView = makeHotkeyView(h)
            binding.hotkeys.addView(hotkeyView)
        }
    }

    private fun handleHotkeyDrag(ev: DragEvent) {
        verbose("SwHotkeysFragment.handleHotkeyDragged()")

        when (ev.action) {
            DragEvent.ACTION_DRAG_STARTED -> debug("ACTION_DRAG_STARTED")
            DragEvent.ACTION_DRAG_ENTERED -> debug("ACTION_DRAG_ENTERED")
            DragEvent.ACTION_DRAG_EXITED -> debug("ACTION_DRAG_EXITED")
            DragEvent.ACTION_DRAG_LOCATION -> debug("ACTION_DRAG_LOCATION")
            DragEvent.ACTION_DRAG_ENDED -> debug("ACTION_DRAG_ENDED")
            DragEvent.ACTION_DROP -> {
                debug("ACTION_DROP")

                /* An hotkeys has been dropped in the container, figure out
                 * which hotkey has been dropped and update the view model accordingly.
                 * Note that instead of updating the UI directly, we update the
                 * view model which dispatches the change back to us through LiveData,
                 * in this way the changes will survive orientation changes. */
                if (ev.clipData.itemCount < 1) {
                    warn("Invalid event data")
                    return
                }

                val hotkeyIdStr: String = ev.clipData.getItemAt(0).text.toString()

                debug("Dropped hotkey id: $hotkeyIdStr")

                val hotkeyView: SwHotkeyView? = binding.hotkeys.findViewWithTag(hotkeyIdStr)

                if (hotkeyView == null) {
                    warn("Failed to find hotkey for tag $hotkeyIdStr")
                    return
                }

                // Update the position of the hotkey in the view model

                val halfWidth = hotkeyView.width / 2
                val halfHeight = hotkeyView.height / 2

                val x = (ev.x - halfWidth).toInt()
                val y = (ev.y - halfHeight).toInt()

                viewModel.updatePosition(hotkeyIdStr.toLong(), x, y)
            }
        }
    }

    private fun handleSaveAction() {
        verbose("SwHotkeysFragment.handleSaveAction()")

        // Commit the in-memory changes to DB (for current orientation)
        viewModel.commit()

        Snackbar.make(
            requireParentFragment().requireView(),
            getString(R.string.sw_hotkeys_saved, viewModel.orientationSnapshot.name),
            Snackbar.LENGTH_LONG
        ).show()

        // Do not navigate up!,
        // otherwise the hotkeys for the other orientation are lost
    }

    private fun handleImportAction() {
        verbose("SwHotkeysFragment.handleImportAction()")

        val currentOrientation = viewModel.orientationSnapshot
        val otherOrientation = !currentOrientation

        debug("Proposing to import hotkeys from $currentOrientation to $otherOrientation")

        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.sw_hotkeys_import_confirmation_title)
            .setMessage(getString(R.string.sw_hotkeys_import_confirmation_message, currentOrientation, otherOrientation))
            .setPositiveButton(R.string.ok) { _, _ ->
                // Actually import the hotkeys from the other orientation
                viewModel.import(binding.hotkeys.width, binding.hotkeys.height)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleAddAction() {
        verbose("SwHotkeysFragment.handleAddAction()")

        findNavController().navigate(
            SwHotkeysFragmentDirections.actionAddEditSwHotkey(
                AddEditSwHotkeyViewModel.HOTKEY_ID_NONE,
                getString(R.string.toolbar_title_add_sw_hotkey)
            )
        )
    }

    private fun handleClearAction() {
        verbose("SwHotkeysFragment.handleClearAction()")

        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.sw_hotkeys_clear_confirmation_title)
            .setMessage(getString(R.string.sw_hotkeys_clear_confirmation_message, viewModel.orientationSnapshot))
            .setPositiveButton(R.string.ok) { _, _ ->
                // Actually delete all the hotkeys for the current orientation
                viewModel.clear()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }


    private fun makeHotkeyView(hotkey: SwHotkey): View {
        // Build a SwHotkeyView for the given hotkey

        val hotkeyIdStr = hotkey.id.toString()

        val hotkeyView = SwHotkeyView(requireContext(), hotkey = SwHotkeyView.Hotkey.fromSwHotkey(hotkey))

        // Set the tag equals to the hotkey is, so that this can be retrieved with findViewWithTag
        hotkeyView.tag = hotkeyIdStr

        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        lp.leftMargin = hotkey.x
        lp.topMargin = hotkey.y
        hotkeyView.layoutParams = lp

        // Click listener: open AddEditSwHotkey
        hotkeyView.setOnClickListener {
            debug("Single click on hotkey $hotkey")
            findNavController().navigate(
                SwHotkeysFragmentDirections.actionAddEditSwHotkey(
                    hotkey.id,
                    getString(R.string.toolbar_title_edit_sw_hotkey)
                )
            )
        }

        // Long click listener: start drag and drop
        hotkeyView.setOnLongClickListener {
            debug("Long click on hotkey $hotkey")
            it.startDragAndDrop(
                ClipData.newPlainText(hotkeyIdStr, hotkeyIdStr),
                View.DragShadowBuilder(hotkeyView),
                hotkey, 0
            )
            true
        }

        return hotkeyView
    }

    private fun setMenuItemEnabled(menuItem: MenuItem, enabled: Boolean) {
        menuItem.isEnabled = enabled
        menuItem.icon.alpha = if (enabled) 255 else 127
    }
}