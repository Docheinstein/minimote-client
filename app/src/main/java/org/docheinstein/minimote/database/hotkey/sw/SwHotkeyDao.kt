package org.docheinstein.minimote.database.hotkey.sw

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.docheinstein.minimote.orientation.Orientation


@Dao
interface SwHotkeyDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun observe(id: Long): Flow<SwHotkey>

    @Query("SELECT * FROM $TABLE_NAME")
    fun observeAll(): Flow<List<SwHotkey>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ORIENTATION = :orientation")
    fun observeAllForOrientation(orientation: Orientation): Flow<List<SwHotkey>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    suspend fun get(id: Long): SwHotkey?

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
    suspend fun clearAll(): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ORIENTATION = :orientation")
    suspend fun clearAllForOrientation(orientation: Orientation): Int

    @Transaction
    suspend fun replaceAllForOrientation(orientation: Orientation, hotkeys: List<SwHotkey>) {
        clearAllForOrientation(orientation)
        saveAll(hotkeys)
    }
}