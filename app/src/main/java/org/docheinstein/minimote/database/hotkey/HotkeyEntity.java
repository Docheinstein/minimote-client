package org.docheinstein.minimote.database.hotkey;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = HotkeyEntity.TABLE_NAME)
public class HotkeyEntity {

    public static final String TABLE_NAME = "hotkey";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SHIFT = "shift";
    public static final String COLUMN_CTRL = "ctrl";
    public static final String COLUMN_ALT = "alt";
    public static final String COLUMN_ALTGR = "altgr";
    public static final String COLUMN_META = "meta";
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_TEXT_SIZE = "text_size";
//    public static final String COLUMN_TEXT_COLOR = "text_color";
//    public static final String COLUMN_BACKGROUND_COLOR = "background_color";
//    public static final String COLUMN_BORDER_SIZE = "border_size";
//    public static final String COLUMN_BORDER_COLOR = "border_color";
    public static final String COLUMN_PADDING = "padding";
    public static final String COLUMN_X_REL = "x_rel";
    public static final String COLUMN_Y_ABS = "y_abs";

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public int id;

    @ColumnInfo(name = COLUMN_NAME)
    public String name;

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

    @ColumnInfo(name = COLUMN_TEXT_SIZE)
    public int textSize;

//    @ColumnInfo(name = COLUMN_TEXT_COLOR)
//    public int textColor;

//    @ColumnInfo(name = COLUMN_BACKGROUND_COLOR)
//    public int backgroundColor;

//    @ColumnInfo(name = COLUMN_BORDER_SIZE)
//    public int borderSize;

//    @ColumnInfo(name = COLUMN_BORDER_COLOR)
//    public int borderColor;

    @ColumnInfo(name = COLUMN_PADDING)
    public int padding;

    @ColumnInfo(name = COLUMN_X_REL)
    public double xRel;

    @ColumnInfo(name = COLUMN_Y_ABS)
    public double yAbs;

    public HotkeyEntity(
            int id,
            String name,
            boolean shift, boolean ctrl, boolean alt, boolean altgr, boolean meta,
            String key,
            int textSize,
//            int textColor,
//            int backgroundColor,
//            int borderSize, int borderColor,
            int padding,
            double xRel, double yAbs
    ) {
        this.id = id;
        this.name = name;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
        this.altgr = altgr;
        this.meta = meta;
        this.key = key;
        this.textSize = textSize;
//        this.textColor = textColor;
//        this.backgroundColor = backgroundColor;
//        this.borderSize = borderSize;
//        this.borderColor = borderColor;
        this.padding = padding;
        this.xRel = xRel;
        this.yAbs = yAbs;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format(
                "Id: %d\n" +
                "Name: %s\n" +
                "Shift: %b\n" +
                "Ctrl: %b\n" +
                "Alt: %b\n" +
                "AltGr: %b\n" +
                "Meta: %b\n" +
                "Key: %s\n" +
                "X: %f\n" +
                "Y: %f\n" +
                "textSize: %d\n" +
//                "textColor: %d\n" +
//                "backgroundColor: %d\n" +
//                "borderSize: %d\n" +
//                "borderColor: %d\n" +
                "padding: %d\n",
                id, name,
                shift, ctrl, alt, altgr, meta, key, xRel, yAbs,
                textSize,
//                textColor,
//                backgroundColor,
//                borderSize, borderColor,
                padding
        );
    }
}
