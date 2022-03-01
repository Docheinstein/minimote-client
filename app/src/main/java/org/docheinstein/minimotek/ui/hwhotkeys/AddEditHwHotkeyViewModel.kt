package org.docheinstein.minimotek.ui.hwhotkeys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.database.hotkey.hw.HwHotkey
import org.docheinstein.minimotek.database.hotkey.hw.HwHotkeyRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.verbose
import javax.inject.Inject


@HiltViewModel
class AddEditHwHotkeyViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val hwHotkeyRepository: HwHotkeyRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val HW_HOTKEY_ID_STATE_KEY = "hwHotkeyId"
        const val HW_HOTKEY_ID_NONE = Long.MIN_VALUE
    }

    enum class Mode {
        ADD,
        EDIT
    }

    private val hwHotkeyId: Long = savedStateHandle[HW_HOTKEY_ID_STATE_KEY] ?: HW_HOTKEY_ID_NONE
    val mode = if (hwHotkeyId == HW_HOTKEY_ID_NONE) Mode.ADD else Mode.EDIT

    var fetched = false
    val hwHotkey = if (mode == Mode.EDIT) hwHotkeyRepository.observe(hwHotkeyId).asLiveData() else null

    init {
        verbose("AddEditHwHotkeyViewModel.init() for hwHotkeyId = $hwHotkeyId")
    }

    override fun onCleared() {
        verbose("AddEditHwHotkeyViewModel.onCleared()")
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
            id = if (mode == Mode.ADD) AUTO_ID else hwHotkeyId,
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
        if (hwHotkeyId == HW_HOTKEY_ID_NONE) {
            error("Cannot delete, invalid hwHotkeyId")
            return
        }

        ioScope.launch {
            hwHotkeyRepository.delete(hwHotkeyId)
        }
    }
}