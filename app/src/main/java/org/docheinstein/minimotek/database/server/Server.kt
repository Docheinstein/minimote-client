package org.docheinstein.minimotek.database.server

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Minimote server.
 * Identified by the pair (address, port).
 */

const val TABLE_NAME = "server"

const val COLUMN_ID = "id"
const val COLUMN_ADDRESS = "address"
const val COLUMN_PORT = "port"
const val COLUMN_NAME = "name"
const val COLUMN_ICON = "icon"

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
    var id: Long,

    @ColumnInfo(name = COLUMN_ADDRESS)
    var address: String,

    @ColumnInfo(name = COLUMN_PORT)
    var port: Int,

    @ColumnInfo(name = COLUMN_NAME)
    var name: String?,

    @ColumnInfo(name = COLUMN_ICON)
    var icon: Uri?,
) {

    val displayName: String
        get() = name ?: address

    override fun toString(): String {
        return "(id=$id, address=$address, port=$port, name=$name, icon=$icon)"
    }
}
