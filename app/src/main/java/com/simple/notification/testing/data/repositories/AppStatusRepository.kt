package com.simple.notification.testing.data.repositories

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.simple.notification.testing.AutoStartUtils
import com.simple.notification.testing.NotificationUtils_Legacy
import com.simple.notification.testing.PowerUtils
import com.simple.notification.testing.data.models.AppStatus

class AppStatusRepository(private val context: Context) {

    fun getInstalledApps(): List<AppStatus> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return apps.filter { 
            // Lọc bỏ app hệ thống nếu muốn, ở đây giữ lại các app có thể có ích
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName == context.packageName
        }.map { appInfo ->
            val packageName = appInfo.packageName
            val appName = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            
            val isNotificationGranted = NotificationUtils_Legacy.isGranted(context, packageName)
            val isAutoStartEnabled = AutoStartUtils.isEnabled(context, packageName)
            val isIgnoringBatteryOptimizations = PowerUtils.isIgnoringBatteryOptimizations(context, packageName)
            
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
