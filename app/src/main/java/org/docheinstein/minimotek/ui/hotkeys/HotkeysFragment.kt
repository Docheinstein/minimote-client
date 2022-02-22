package org.docheinstein.minimotek.ui.hotkeys

import android.content.ClipData
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.hotkey.Hotkey
import org.docheinstein.minimotek.databinding.HotkeysBinding
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn
import kotlin.math.roundToInt

@AndroidEntryPoint
class HotkeysFragment : Fragment() {
    private val viewModel: HotkeysViewModel by viewModels()
    private lateinit var binding: HotkeysBinding

    private lateinit var saveButton: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = HotkeysBinding.inflate(inflater, container, false)

        // Observe hw hotkeys list changes
        viewModel.hotkeys.observe(viewLifecycleOwner) { hotkeys ->
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
        inflater.inflate(R.menu.add_save, menu)
        saveButton = menu.findItem(R.id.save_menu_item)
        saveButton.icon.mutate()
//        saveButton.isEnabled = false
//        saveButton.icon.mutate().alpha = 120
        setSaveButtonEnabled(false)
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
//        when(item.itemId) {
//            R.id.edit_menu_item -> {
//                val hwHotkey = adapter.selected()
//                debug("Going to edit hwHotkey at position ${adapter.selection}: ${hwHotkey?.id}")
//                if (hwHotkey != null) {
//                    findNavController().navigate(
//                        HwHotkeysFragmentDirections.actionAddEditHwHotkey(
//                            hwHotkey.id,
//                            getString(R.string.toolbar_title_edit_hw_hotkey)
//                        )
//                    )
//                }
//            }
//            R.id.delete_menu_item -> {
//                val hwHotkey = adapter.selected()
//                debug("Going to delete hwHotkey at position ${adapter.selection}: ${hwHotkey?.id}")
//                if (hwHotkey != null) {
//                    AlertDialog.Builder(requireActivity())
//                        .setTitle(R.string.delete_hw_hotkey_confirmation_title)
//                        .setMessage(R.string.delete_hw_hotkey_confirmation_message)
//                        .setPositiveButton(R.string.ok) { _, _ ->
//                            // actually delete
//                            viewModel.delete(hwHotkey)
//                            Snackbar.make(
//                                requireParentFragment().requireView(),
//                                getString(R.string.hw_hotkey_removed, hwHotkey.button.name),
//                                Snackbar.LENGTH_LONG
//                            ).show()
//
//                            findNavController().navigateUp()
//                        }
//                        .setNegativeButton(R.string.cancel, null)
//                        .show()
//                }
//            }
//        }
        return super.onContextItemSelected(item)
    }

    private fun handleHotkeysUpdate(hotkeys: List<Hotkey>) {
        debug("Hotkey list has been updating, updating UI")

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

                val hotkeyIdStr: String = ev.clipData.getItemAt(0).text.toString()

                debug("Dropped hotkey: $hotkeyIdStr")

                val hotkeyView: HotkeyView? = binding.hotkeys.findViewWithTag(hotkeyIdStr)

                if (hotkeyView == null) {
                    warn("Failed to find hotkey for tag $hotkeyIdStr")
                    return
                }

                val halfWidth = hotkeyView.width / 2
                val halfHeight = hotkeyView.height / 2

                hotkeyView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    val x = (ev.x - halfWidth).toInt()
                    val y = (ev.y - halfHeight).toInt()
                    debug("Updating X = $x, Y = $y")
                    this.leftMargin = x
                    this.topMargin = y
                }

                setSaveButtonEnabled(true)
            }
            else -> {
                warn("Unknown rag event detected")
            }
        }
    }

    private fun makeHotkeyView(hotkey: Hotkey): View {
        val hotkeyIdStr = hotkey.id.toString()

        val hotkeyView = HotkeyView(requireContext(), hotkey = hotkey)
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
                HotkeysFragmentDirections.actionAddEditHotkey(
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

        for (hotkeyView in binding.hotkeys.children) {
            if (hotkeyView !is HotkeyView) {
                warn("Child view is not an hotkey view!?")
                continue
            }

            val lp = hotkeyView.layoutParams as FrameLayout.LayoutParams
            val id = (hotkeyView.tag as String).toLong()
            val x = lp.leftMargin
            val y = lp.topMargin

            debug("Updating hotkey $id to position ($x,$y)")
            viewModel.updatePosition(id, x, y)
        }

        findNavController().navigateUp()
    }

    private fun handleAddHotkeyButton() {
        findNavController().navigate(
            HotkeysFragmentDirections.actionAddEditHotkey(
                AddEditHotkeyViewModel.HOTKEY_ID_NONE,
                getString(R.string.toolbar_title_add_hotkey)
            )
        )
    }

    private fun setSaveButtonEnabled(enabled: Boolean) {
        if (enabled) {
            saveButton.isEnabled = true
            saveButton.icon.alpha = 255
        } else {
            saveButton.isEnabled = false
            saveButton.icon.alpha = 127
        }
    }
}