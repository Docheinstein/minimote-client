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

    suspend fun updatePosition(id: Long, x: Int, y: Int) {
        info("Updating hotkey position $id")
        hotkeyDao.updatePosition(id, x, y)
    }
    suspend fun save(hotkey: Hotkey) {
        info("Saving hotkey $hotkey")
        hotkeyDao.save(hotkey)
    }

    suspend fun delete(hotkey: Hotkey) {
        info("Deleting hotkey $hotkey")
        hotkeyDao.delete(hotkey)
    }
}