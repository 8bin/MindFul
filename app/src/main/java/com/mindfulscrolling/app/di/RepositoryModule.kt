package com.mindfulscrolling.app.di

import com.mindfulscrolling.app.data.repository.AppRepositoryImpl
import com.mindfulscrolling.app.data.repository.SettingsRepositoryImpl
import com.mindfulscrolling.app.data.repository.UsageRepositoryImpl
import com.mindfulscrolling.app.domain.repository.AppRepository
import com.mindfulscrolling.app.domain.repository.SettingsRepository
import com.mindfulscrolling.app.domain.repository.UsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(
        appRepositoryImpl: AppRepositoryImpl
    ): AppRepository

    @Binds
    @Singleton
    abstract fun bindUsageRepository(
        usageRepositoryImpl: UsageRepositoryImpl
    ): UsageRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
