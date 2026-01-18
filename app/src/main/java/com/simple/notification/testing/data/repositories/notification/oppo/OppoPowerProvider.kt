package com.simple.notification.testing.data.repositories.notification.oppo

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import com.google.auto.service.AutoService
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import kotlinx.coroutines.flow.first

@AutoService(PowerProvider::class)
class OppoPowerProvider : OppoProvider, PowerProvider {

    override suspend fun openBatteryOptimizationSettings(packageName: String) {
        val activity = MainApplication.activityResumeFlow.first()
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.coloros.oppoguardelf",
                    "com.coloros.powermanager.fusion.primary.PrimaryBatteryActivity"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            openGeneralBatterySettings()
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
