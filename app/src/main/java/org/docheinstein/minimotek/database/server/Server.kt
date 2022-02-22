package org.docheinstein.minimotek.database.server

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val TABLE_NAME = "server"

const val COLUMN_ID = "id"
const val COLUMN_ADDRESS = "address"
const val COLUMN_PORT = "port"
const val COLUMN_NAME = "name"

@Entity(
    tableName = TABLE_NAME,
    indices = [Index(
        value = [COLUMN_ADDRESS, COLUMN_PORT],
        unique = true
    )]
)
data class Server(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    val id: Long,

    @ColumnInfo(name = COLUMN_ADDRESS)
    val address: String,

    @ColumnInfo(name = COLUMN_PORT)
    val port: Int,

    @ColumnInfo(name = COLUMN_NAME)
    val name: String?,
) {
    fun displayName(): String {
        return name ?: address
    }

    override fun toString(): String {
        return "(id=$id, address=$address, port=$port, name=$name)"
    }
}
