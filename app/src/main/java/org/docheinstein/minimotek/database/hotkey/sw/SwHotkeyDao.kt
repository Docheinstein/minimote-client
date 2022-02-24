package org.docheinstein.minimotek.database.hotkey.sw

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimotek.orientation.Orientation


@Dao
interface SwHotkeyDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun load(id: Long): Flow<SwHotkey>

    @Query("SELECT * FROM $TABLE_NAME")
    fun loadAll(): Flow<List<SwHotkey>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ORIENTATION = :orientation")
    fun loadAllForOrientation(orientation: Orientation): Flow<List<SwHotkey>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    suspend fun get(id: Long): SwHotkey

    @Query("SELECT * FROM $TABLE_NAME")
    suspend fun getAll(): List<SwHotkey>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ORIENTATION = :orientation")
    suspend fun getAllForOrientation(orientation: Orientation): List<SwHotkey>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(hotkey: SwHotkey): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(hotkeys: List<SwHotkey>)

    @Delete
    suspend fun delete(hotkey: SwHotkey): Int

    @Query("DELETE FROM $TABLE_NAME")
    suspend fun clear(): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ORIENTATION = :orientation")
    suspend fun clearForOrientation(orientation: Orientation): Int


    @Transaction
    suspend fun replaceForOrientation(orientation: Orientation, hotkeys: List<SwHotkey>) {
        clearForOrientation(orientation)
        saveAll(hotkeys)
    }

    // UPDATE
    @Query("UPDATE $TABLE_NAME SET $COLUMN_X = :x, $COLUMN_Y = :y WHERE $COLUMN_ID = :id")
    fun updatePosition(id: Long, x: Int, y: Int)
}