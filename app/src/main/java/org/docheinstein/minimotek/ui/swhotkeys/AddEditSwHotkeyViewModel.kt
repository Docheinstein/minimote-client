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
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val HOTKEY_ID_STATE_KEY = "hotkeyId"
        const val HOTKEY_ID_NONE = Long.MIN_VALUE
    }

    enum class Mode {
        ADD,
        EDIT
    }

    val swHotkeyId: Long = savedStateHandle[HOTKEY_ID_STATE_KEY] ?: HOTKEY_ID_NONE
    val mode = if (swHotkeyId == HOTKEY_ID_NONE) Mode.ADD else Mode.EDIT
    var swHotkey: SwHotkey? = null

    init {
        debug("AddEditSwHotkeyViewModel.init() for hotkeyId = $swHotkeyId")
    }
}