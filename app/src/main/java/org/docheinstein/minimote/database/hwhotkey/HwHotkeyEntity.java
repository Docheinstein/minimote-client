package org.docheinstein.minimote.database.hwhotkey;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = HwHotkeyEntity.TABLE_NAME)
public class HwHotkeyEntity {

    public static final String TABLE_NAME = "hwhotkey";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_BUTTON = "button";
    public static final String COLUMN_SHIFT = "shift";
    public static final String COLUMN_CTRL = "ctrl";
    public static final String COLUMN_ALT = "alt";
    public static final String COLUMN_ALTGR = "altgr";
    public static final String COLUMN_META = "meta";
    public static final String COLUMN_KEY = "key";

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public int id;

    @ColumnInfo(name = COLUMN_BUTTON)
    public String button;

    @ColumnInfo(name = COLUMN_SHIFT)
    public boolean shift;

    @ColumnInfo(name = COLUMN_CTRL)
    public boolean ctrl;

    @ColumnInfo(name = COLUMN_ALT)
    public boolean alt;

    @ColumnInfo(name = COLUMN_ALTGR)
    public boolean altgr;

    @ColumnInfo(name = COLUMN_META)
    public boolean meta;

    @ColumnInfo(name = COLUMN_KEY)
    public String key;

    public HwHotkeyEntity(
            int id,
            String button,
            boolean shift, boolean ctrl, boolean alt, boolean altgr, boolean meta,
            String key
    ) {
        this.id = id;
        this.button = button;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
        this.altgr = altgr;
        this.meta = meta;
        this.key = key;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format(
                "Id: %d\n" +
                "Button: %s\n" +
                "Shift: %b\n" +
                "Ctrl: %b\n" +
                "Alt: %b\n" +
                "AltGr: %b\n" +
                "Meta: %b\n" +
                "Key: %s\n",
                id, button,
                shift, ctrl, alt, altgr, meta, key
        );
    }
}
