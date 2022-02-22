package org.docheinstein.minimotek.database.hotkey

import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.util.info
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HotkeyRepository @Inject constructor(private val hotkeyDao: HotkeyDao) {
    val hotkeys: Flow<List<Hotkey>> = hotkeyDao.loadAll()

    fun load(id: Long): Flow<Hotkey> {
        return hotkeyDao.load(id)
    }

    suspend fun save(hotkey: Hotkey) {
        info("Saving hotkeyDao $hotkey")
        hotkeyDao.save(hotkey)
    }

    suspend fun delete(hotkey: Hotkey) {
        info("Deleting hwHotkey $hotkey")
        hotkeyDao.delete(hotkey)
    }
}