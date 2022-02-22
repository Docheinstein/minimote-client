package org.docheinstein.minimotek.database.hotkey

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface HotkeyDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun load(id: Long): Flow<Hotkey>

    @Query("SELECT * FROM $TABLE_NAME")
    fun loadAll(): Flow<List<Hotkey>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(hotkey: Hotkey): Long

    @Delete
    suspend fun delete(hotkey: Hotkey): Int
}