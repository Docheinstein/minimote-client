package org.docheinstein.minimote.database.hotkey;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import static org.docheinstein.minimote.database.hotkey.HotkeyEntity.COLUMN_ID;
import static org.docheinstein.minimote.database.hotkey.HotkeyEntity.COLUMN_X_REL;
import static org.docheinstein.minimote.database.hotkey.HotkeyEntity.COLUMN_Y_ABS;
import static org.docheinstein.minimote.database.hotkey.HotkeyEntity.TABLE_NAME;

@Dao
public interface HotkeyEntityDao {

    // SELECT

    @Query( "SELECT * FROM " + TABLE_NAME +
            " WHERE " + COLUMN_ID + " = :id")
    HotkeyEntity getById(int id);

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<HotkeyEntity>> getAllObservable();

    // INSERT

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addOrReplace(HotkeyEntity hotkey);

    // UPDATE

    @Query("UPDATE " + TABLE_NAME +
            " SET " + COLUMN_X_REL + " = :xRel, " + COLUMN_Y_ABS + " = :yAbs " +
            "WHERE " + COLUMN_ID + " = :id")
    void updatePosition(int id, double xRel, int yAbs);

    // DELETE

    @Query("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = :id")
    int deleteById(int id);
}
