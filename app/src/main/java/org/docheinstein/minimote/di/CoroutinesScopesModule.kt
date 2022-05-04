package org.docheinstein.minimote.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton


@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultGlobalScope

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IOGlobalScope

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesScopesModule {

    @Singleton
    @DefaultGlobalScope
    @Provides
    fun providesDefaultGlobalCoroutineScope(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher)
        = CoroutineScope(SupervisorJob() + defaultDispatcher)

    @Singleton
    @IOGlobalScope
    @Provides
    fun providesIOGlobalCoroutineScope(@IODispatcher ioDispatcher: CoroutineDispatcher)
         = CoroutineScope(SupervisorJob() + ioDispatcher)
}