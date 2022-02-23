package org.docheinstein.minimotek.ui.swhotkeys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.AUTO_ID
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkeyRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject

private const val DEFAULT_HOTKEY_X = 24
private const val DEFAULT_HOTKEY_Y = 24

@HiltViewModel
class AddEditSwHotkeyViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val swHotkeyRepository: SwHotkeyRepository,
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

    val swHotkeyId: Long = savedStateHandle[HOTKEY_ID_STATE_KEY] ?: HOTKEY_ID_NONE
    val mode = if (swHotkeyId == HOTKEY_ID_NONE) Mode.ADD else Mode.EDIT
//    val swHotkey = if (mode == Mode.EDIT) swHotkeyRepository.load(hotkeyId).asLiveData() else null

    init {
        debug("AddEditHotkeyViewModel.init() for hotkeyId = $swHotkeyId")
    }
//
//    fun save(
//        key: MinimoteKeyType,
//        alt: Boolean,
//        altgr: Boolean,
//        ctrl: Boolean,
//        meta: Boolean,
//        shift: Boolean,
//        label: String?
//    ): SwHotkey {
//        val hotkey = SwHotkey(
//            id = if (mode == Mode.EDIT) swHotkey?.value!!.id else AUTO_ID,
//            alt = alt,
//            altgr = altgr,
//            ctrl = ctrl,
//            meta = meta,
//            shift = shift,
//            key = key,
//            label = label,
//            x = DEFAULT_HOTKEY_X,
//            y = DEFAULT_HOTKEY_Y
//        )
//        ioScope.launch {
//            swHotkeyRepository.save(hotkey)
//        }
//        return hotkey
//    }
//
//    fun delete() {
//        ioScope.launch {
//            swHotkeyRepository.delete(swHotkey?.value!!)
//        }
//    }
}