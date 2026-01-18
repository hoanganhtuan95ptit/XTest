package com.simple.notification.testing.data.repositories.notification.vivo

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import com.google.auto.service.AutoService
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import kotlinx.coroutines.flow.first

@AutoService(PowerProvider::class)
class VivoPowerProvider : VivoProvider, PowerProvider {

    override suspend fun openBatteryOptimizationSettings(packageName: String) {
        val activity = MainApplication.activityResumeFlow.first()
        try {
            val intent = Intent().apply {
                component = ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent().apply {
                    component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
            } catch (e2: Exception) {
                openGeneralBatterySettings()
            }
        }
    }

    private suspend fun openGeneralBatterySettings() {
        val activity = MainApplication.activityResumeFlow.first()
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        }
    }
}
