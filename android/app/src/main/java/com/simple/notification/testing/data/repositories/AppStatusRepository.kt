package com.simple.notification.testing.data.repositories

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.simple.notification.testing.data.models.AppStatus
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import com.simple.notification.testing.data.repositories.notification.general.DefaultNotificationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppStatusRepository(private val context: Context) {

    private val notificationProvider = DefaultNotificationProvider()

    suspend fun getInstalledApps(): List<AppStatus> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val powerProvider = PowerProvider.get()
        val autoStartProvider = AutoStartProvider.get()

        apps.filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName == context.packageName
        }.mapNotNull { appInfo ->
            val packageName = appInfo.packageName
            val appName = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)

            val isNotificationGranted = notificationProvider.isGranted(packageName)
            val isAutoStartEnabled = autoStartProvider.isEnabled(packageName)
            val isIgnoringBatteryOptimizations = powerProvider.isIgnoringBatteryOptimizations(packageName)

            AppStatus(
                packageName = packageName,
                appName = appName,
                icon = icon,
                isNotificationGranted = isNotificationGranted,
                isAutoStartEnabled = isAutoStartEnabled,
                isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations
            )
        }.sortedBy { it.appName }
    }
}
