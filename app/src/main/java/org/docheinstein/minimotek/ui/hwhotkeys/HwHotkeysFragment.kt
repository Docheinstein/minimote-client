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
import org.docheinstein.minimotek.database.hwhotkey.HwHotkey
import org.docheinstein.minimotek.databinding.*
import org.docheinstein.minimotek.ui.controller.base.SelectableListAdapter
import org.docheinstein.minimotek.ui.hwhotkey.AddEditHwHotkeyFragmentDirections
import org.docheinstein.minimotek.ui.hwhotkey.AddEditHwHotkeyViewModel
import org.docheinstein.minimotek.ui.servers.ServersFragmentDirections
import org.docheinstein.minimotek.util.debug


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
            return oldItem.button == newItem.button // UI based equality
        }
    }

    class HwHotkeyListAdapter : SelectableListAdapter<HwHotkey, HwHotkeyListAdapter.ViewHolder>(HwHotkeyDiffCallback()) {
        class ViewHolder(
            val binding: HwHotkeyListItemBinding
        ) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
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
                    menu.setHeaderTitle(view.context.getString(R.string.hw_hotkey_list_item_context_menu_title))
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

            holder.binding.label.text = hwHotkey.button.name

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
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        debug("HwHotkeysFragment.onCreateView()")

        binding = HwHotkeyListBinding.inflate(inflater, container, false)


        // Server list
        adapter = HwHotkeyListAdapter()
        binding.hwHotkeyList.adapter = adapter

        // Observe server list changes
        viewModel.hwHotkeys.observe(viewLifecycleOwner) { hwHotkeys ->
            debug("Server list update detected (new size = ${hwHotkeys.size}, changing UI accordingly)")
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
                handleAddHwHotkeyButton()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit_menu_item -> {
                val hwHotkey = adapter.selected
                debug("Going to edit hwHotkey at position ${adapter.selection}: ${hwHotkey?.id}")
                if (hwHotkey != null) {
                    findNavController().navigate(
                        HwHotkeysFragmentDirections.actionAddEditHwHotkey(
                            hwHotkey.id,
                            getString(R.string.toolbar_title_edit_hw_hotkey)
                        )
                    )
                }
            }
            R.id.delete_menu_item -> {
                val hwHotkey = adapter.selected
                debug("Going to delete hwHotkey at position ${adapter.selection}: ${hwHotkey?.id}")
                if (hwHotkey != null) {
                    AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.delete_hw_hotkey_confirmation_title)
                        .setMessage(R.string.delete_hw_hotkey_confirmation_message)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            // actually delete
                            viewModel.delete(hwHotkey)
                            Snackbar.make(
                                requireParentFragment().requireView(),
                                getString(R.string.hw_hotkey_removed, hwHotkey.button.name),
                                Snackbar.LENGTH_LONG
                            ).show()

                            findNavController().navigateUp()
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun handleAddHwHotkeyButton() {
        findNavController().navigate(
            HwHotkeysFragmentDirections.actionAddEditHwHotkey(
                AddEditHwHotkeyViewModel.HW_HOTKEY_ID_NONE,
                getString(R.string.toolbar_title_add_hw_hotkey)
            )
        )
    }
}