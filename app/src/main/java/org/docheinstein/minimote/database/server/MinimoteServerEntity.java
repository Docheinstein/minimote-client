package org.docheinstein.minimote.database.server;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = MinimoteServerEntity.TABLE_NAME,
        primaryKeys = {
            MinimoteServerEntity.COLUMN_ADDRESS,
                MinimoteServerEntity.COLUMN_PORT
})
public class MinimoteServerEntity {

    public static final String TABLE_NAME = "minimote_server";

    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PORT = "port";
    public static final String COLUMN_HOSTNAME = "hostname";
    public static final String COLUMN_DISPLAY_NAME = "display_name";
    public static final String COLUMN_AUTO_CONNECT = "auto_connect";

    @NonNull
    @ColumnInfo(name = COLUMN_ADDRESS)
    public String address;

    @ColumnInfo(name = COLUMN_PORT)
    public int port;

    @ColumnInfo(name = COLUMN_HOSTNAME)
    public String hostname;

    @ColumnInfo(name = COLUMN_DISPLAY_NAME)
    public String displayName;

    @ColumnInfo(name = COLUMN_AUTO_CONNECT)
    public boolean autoConnect;

    public MinimoteServerEntity(@NonNull String address, int port,
                                String hostname, String displayName,
                                boolean autoConnect) {
        this.address = address;
        this.port = port;
        this.hostname = hostname;
        this.displayName = displayName;
        this.autoConnect = autoConnect;
    }

    @NonNull
    @Override
    public String toString() {
        return displayName + " - " + address + ":" + port + " (" + hostname + ")";
    }
}
