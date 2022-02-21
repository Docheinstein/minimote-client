package org.docheinstein.minimotek.database.hwhotkey

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.keys.MinimoteKeyType


const val TABLE_NAME = "hwhotkey"
const val COLUMN_ID = "id"
const val COLUMN_BUTTON = "button"
const val COLUMN_SHIFT = "shift"
const val COLUMN_CTRL = "ctrl"
const val COLUMN_ALT = "alt"
const val COLUMN_ALTGR = "altgr"
const val COLUMN_META = "meta"
const val COLUMN_KEY = "key"

@Entity(
    tableName = TABLE_NAME,
)
data class HwHotkey(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    val id: Long,

    @ColumnInfo(name = COLUMN_BUTTON)
    val button: ButtonType,

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

    @ColumnInfo(name = COLUMN_KEY)
    var key: MinimoteKeyType,
) {
    constructor(button: ButtonType, shift: Boolean, ctrl: Boolean, alt: Boolean, altgr: Boolean, meta: Boolean, key: MinimoteKeyType) :
        this(0, button, shift, ctrl, alt, altgr, meta, key)

    override fun toString(): String {
        return "(id=$id, button=$button, shift=$shift, ctrl=$ctrl, alt=$alt, altgr=$altgr, meta=$meta, key=$key)"
    }
}