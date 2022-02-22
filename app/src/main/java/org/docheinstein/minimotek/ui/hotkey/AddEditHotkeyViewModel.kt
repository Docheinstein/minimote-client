package org.docheinstein.minimotek.ui.hotkey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.database.hwhotkey.HwHotkey
import org.docheinstein.minimotek.database.hwhotkey.HwHotkeyRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject


@HiltViewModel
class AddEditHotkeyViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val hotkeyRepository: HotkeyRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val HOTKEY_ID_STATE_KEY = "hotkeyId"
        const val HOTKEY_ID_NONE = -1L
    }

    enum class Mode {
        ADD,
        EDIT
    }

    private val hwHotkeyId: Long = savedStateHandle[HOTKEY_ID_STATE_KEY] ?: HOTKEY_ID_NONE
    val mode = if (hwHotkeyId != HOTKEY_ID_NONE) Mode.EDIT else Mode.ADD
    val hotkey = if (mode == Mode.EDIT) hotkeyRepository.load(hwHotkeyId).asLiveData() else null

    init {
        debug("AddEditHotkeyViewModel.init() for hwHotkeyId = $hwHotkeyId")
    }

    fun insert(hwHotkey: HwHotkey) {
        ioScope.launch {
            hotkeyRepository.add(hwHotkey)
        }
    }

    fun update(hwHotkey: HwHotkey) {
        ioScope.launch {
            hotkeyRepository.update(hwHotkey)
        }
    }

    fun delete() {
        ioScope.launch {
            hotkeyRepository.delete(hotkey?.value!!)
        }
    }
}