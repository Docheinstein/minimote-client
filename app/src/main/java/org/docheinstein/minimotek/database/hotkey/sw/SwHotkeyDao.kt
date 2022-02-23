package org.docheinstein.minimotek.database.hotkey.sw

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface SwHotkeyDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun load(id: Long): Flow<SwHotkey>

    @Query("SELECT * FROM $TABLE_NAME")
    fun loadAll(): Flow<List<SwHotkey>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(hotkey: SwHotkey): Long

    @Delete
    suspend fun delete(hotkey: SwHotkey): Int

    // UPDATE
    @Query("UPDATE $TABLE_NAME SET $COLUMN_X = :x, $COLUMN_Y = :y WHERE $COLUMN_ID = :id")
    fun updatePosition(id: Long, x: Int, y: Int)
}