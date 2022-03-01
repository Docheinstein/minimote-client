package org.docheinstein.minimotek.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier


@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IODispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@InstallIn(SingletonComponent::class)
@Module
object DispatchersModule {
    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher() = Dispatchers.Default

    @IODispatcher
    @Provides
    fun providesIODispatcher() = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher() = Dispatchers.Main
}