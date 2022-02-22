package org.docheinstein.minimotek.ui.hotkeys

import android.content.ClipData
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.database.hotkey.Hotkey
import org.docheinstein.minimotek.databinding.HotkeysBinding
import org.docheinstein.minimotek.ui.hotkey.AddEditHotkeyViewModel
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn

@AndroidEntryPoint
class HotkeysFragment : Fragment() {
    private val viewModel: HotkeysViewModel by viewModels()
    private lateinit var binding: HotkeysBinding


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
        inflater.inflate(R.menu.add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_menu_item -> {
                handleAddHotkeyButton()
                return true
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

                val hotkeyView: TextView? = binding.hotkeys.findViewWithTag(hotkeyIdStr)

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

            }
            else -> {
                warn("Unknown rag event detected")
            }
        }
    }

    private fun makeHotkeyView(hotkey: Hotkey): View {
        val hotkeyIdStr = hotkey.id.toString()

        val hotkeyView = TextView(context)
        hotkeyView.tag = hotkeyIdStr
        hotkeyView.text = hotkey.displayName()
        hotkeyView.textSize = 24f

        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        lp.leftMargin = 50
        lp.topMargin = 50
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
                null, 0
            )

            true
        }

        return hotkeyView
    }

    private fun handleAddHotkeyButton() {
        findNavController().navigate(
            HotkeysFragmentDirections.actionAddEditHotkey(
                AddEditHotkeyViewModel.HOTKEY_ID_NONE,
                getString(R.string.toolbar_title_add_hotkey)
            )
        )
    }
}