package org.docheinstein.minimotek.data.server

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ServerDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    suspend fun get(id: Int): Server

    @Query("SELECT * FROM $TABLE_NAME")
    suspend fun getAll(): List<Server>

    @Insert
    suspend fun add(server: Server): Long
}