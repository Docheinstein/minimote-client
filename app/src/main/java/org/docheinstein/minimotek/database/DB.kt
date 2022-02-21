package org.docheinstein.minimotek.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.docheinstein.minimotek.database.hwhotkey.HwHotkey
import org.docheinstein.minimotek.database.hwhotkey.HwHotkeyDao
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.database.server.ServerDao

const val DATABASE_VERSION = 6
const val DATABASE_NAME = "minimote"

@Database(
    version = DATABASE_VERSION,
    exportSchema = false,
    entities = [
        Server::class,
        HwHotkey::class
   ],
)
abstract class DB : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun hwHotkeyDao(): HwHotkeyDao

    // Singleton
    companion object {
        private var instance: DB? = null

        fun getInstance(context: Context): DB {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    DB::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}