package com.leekleak.trafficlight.integrations

import android.content.pm.PackageManager
import com.leekleak.play_integration.playModule
import org.koin.dsl.module

val integrationsModule = module {
    includes(playModule)
    single<PlayServicesProvider> { PlayServicesProviderImpl(get()) }
    single<ShizukuServicesProvider> {
        object : ShizukuServicesProvider {
            override fun updateSimData() = Unit
            override fun shizukuRunning(): Boolean = false
            override fun shizukuPermission(): Int = PackageManager.PERMISSION_DENIED
            override fun shizukuRequestPermission() = Unit
            override fun enable() = Unit
            override fun disable() = Unit
        }
    }
}