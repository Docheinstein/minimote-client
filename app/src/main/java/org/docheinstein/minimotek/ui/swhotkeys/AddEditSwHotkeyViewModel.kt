package org.docheinstein.minimotek.ui.swhotkeys

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.verbose
import javax.inject.Inject

@HiltViewModel
class AddEditSwHotkeyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val HOTKEY_ID_STATE_KEY = "swHotkeyId"
        const val HOTKEY_ID_NONE = Long.MIN_VALUE
    }

    enum class Mode {
        ADD,
        EDIT
    }

    val swHotkeyId: Long = savedStateHandle[HOTKEY_ID_STATE_KEY] ?: HOTKEY_ID_NONE
    val mode = if (swHotkeyId == HOTKEY_ID_NONE) Mode.ADD else Mode.EDIT

    // Do not fetch from DB.
    // this must be fetched in-memory from the shared view model
    var swHotkey: SwHotkey? = null

    init {
        debug("AddEditSwHotkeyViewModel.init() for swHotkeyId = $swHotkeyId")
    }

    override fun onCleared() {
        verbose("AddEditHwHotkeyViewModel.onCleared()")
    }
}