package com.codconfirm.app.di

import com.codconfirm.app.core.common.DefaultDispatcherProvider
import com.codconfirm.app.core.common.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        implementation: DefaultDispatcherProvider,
    ): DispatcherProvider

    companion object {
        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
