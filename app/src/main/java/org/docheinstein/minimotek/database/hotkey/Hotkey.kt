package org.docheinstein.minimotek.database.hotkey

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.keys.MinimoteKeyType


const val TABLE_NAME = "hotkey"

const val COLUMN_ID = "id"
const val COLUMN_KEY = "key"
const val COLUMN_LABEL = "label"
const val COLUMN_SHIFT = "shift"
const val COLUMN_CTRL = "ctrl"
const val COLUMN_ALT = "alt"
const val COLUMN_ALTGR = "altgr"
const val COLUMN_META = "meta"
const val COLUMN_X = "x"
const val COLUMN_Y = "y"


@Entity(
    tableName = TABLE_NAME,
)
data class Hotkey(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    val id: Long,

    @ColumnInfo(name = COLUMN_KEY)
    var key: MinimoteKeyType,

    @ColumnInfo(name = COLUMN_SHIFT)
    var shift: Boolean,

    @ColumnInfo(name = COLUMN_CTRL)
    var ctrl: Boolean,

    @ColumnInfo(name = COLUMN_ALT)
    var alt: Boolean,

    @ColumnInfo(name = COLUMN_ALTGR)
    var altgr: Boolean,

    @ColumnInfo(name = COLUMN_META)
    var meta: Boolean,

    @ColumnInfo(name = COLUMN_X)
    var x: Int,

    @ColumnInfo(name = COLUMN_Y)
    var y: Int,

    @ColumnInfo(name = COLUMN_LABEL)
    var label: String?,
) {
    fun displayName(): String {
        if (label != null)
            return label!!

        val tokens = mutableListOf<String>()
        if (ctrl)
            tokens.add("CTRL")
        if (alt)
            tokens.add("ALT")
        if (altgr)
            tokens.add("ALT GR")
        if (meta)
            tokens.add("META")
        if (shift)
            tokens.add("SHIFT")
        tokens.add(key.keyString)

        return tokens.joinToString(separator = "+")
    }


    override fun toString(): String {
        return "(id=$id, shift=$shift, ctrl=$ctrl, alt=$alt, altgr=$altgr, meta=$meta, key=${key.keyString}, label=$label)"
    }
}