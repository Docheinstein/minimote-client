package org.docheinstein.minimotek.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.docheinstein.minimotek.data.server.Server
import org.docheinstein.minimotek.data.server.ServerDao

const val DATABASE_VERSION = 5
const val DATABASE_NAME = "minimote"

@Database(
    version = DATABASE_VERSION,
    exportSchema = false,
    entities = [Server::class],
)
abstract class DB : RoomDatabase() {
    abstract fun serverDao(): ServerDao

    // Singleton
    companion object {
        private var instance: DB? = null

        fun getInstance(context: Context): DB {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    DB::class.java, DATABASE_NAME
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}