package org.docheinstein.minimotek.ui.hwhotkeys

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.database.hotkey.hw.HwHotkey
import org.docheinstein.minimotek.databinding.*
import org.docheinstein.minimotek.ui.base.SelectableListAdapter
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.verbose
import org.docheinstein.minimotek.util.warn

/**
 * Fragment representing the hardware hotkey list
 * (hotkeys mapped to physical buttons, e.g. VolumeUp/VolumeDown).
 * The actions that can be performed on this screen are:
 * - Add a hardware hotkey (opens AddEdit screen)
 * - Edit a hardware hotkey (opens AddEdit screen)
 * - Delete a hardware hotkey
 */

@AndroidEntryPoint
class HwHotkeysFragment : Fragment() {

    private val viewModel: HwHotkeysViewModel by viewModels()
    private lateinit var binding: HwHotkeyListBinding
    private lateinit var adapter: HwHotkeyListAdapter

    private class HwHotkeyDiffCallback : DiffUtil.ItemCallback<HwHotkey>() {
        override fun areItemsTheSame(oldItem: HwHotkey, newItem: HwHotkey): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HwHotkey, newItem: HwHotkey): Boolean {
             // UI based equality
            return oldItem.button == newItem.button
        }
    }

    class HwHotkeyListAdapter : SelectableListAdapter<HwHotkey, HwHotkeyListAdapter.ViewHolder>(HwHotkeyDiffCallback()) {
        class ViewHolder(val binding: HwHotkeyListItemBinding) :
                RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

            init {
                binding.root.setOnCreateContextMenuListener(this)
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                view: View?,
                context_info: ContextMenu.ContextMenuInfo?
            ) {
                if (view != null && menu != null) {
                    val menuInflater = MenuInflater(view.context)
                    menuInflater.inflate(R.menu.edit_delete, menu)
                    menu.setHeaderTitle(view.context.getString(R.string.choose_an_action))
                }
            }
        }

        override fun doCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                HwHotkeyListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false),

            )
        }

        override fun doBindViewHolder(holder: ViewHolder, position: Int) {
            val hwHotkey = getItem(position)

            // Text
            holder.binding.label.text = hwHotkey.button.name

            // Icon
            val icon = when (hwHotkey.button) {
                ButtonType.VolumeDown -> { R.drawable.volume_down }
                ButtonType.VolumeUp -> { R.drawable.volume_up }
            }

            holder.binding.icon.setImageResource(icon)

            // Click listener: open AddEditHwhotkey
            holder.binding.root.setOnClickListener {
                debug("Click on hwHotkey $hwHotkey")
                 holder.itemView.findNavController().navigate(
                    HwHotkeysFragmentDirections.actionAddEditHwHotkey(
                        hwHotkey.id,
                        holder.itemView.context.getString(R.string.toolbar_title_edit_hw_hotkey)
                ))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        verbose("HwHotkeysFragment.onCreate()")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        verbose("HwHotkeysFragment.onCreateView()")

        binding = HwHotkeyListBinding.inflate(inflater, container, false)

        // Hardware hotkeys adapter
        adapter = HwHotkeyListAdapter()
        binding.hwHotkeyList.adapter = adapter

        // Observe hardware hotkeys changes
        viewModel.hwHotkeys.observe(viewLifecycleOwner) { hwHotkeys ->
            debug("Hardware hotkeys update received in UI (size = ${hwHotkeys.size})")
            adapter.submitList(hwHotkeys)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_menu_item -> {
                handleAddAction()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_menu_item -> {
                handleEditAction()
                return true
            }
            R.id.delete_menu_item -> {
                handleDeleteAction()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun handleAddAction() {
        verbose("HwHotkeysFragment.handleAddAction()")

        findNavController().navigate(
            HwHotkeysFragmentDirections.actionAddEditHwHotkey(
                AddEditHwHotkeyViewModel.HW_HOTKEY_ID_NONE,
                getString(R.string.toolbar_title_add_hw_hotkey)
            )
        )
    }

    private fun handleEditAction() {
        verbose("HwHotkeysFragment.handleEditAction()")

        val hwHotkey = adapter.selectedItem
        debug("Going to edit hardware hotkey at position ${adapter.selectedPosition}: ${hwHotkey?.id}")
        if (hwHotkey != null) {
            findNavController().navigate(
                HwHotkeysFragmentDirections.actionAddEditHwHotkey(
                    hwHotkey.id,
                    getString(R.string.toolbar_title_edit_hw_hotkey)
                )
            )
        } else {
            warn("No hotkey selected, cannot edit")
        }
    }

    private fun handleDeleteAction() {
        verbose("HwHotkeysFragment.handleDeleteAction()")

        val hwHotkey = adapter.selectedItem
        debug("Going to delete hardware hotkey at position ${adapter.selectedPosition}: ${hwHotkey?.id}")
        if (hwHotkey != null) {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.delete_hw_hotkey_confirmation_title)
                .setMessage(getString(R.string.delete_hw_hotkey_confirmation_message, hwHotkey.button.name))
                .setPositiveButton(R.string.ok) { _, _ ->
                    // Actually delete
                    viewModel.delete(hwHotkey.id)
                    Snackbar.make(
                        requireParentFragment().requireView(),
                        getString(R.string.removed, hwHotkey.button.name),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
}