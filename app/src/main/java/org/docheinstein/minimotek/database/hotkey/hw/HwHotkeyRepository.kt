package org.docheinstein.minimotek.database.hotkey.hw

import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HwHotkeyRepository @Inject constructor(private val hwHotkeyDao: HwHotkeyDao) {
    fun observe(id: Long): Flow<HwHotkey> {
        debug("HwHotkeyRepository.observe()")
        return hwHotkeyDao.observe(id)
    }

    fun observeAll(): Flow<List<HwHotkey>> {
        debug("HwHotkeyRepository.observeAll()")
        return hwHotkeyDao.observeAll()
    }

    suspend fun get(id: Long): HwHotkey? {
        debug("HwHotkeyRepository.get($id)")
        return hwHotkeyDao.get(id)
    }

    suspend fun getByButton(button: ButtonType): HwHotkey? {
        debug("HwHotkeyRepository.getByButton($button)")
        return hwHotkeyDao.getByButton(button)
    }

    suspend fun save(hwHotkey: HwHotkey) {
        debug("HwHotkeyRepository.save($hwHotkey)")
        hwHotkeyDao.save(hwHotkey)
    }

    suspend fun delete(id: Long) {
        debug("HwHotkeyRepository.delete($id)")
        hwHotkeyDao.delete(id)
    }
}