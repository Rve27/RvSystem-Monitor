package com.rve.systemmonitor.data.di

import com.rve.systemmonitor.data.repository.BatteryRepositoryImpl
import com.rve.systemmonitor.data.repository.CpuRepositoryImpl
import com.rve.systemmonitor.data.repository.HardwareRepositoryImpl
import com.rve.systemmonitor.data.repository.MemoryRepositoryImpl
import com.rve.systemmonitor.data.repository.SettingsRepositoryImpl
import com.rve.systemmonitor.domain.repository.BatteryRepository
import com.rve.systemmonitor.domain.repository.CpuRepository
import com.rve.systemmonitor.domain.repository.HardwareRepository
import com.rve.systemmonitor.domain.repository.MemoryRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
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
    abstract fun bindBatteryRepository(batteryRepositoryImpl: BatteryRepositoryImpl): BatteryRepository

    @Binds
    @Singleton
    abstract fun bindCpuRepository(cpuRepositoryImpl: CpuRepositoryImpl): CpuRepository

    @Binds
    @Singleton
    abstract fun bindMemoryRepository(memoryRepositoryImpl: MemoryRepositoryImpl): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindHardwareRepository(hardwareRepositoryImpl: HardwareRepositoryImpl): HardwareRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository
}
