package org.docheinstein.minimotek.data.server

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun get(id: Long): Flow<Server>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Flow<List<Server>>

    @Insert
    suspend fun add(server: Server): Long

    @Update
    suspend fun update(server: Server): Int

    @Delete
    suspend fun delete(server: Server): Int
}