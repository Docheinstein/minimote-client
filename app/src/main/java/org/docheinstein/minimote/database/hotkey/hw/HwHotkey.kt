package org.docheinstein.minimote.database.hotkey.hw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.docheinstein.minimote.buttons.ButtonType
import org.docheinstein.minimote.database.hotkey.Hotkey
import org.docheinstein.minimote.keys.MinimoteKeyType

/**
 * Hardware hotkey.
 * i.e. [Hotkey] associated with a physical button [ButtonType]
 */

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
    indices = [Index(
        value = [COLUMN_BUTTON],
        unique = true
    )]
)
data class HwHotkey(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    var id: Long,

    @ColumnInfo(name = COLUMN_BUTTON)
    var button: ButtonType,

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
    override fun toString(): String {
        return "(id=$id, button=$button, shift=$shift, ctrl=$ctrl, alt=$alt, altgr=$altgr, meta=$meta, key=$key)"
    }

    fun toHotkey(): Hotkey {
        return Hotkey(
            key = key,
            shift = shift,
            ctrl = ctrl,
            alt = alt,
            altgr = altgr,
            meta = meta
        )
    }
}