package org.docheinstein.minimotek.database.server

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun load(id: Long): Flow<Server>

    @Query("SELECT * FROM $TABLE_NAME")
    fun loadAll(): Flow<List<Server>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(server: Server): Long

    @Update
    suspend fun update(server: Server): Int

    @Delete
    suspend fun delete(server: Server): Int
}