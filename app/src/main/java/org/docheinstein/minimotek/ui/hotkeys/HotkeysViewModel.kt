package org.docheinstein.minimotek.ui.hotkeys

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
class HotkeysViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val hwHotkeyRepository: HwHotkeyRepository
) : ViewModel() {

    val hwHotkeys = hwHotkeyRepository.hwHotkeys.asLiveData()

    init {
        debug("HwHotkeysViewModel.init()")
    }

    fun delete(hwHotkey: HwHotkey) {
        ioScope.launch {
            hwHotkeyRepository.delete(hwHotkey)
        }
    }
}