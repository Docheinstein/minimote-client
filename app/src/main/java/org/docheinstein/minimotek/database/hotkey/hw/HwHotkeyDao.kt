package org.docheinstein.minimotek.database.hotkey.hw

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.buttons.ButtonType

@Dao
interface HwHotkeyDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun load(id: Long): Flow<HwHotkey>

    @Query("SELECT * FROM $TABLE_NAME")
    fun loadAll(): Flow<List<HwHotkey>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_BUTTON = :button")
    suspend fun getByButton(button: ButtonType): HwHotkey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(hwHotkey: HwHotkey): Long

    @Delete
    suspend fun delete(hwHotkey: HwHotkey): Int
}