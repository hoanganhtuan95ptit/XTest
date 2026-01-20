package com.simple.notification.testing.data.repositories.notification.xiaomi

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.google.auto.service.AutoService
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import kotlinx.coroutines.flow.first

@AutoService(AutoStartProvider::class)
class XiaomiAutoStartProvider : XiaomiProvider, AutoStartProvider {

    override fun isAvailable(): Boolean = true

    override fun isEnabled(packageName: String): Boolean? {
        return try {
            val context = MainApplication.share
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val uid = context.packageManager.getPackageUid(packageName, 0)
            
            val method = appOpsManager.javaClass.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val result = method.invoke(
                appOpsManager,
                10008, // OP_AUTO_START
                uid,
                packageName
            ) as Int
            result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun openAutoStartSettings(packageName: String) {
        try {
            val intent = Intent().apply {
                component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            MainApplication.activityResumeFlow.first().startActivity(intent)
        } catch (e: Exception) {
            // Fallback logic could be added here
        }
    }
}
