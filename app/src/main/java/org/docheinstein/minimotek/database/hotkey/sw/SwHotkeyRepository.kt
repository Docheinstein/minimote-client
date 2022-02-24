package org.docheinstein.minimotek.database.hotkey.sw

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.orientation.Orientation
import org.docheinstein.minimotek.util.info
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SwHotkeyRepository @Inject constructor(private val swHotkeyDao: SwHotkeyDao) {
    val hotkeys: Flow<List<SwHotkey>> = swHotkeyDao.loadAll()
    val landscapeHotkeys: Flow<List<SwHotkey>> = swHotkeyDao.loadAllForOrientation(Orientation.Landscape)
    val portraitHotkeys: Flow<List<SwHotkey>> = swHotkeyDao.loadAllForOrientation(Orientation.Portrait)

    fun load(id: Long): Flow<SwHotkey> {
        return swHotkeyDao.load(id)
    }

    suspend fun getAll(orientation: Orientation? = null): List<SwHotkey> {
        return if (orientation == null)
            swHotkeyDao.getAll()
        else
            swHotkeyDao.getAllForOrientation(orientation)
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

    suspend fun clear(orientation: Orientation? = null) {
        info("Clearing hotkeys for orientation $orientation")
        if (orientation == null)
            swHotkeyDao.clear()
        else
            swHotkeyDao.clearForOrientation(orientation)
    }

    suspend fun replaceForOrientation(orientation: Orientation, hotkeys: List<SwHotkey>) {
        info("Replacing hotkeys for orientation $orientation")
        swHotkeyDao.replaceForOrientation(orientation, hotkeys)
    }
}