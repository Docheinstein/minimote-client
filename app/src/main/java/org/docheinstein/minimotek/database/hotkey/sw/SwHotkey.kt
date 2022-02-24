package org.docheinstein.minimotek.database.hotkey.sw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.docheinstein.minimotek.orientation.Orientation
import org.docheinstein.minimotek.database.hotkey.Hotkey
import org.docheinstein.minimotek.keys.MinimoteKeyType


const val TABLE_NAME = "swhotkey"

const val COLUMN_ID = "id"
const val COLUMN_KEY = "key"
const val COLUMN_LABEL = "label"
const val COLUMN_SHIFT = "shift"
const val COLUMN_CTRL = "ctrl"
const val COLUMN_ALT = "alt"
const val COLUMN_ALTGR = "altgr"
const val COLUMN_META = "meta"
const val COLUMN_ORIENTATION = "orientation"
const val COLUMN_X = "x"
const val COLUMN_Y = "y"
const val COLUMN_SIZE = "size"


@Entity(
    tableName = TABLE_NAME,
)
data class SwHotkey (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    var id: Long,

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

    @ColumnInfo(name = COLUMN_ORIENTATION)
    var orientation: Orientation,

    @ColumnInfo(name = COLUMN_X)
    var x: Int,

    @ColumnInfo(name = COLUMN_Y)
    var y: Int,

    @ColumnInfo(name = COLUMN_SIZE)
    var size: Int,

    @ColumnInfo(name = COLUMN_LABEL)
    var label: String?,
) {

    override fun toString(): String {
        return "(id=$id, " +
                "shift=$shift, ctrl=$ctrl, alt=$alt, altgr=$altgr, meta=$meta, " +
                "key=${key.keyString}, " +
                "label=$label, orientation=$orientation, x=$x, y=$y, size=$size)"
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