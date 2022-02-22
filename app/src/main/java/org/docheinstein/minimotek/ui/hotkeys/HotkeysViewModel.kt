package org.docheinstein.minimotek.ui.hotkeys

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
import org.docheinstein.minimotek.ui.hwhotkeys.AddEditHwHotkeyViewModel
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject

@HiltViewModel
class HotkeysViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val hotkeyRepository: HotkeyRepository
) : ViewModel() {

    val hotkeys = hotkeyRepository.hotkeys.asLiveData()

    init {
        debug("HwHotkeysViewModel.init()")
    }

    fun updatePosition(id: Long, x: Int, y: Int) {
        ioScope.launch {
            hotkeyRepository.updatePosition(id, x, y)
        }
    }
}