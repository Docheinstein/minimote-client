package org.docheinstein.minimotek.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.docheinstein.minimotek.database.DB
import org.docheinstein.minimotek.database.hotkey.HotkeyDao
import org.docheinstein.minimotek.database.hwhotkey.HwHotkeyDao
import org.docheinstein.minimotek.database.server.ServerDao
import org.docheinstein.minimotek.util.debug
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DBModule {
    @Singleton
    @Provides
    fun provideDB(@ApplicationContext context: Context): DB {
        debug("provideDB")
        return DB.getInstance(context)
    }

    @Provides
    fun provideServerDao(db: DB): ServerDao {
        debug("provideServerDao")
        return db.serverDao()
    }

    @Provides
    fun provideHotkeyDao(db: DB): HotkeyDao {
        debug("provideHotkeyDao")
        return db.hotkeyDao()
    }

    @Provides
    fun provideHwHotkeyDao(db: DB): HwHotkeyDao {
        debug("provideHwHotkeyDao")
        return db.hwHotkeyDao()
    }
}
