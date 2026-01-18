package com.simple.notification.testing.data.repositories.notification.xiaomi

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import kotlinx.coroutines.flow.first

class XiaomiPowerProvider : XiaomiProvider, PowerProvider {

    override suspend fun openBatteryOptimizationSettings(packageName: String) {
        try {
            val intent = Intent().apply {
                component = ComponentName("com.miui.securitycenter", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
                putExtra("package_name", packageName)
                putExtra("package_label", "NotiTesting")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            MainApplication.activityResumeFlow.first().startActivity(intent)
        } catch (e: Exception) {
            openGeneralBatterySettings()
        }
    }

    private suspend fun openGeneralBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            MainApplication.activityResumeFlow.first().startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            MainApplication.activityResumeFlow.first().startActivity(intent)
        }
    }
}
