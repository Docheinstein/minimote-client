package org.docheinstein.minimotek.ui.hwhotkeys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.database.hwhotkey.HwHotkey
import org.docheinstein.minimotek.database.hwhotkey.HwHotkeyRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
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
    val mode = if (hwHotkeyId == HW_HOTKEY_ID_NONE) Mode.ADD else Mode.EDIT
    val hwHotkey = if (mode == Mode.EDIT) hwHotkeyRepository.load(hwHotkeyId).asLiveData() else null

    init {
        debug("AddEditHwHotkeyViewModel.init() for hwHotkeyId = $hwHotkeyId")
    }

    fun save(
        button: ButtonType,
        alt: Boolean,
        altgr: Boolean,
        ctrl: Boolean,
        meta: Boolean,
        shift: Boolean,
        key: MinimoteKeyType
    ): HwHotkey {
        val hwHotkey = HwHotkey(
            id = if (mode == Mode.EDIT) hwHotkey?.value!!.id else AUTO_ID,
            button = button,
            alt = alt,
            altgr = altgr,
            ctrl = ctrl,
            meta = meta,
            shift = shift,
            key = key
        )
        ioScope.launch {
            hwHotkeyRepository.save(hwHotkey)
        }
        return hwHotkey
    }


    fun delete() {
        ioScope.launch {
            hwHotkeyRepository.delete(hwHotkey?.value!!)
        }
    }
}