package org.docheinstein.minimote.database.hotkey.sw

import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimote.orientation.Orientation
import org.docheinstein.minimote.util.debug
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SwHotkeyRepository @Inject constructor(private val swHotkeyDao: SwHotkeyDao) {
    fun observeAll(orientation: Orientation? = null): Flow<List<SwHotkey>> {
        if (orientation == null) {
            debug("SwHotkeyRepository.observeAll()")
            return swHotkeyDao.observeAll()
        }

        debug("SwHotkeyRepository.observeAllForOrientation($orientation)")
        return swHotkeyDao.observeAllForOrientation(orientation)
    }

    suspend fun getAll(orientation: Orientation? = null): List<SwHotkey> {
        if (orientation == null) {
            debug("SwHotkeyRepository.getAll()")
            return swHotkeyDao.getAll()
        }

        debug("SwHotkeyRepository.getAllForOrientation($orientation)")
        return swHotkeyDao.getAllForOrientation(orientation)
    }

    suspend fun save(hotkey: SwHotkey) {
        debug("SwHotkeyRepository.save($hotkey)")
        swHotkeyDao.save(hotkey)
    }

    suspend fun delete(hotkey: SwHotkey) {
        debug("SwHotkeyRepository.delete($hotkey)")
        swHotkeyDao.delete(hotkey)
    }

    suspend fun replaceAllForOrientation(orientation: Orientation, hotkeys: List<SwHotkey>) {
        debug("SwHotkeyRepository.replaceAllForOrientation($orientation, ${hotkeys.size} hotkeys)")
        swHotkeyDao.replaceAllForOrientation(orientation, hotkeys)
    }
}