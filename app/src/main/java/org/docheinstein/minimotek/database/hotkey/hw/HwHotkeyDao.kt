package org.docheinstein.minimotek.database.hotkey.hw

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.buttons.ButtonType

@Dao
interface HwHotkeyDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun observe(id: Long): Flow<HwHotkey>

    @Query("SELECT * FROM $TABLE_NAME")
    fun observeAll(): Flow<List<HwHotkey>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    suspend fun get(id: Long): HwHotkey?

    @Query("SELECT * FROM $TABLE_NAME")
    suspend fun getAll(): List<HwHotkey>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_BUTTON = :button")
    suspend fun getByButton(button: ButtonType): HwHotkey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(hwHotkey: HwHotkey): Long

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    suspend fun delete(id: Long): Int
}