package org.docheinstein.minimotek.database.hwhotkey

import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.info
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HwHotkeyRepository @Inject constructor(private val hwHotkeyDao: HwHotkeyDao) {
    val hwHotkeys: Flow<List<HwHotkey>> = hwHotkeyDao.loadAll()

    fun load(id: Long): Flow<HwHotkey> {
        return hwHotkeyDao.load(id)
    }

    fun loadAll(): Flow<List<HwHotkey>> {
        return hwHotkeyDao.loadAll()
    }

    suspend fun get(button: ButtonType): HwHotkey? {
        debug("Getting hw hotkey by button $button")
        return hwHotkeyDao.getByButton(button)
    }

    suspend fun save(hwHotkey: HwHotkey) {
        info("Saving hwHotkey $hwHotkey")
        hwHotkeyDao.save(hwHotkey)
    }

    suspend fun delete(hwHotkey: HwHotkey) {
        info("Deleting hwHotkey $hwHotkey")
        hwHotkeyDao.delete(hwHotkey)
    }
}