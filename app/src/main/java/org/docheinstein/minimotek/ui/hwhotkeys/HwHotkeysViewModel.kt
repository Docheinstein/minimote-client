package org.docheinstein.minimotek.ui.hwhotkeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.docheinstein.minimotek.database.hotkey.hw.HwHotkeyRepository
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.util.verbose
import javax.inject.Inject


@HiltViewModel
class HwHotkeysViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    private val hwHotkeyRepository: HwHotkeyRepository
) : ViewModel() {

    val hwHotkeys = hwHotkeyRepository.observeAll().asLiveData()

    init {
        verbose("HwHotkeysViewModel.init()")
    }

    override fun onCleared() {
        verbose("HwHotkeysViewModel.onCleared()")
    }

    fun delete(id: Long) {
        ioScope.launch {
            hwHotkeyRepository.delete(id)
        }
    }
}