package org.docheinstein.minimotek.database.hotkey.sw

import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.util.info
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SwHotkeyRepository @Inject constructor(private val swHotkeyDao: SwHotkeyDao) {
    val hotkeys: Flow<List<SwHotkey>> = swHotkeyDao.loadAll()

    fun load(id: Long): Flow<SwHotkey> {
        return swHotkeyDao.load(id)
    }

    suspend fun updatePosition(id: Long, x: Int, y: Int) {
        info("Updating hotkey position $id")
        swHotkeyDao.updatePosition(id, x, y)
    }
    suspend fun save(hotkey: SwHotkey) {
        info("Saving hotkey $hotkey")
        swHotkeyDao.save(hotkey)
    }

    suspend fun delete(hotkey: SwHotkey) {
        info("Deleting hotkey $hotkey")
        swHotkeyDao.delete(hotkey)
    }
}