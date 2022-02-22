package org.docheinstein.minimotek.ui.hotkey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.database.hotkey.Hotkey
import org.docheinstein.minimotek.database.hotkey.HotkeyRepository
import org.docheinstein.minimotek.database.hwhotkey.HwHotkey
import org.docheinstein.minimotek.database.hwhotkey.HwHotkeyRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.ui.hwhotkey.AddEditHwHotkeyViewModel
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

    private val hotkeyId: Long = savedStateHandle[HOTKEY_ID_STATE_KEY] ?: HOTKEY_ID_NONE
    val mode = if (hotkeyId == HOTKEY_ID_NONE) Mode.ADD else Mode.EDIT
    val hotkey = if (mode == Mode.EDIT) hotkeyRepository.load(hotkeyId).asLiveData() else null

    init {
        debug("AddEditHotkeyViewModel.init() for hotkeyId = $hotkeyId")
    }

    fun save(
        key: MinimoteKeyType,
        alt: Boolean,
        altgr: Boolean,
        ctrl: Boolean,
        meta: Boolean,
        shift: Boolean,
        label: String?
        ): Hotkey {
        val hotkey = Hotkey(
            id = if (mode == Mode.EDIT) hotkey?.value!!.id else AUTO_ID,
            alt = alt,
            altgr = altgr,
            ctrl = ctrl,
            meta = meta,
            shift = shift,
            key = key,
            label = label,
        )
        ioScope.launch {
            hotkeyRepository.save(hotkey)
        }
        return hotkey
    }

    fun delete() {
        ioScope.launch {
            hotkeyRepository.delete(hotkey?.value!!)
        }
    }
}