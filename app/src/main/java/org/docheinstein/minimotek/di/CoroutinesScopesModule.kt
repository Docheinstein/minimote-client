package org.docheinstein.minimotek.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.docheinstein.minimotek.util.debug
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
    fun providesDefaultGlobalCoroutineScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        debug("providesDefaultGlobalCoroutineScope")
        return CoroutineScope(SupervisorJob() + defaultDispatcher)
    }

    @Singleton
    @IOGlobalScope
    @Provides
    fun providesIOGLobalCoroutineScope(
            @IODispatcher ioDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        debug("providesIOGLobalCoroutineScope")
        return CoroutineScope(SupervisorJob() + ioDispatcher)
    }
}