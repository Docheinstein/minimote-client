package org.docheinstein.minimote.database.server;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import static org.docheinstein.minimote.database.server.MinimoteServerEntity.COLUMN_ADDRESS;
import static org.docheinstein.minimote.database.server.MinimoteServerEntity.TABLE_NAME;

@Dao
public interface MinimoteServerEntityDao {

    @Query( "SELECT * FROM " + TABLE_NAME +
            " WHERE " + COLUMN_ADDRESS + " = :address")
    MinimoteServerEntity getByAddress(String address);

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<MinimoteServerEntity>> getAllObservable();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addOrReplace(MinimoteServerEntity server);

    @Query("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ADDRESS + " = :address")
    int deleteByAddress(String address);
}
