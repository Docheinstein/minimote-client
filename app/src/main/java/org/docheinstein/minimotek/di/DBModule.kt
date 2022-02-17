package org.docheinstein.minimotek.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.docheinstein.minimotek.data.DB
import org.docheinstein.minimotek.data.server.ServerDao
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
}
