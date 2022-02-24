package org.docheinstein.minimotek.database

import android.content.Context
import android.net.Uri
import androidx.room.*
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkeyDao
import org.docheinstein.minimotek.database.hotkey.hw.HwHotkey
import org.docheinstein.minimotek.database.hotkey.hw.HwHotkeyDao
import org.docheinstein.minimotek.database.server.Server
import org.docheinstein.minimotek.database.server.ServerDao
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey

const val DATABASE_VERSION = 13
const val DATABASE_NAME = "minimote"


@Database(
    version = DATABASE_VERSION,
    exportSchema = false,
    entities = [
        Server::class,
        SwHotkey::class,
        HwHotkey::class
   ],
)
@TypeConverters(DB.Converters::class)
abstract class DB : RoomDatabase() {
    class Converters {
        @TypeConverter
        fun stringToUri(string: String?): Uri? {
            return if (string == null) null else Uri.parse(string)
        }

        @TypeConverter
        fun uriToString(uri: Uri?): String? {
            return uri?.toString()
        }
    }

    abstract fun serverDao(): ServerDao
    abstract fun hwHotkeyDao(): HwHotkeyDao
    abstract fun swHotkeyDao(): SwHotkeyDao

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