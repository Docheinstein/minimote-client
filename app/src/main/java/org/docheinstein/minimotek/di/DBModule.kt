package org.docheinstein.minimotek.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.docheinstein.minimotek.database.DB
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DBModule {
    @Singleton
    @Provides
    fun provideDB(@ApplicationContext context: Context) = DB.getInstance(context)

    @Provides
    fun provideServerDao(db: DB) = db.serverDao()

    @Provides
    fun provideSwHotkeyDao(db: DB) = db.swHotkeyDao()

    @Provides
    fun provideHwHotkeyDao(db: DB) = db.hwHotkeyDao()
}
