package com.rve.systemmonitor.data.di

import com.rve.systemmonitor.data.repository.SettingsRepositoryImpl
import com.rve.systemmonitor.data.repository.SystemInfoRepositoryImpl
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindSystemInfoRepository(
        systemInfoRepositoryImpl: SystemInfoRepositoryImpl
    ): SystemInfoRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
