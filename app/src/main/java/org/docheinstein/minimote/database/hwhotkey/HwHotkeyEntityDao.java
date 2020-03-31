package org.docheinstein.minimote.database.hwhotkey;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import static org.docheinstein.minimote.database.hwhotkey.HwHotkeyEntity.COLUMN_BUTTON;
import static org.docheinstein.minimote.database.hwhotkey.HwHotkeyEntity.COLUMN_ID;
import static org.docheinstein.minimote.database.hwhotkey.HwHotkeyEntity.TABLE_NAME;


@Dao
public interface HwHotkeyEntityDao {

    // SELECT

    @Query( "SELECT * FROM " + TABLE_NAME +
            " WHERE " + COLUMN_ID + " = :id")
    HwHotkeyEntity getById(int id);

    @Query( "SELECT * FROM " + TABLE_NAME +
            " WHERE " + COLUMN_BUTTON + " = :button")
    HwHotkeyEntity getByButton(String button);

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<HwHotkeyEntity>> getAllObservable();

    // INSERT

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addOrReplace(HwHotkeyEntity hotkey);

    // DELETE

    @Query("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = :id")
    int deleteById(int id);
}
