package org.docheinstein.minimote.database.server;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = MinimoteServerEntity.TABLE_NAME)
public class MinimoteServerEntity {

    public static final String TABLE_NAME = "minimote_server";

    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_HOSTNAME = "hostname";
    public static final String COLUMN_DISPLAY_NAME = "display_name";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ADDRESS)
    public String address;

    @ColumnInfo(name = COLUMN_HOSTNAME)
    public String hostname;

    @ColumnInfo(name = COLUMN_DISPLAY_NAME)
    public String displayName;

    public MinimoteServerEntity(@NonNull String address, String hostname, String displayName) {
        this.address = address;
        this.hostname = hostname;
        this.displayName = displayName;
    }

    @NonNull
    @Override
    public String toString() {
        return displayName + " - " + address + " (" + hostname + ")";
    }
}
