package org.docheinstein.minimote.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.docheinstein.minimote.database.hotkey.HotkeyEntity;
import org.docheinstein.minimote.database.hotkey.HotkeyEntityDao;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.database.server.MinimoteServerEntityDao;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Database(
        entities = {
                MinimoteServerEntity.class,
                HotkeyEntity.class
        },
        version = 12)
public abstract class DB extends RoomDatabase {

    private static final String TAG = "DB";

    private static final String DATABASE_NAME = "minimote";
    private static DB sInstance;

    private ExecutorService mExecutor;

    public static synchronized void init(Context context) {
        if (sInstance != null) {
            Log.w(TAG, "Database already initialized, skipping init()");
            return;
        }
        sInstance = Room.databaseBuilder(
                context.getApplicationContext(), DB.class, DATABASE_NAME
        )
                .fallbackToDestructiveMigration()
                .build();
        sInstance.mExecutor = Executors.newSingleThreadExecutor();
    }

    public static DB getInstance() {
        if (sInstance == null)
            throw new RuntimeException("Database required before initialization");
        return sInstance;
    }

    public Executor getExecutor() {
        return mExecutor;
    }

    public Future<?> execute(Runnable runnable) {
        return mExecutor.submit(runnable);
    }

    public abstract MinimoteServerEntityDao minimoteServerDao();
    public abstract HotkeyEntityDao hotkeyEntityDao();
}
