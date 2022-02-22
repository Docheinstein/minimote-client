package org.docheinstein.minimotek.ui.hwhotkey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.database.hwhotkey.HwHotkey
import org.docheinstein.minimotek.database.hwhotkey.HwHotkeyRepository
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject


@HiltViewModel
class AddEditHwHotkeyViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val hwHotkeyRepository: HwHotkeyRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val HW_HOTKEY_ID_STATE_KEY = "hwHotkeyId"
        const val HW_HOTKEY_ID_NONE = -1L
    }

    enum class Mode {
        ADD,
        EDIT
    }

    private val hwHotkeyId: Long = savedStateHandle[HW_HOTKEY_ID_STATE_KEY] ?: HW_HOTKEY_ID_NONE
    val mode = if (hwHotkeyId != HW_HOTKEY_ID_NONE) Mode.EDIT else Mode.ADD
    val hwHotkey = if (mode == Mode.EDIT) hwHotkeyRepository.load(hwHotkeyId).asLiveData() else null

    init {
        debug("AddEditHwHotkeyViewModel.init() for hwHotkeyId = $hwHotkeyId")
    }

    fun insert(hwHotkey: HwHotkey) {
        ioScope.launch {
            hwHotkeyRepository.add(hwHotkey)
        }
    }

    fun update(hwHotkey: HwHotkey) {
        ioScope.launch {
            hwHotkeyRepository.update(hwHotkey)
        }
    }

    fun delete() {
        ioScope.launch {
            hwHotkeyRepository.delete(hwHotkey?.value!!)
        }
    }
}