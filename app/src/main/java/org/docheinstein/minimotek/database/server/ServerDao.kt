package org.docheinstein.minimotek.database.server

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun observe(id: Long): Flow<Server>

    @Query("SELECT * FROM $TABLE_NAME")
    fun observeAll(): Flow<List<Server>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    suspend fun get(id: Long): Server?

    @Query("SELECT * FROM $TABLE_NAME")
    suspend fun getAll(): List<Server>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(server: Server): Long

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    suspend fun delete(id: Long): Int
}