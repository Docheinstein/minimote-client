package org.docheinstein.minimotek.ui.swhotkeys

import android.app.AlertDialog
import android.content.ClipData
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey
import org.docheinstein.minimotek.databinding.HotkeysBinding
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn

@AndroidEntryPoint
class SwHotkeysFragment : Fragment() {
//    private val viewModel: SwHotkeysViewModel by viewModels()
    private val viewModel: SwHotkeysViewModel by hiltNavGraphViewModels(R.id.nav_sw_hotkeys)
    private lateinit var binding: HotkeysBinding

    private lateinit var clearButton: MenuItem
    private lateinit var saveButton: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        debug("SwHotkeysFragment.onCreate")
        setHasOptionsMenu(true)
    }


    override fun onResume() {
        debug("SwHotkeysFragment.onResume")
        super.onResume()
    }

    override fun onPause() {
        debug("SwHotkeysFragment.onPause")
        super.onPause()
    }

    override fun onStop() {
        debug("SwHotkeysFragment.onStop")
        super.onStop()
    }

    override fun onStart() {
        debug("SwHotkeysFragment.onStart")
        super.onStart()
    }

    override fun onDestroy() {
        debug("SwHotkeysFragment.onDestroy")
        super.onDestroy()
    }

    override fun onDestroyView() {
        debug("SwHotkeysFragment.onDestroyView")
        super.onDestroyView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        debug("SwHotkeysFragment.onCreateView")

        binding = HotkeysBinding.inflate(inflater, container, false)

        // Observe hw hotkeys list changes
        // TODO: prevent reloading/rotation change
        debug("Observing swHotkeys updates")
        viewModel.swHotkeys.observe(viewLifecycleOwner) { hotkeys ->
            debug("Received hotkeys update")
            setMenuItemEnabled(clearButton, hotkeys.isNotEmpty())
            if (hotkeys != null) {
                handleHotkeysUpdate(hotkeys)
            }
        }

        binding.hotkeys.setOnDragListener { v, e ->
            handleHotkeyDragged(v, e)
            true
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.clear_add_save, menu)
        clearButton = menu.findItem(R.id.clear_menu_item)
        saveButton = menu.findItem(R.id.save_menu_item)

        clearButton.icon.mutate()
        saveButton.icon.mutate()

        viewModel.hasPendingChanges.observe(viewLifecycleOwner) { yes ->
            debug("Pending changes = $yes")
            setMenuItemEnabled(saveButton, yes)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_menu_item -> {
                handleAddHotkeyButton()
                return true
            }
            R.id.save_menu_item -> {
                handleSaveHotkeysButton()
            }
            R.id.clear_menu_item -> {
                handleClearButton()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun handleHotkeysUpdate(hotkeys: List<SwHotkey>) {
        debug("Hotkey list updated, updating UI")
        // TODO: update only changed views

        binding.hotkeys.removeAllViews()

        for (h in hotkeys) {
            val hotkeyView = makeHotkeyView(h)
            binding.hotkeys.addView(hotkeyView)
        }
    }


    private fun handleHotkeyDragged(v: View, ev: DragEvent) {
        when (ev.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                debug("Drag event detected: ACTION_DRAG_STARTED")
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                debug("Drag event detected: ACTION_DRAG_ENTERED")
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                debug("Drag event detected: ACTION_DRAG_EXITED")
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                debug("Drag event detected: ACTION_DRAG_LOCATION")
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                debug("Drag event detected: ACTION_DRAG_ENDED")
            }
            DragEvent.ACTION_DROP -> {
                debug("Drag event detected: ACTION_DROP")
                if (ev.clipData.itemCount < 1) {
                    warn("Invalid event data")
                    return
                }

                // TODO: update viewModel

                val hotkeyIdStr: String = ev.clipData.getItemAt(0).text.toString()

                debug("Dropped hotkey: $hotkeyIdStr")

                val hotkeyView: SwHotkeyView? = binding.hotkeys.findViewWithTag(hotkeyIdStr)

                if (hotkeyView == null) {
                    warn("Failed to find hotkey for tag $hotkeyIdStr")
                    return
                }

                val halfWidth = hotkeyView.width / 2
                val halfHeight = hotkeyView.height / 2

                val x = (ev.x - halfWidth).toInt()
                val y = (ev.y - halfHeight).toInt()

                viewModel.updatePosition(hotkeyIdStr.toLong(), x, y)

//                hotkeyView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                    val x = (ev.x - halfWidth).toInt()
//                    val y = (ev.y - halfHeight).toInt()
//                    debug("Updating X = $x, Y = $y")
//                    this.leftMargin = x
//                    this.topMargin = y
//                }
            }
            else -> {
                warn("Unknown rag event detected")
            }
        }
    }

    private fun makeHotkeyView(hotkey: SwHotkey): View {
        val hotkeyIdStr = hotkey.id.toString()

        val hotkeyView = SwHotkeyView(requireContext(), hotkey = hotkey)
        hotkeyView.tag = hotkeyIdStr

        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        lp.leftMargin = hotkey.x
        lp.topMargin = hotkey.y
        hotkeyView.layoutParams = lp

        hotkeyView.setOnClickListener {
            findNavController().navigate(
                SwHotkeysFragmentDirections.actionAddEditSwHotkey(
                    hotkey.id,
                    getString(R.string.toolbar_title_edit_hotkey)
                )
            )
        }

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

    private fun handleSaveHotkeysButton() {
        debug("Clicked on save, saving ${binding.hotkeys.childCount} hotkeys")

        viewModel.commit()
//        for (hotkeyView in binding.hotkeys.children) {
//            if (hotkeyView !is SwHotkeyView) {
//                warn("Child view is not an hotkey view!?")
//                continue
//            }
//
//            val lp = hotkeyView.layoutParams as FrameLayout.LayoutParams
//            val id = (hotkeyView.tag as String).toLong()
//            val x = lp.leftMargin
//            val y = lp.topMargin
//
//            debug("Updating hotkey $id to position ($x,$y)")
//            viewModel.updatePosition(id, x, y)
//        }

        findNavController().navigateUp()
    }

    private fun handleAddHotkeyButton() {
        findNavController().navigate(
            SwHotkeysFragmentDirections.actionAddEditSwHotkey(
                SwHotkeysViewModel.HOTKEY_ID_NONE,
                getString(R.string.toolbar_title_add_hotkey)
            )
        )
    }

    private fun handleClearButton() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.sw_hotkeys_clear_confirmation_title)
            .setMessage(R.string.sw_hotkeys_clear_confirmation_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                // actually delete
                viewModel.clear()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setMenuItemEnabled(menuItem: MenuItem, enabled: Boolean) {
        if (enabled) {
            menuItem.isEnabled = true
            menuItem.icon.alpha = 255
        } else {
            menuItem.isEnabled = false
            menuItem.icon.alpha = 127
        }
    }


}