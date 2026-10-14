package com.rve.systemmonitor

import android.app.Application
import com.rve.systemmonitor.privileged.PrivilegedAccess
import com.rve.systemmonitor.shizuku.ShizukuManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RvSystemMonitorApplication : Application() {
    @Inject
    lateinit var shizukuManager: ShizukuManager

    @Inject
    lateinit var privilegedAccess: PrivilegedAccess

    override fun onCreate() {
        super.onCreate()
        shizukuManager.init()
        privilegedAccess.init()
    }
}
