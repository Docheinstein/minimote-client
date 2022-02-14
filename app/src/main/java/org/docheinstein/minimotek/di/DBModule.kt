package org.docheinstein.minimotek.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.docheinstein.minimotek.data.DB
import org.docheinstein.minimotek.data.server.ServerDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DBModule {
    @Singleton
    @Provides
    fun provideDB(@ApplicationContext context: Context): DB {
        return DB.getInstance(context)
    }

    @Provides
    fun provideServerDao(db: DB): ServerDao {
        return db.serverDao()
    }
}
