package org.docheinstein.minimotek.data.server

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val TABLE_NAME = "server"
const val COLUMN_ID = "id"
const val COLUMN_ADDRESS = "address"
const val COLUMN_PORT = "port"
const val COLUMN_NAME = "name"

@Entity(tableName = TABLE_NAME)
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
    constructor(address: String, port: Int, name: String?) :
            this(0, address, port, name)
}
