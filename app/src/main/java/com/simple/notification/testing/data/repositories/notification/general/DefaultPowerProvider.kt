package com.simple.notification.testing.data.repositories.notification.general

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.google.auto.service.AutoService
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import kotlinx.coroutines.flow.first

@AutoService(PowerProvider::class)
class DefaultPowerProvider : DefaultProvider, PowerProvider {

    override suspend fun openBatteryOptimizationSettings(packageName: String) {
        val activity = MainApplication.activityResumeFlow.first()
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
            } catch (e2: Exception) {
                activity.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }
}
