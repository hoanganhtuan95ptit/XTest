package com.simple.notification.testing.data.repositories.notification.huawei

import android.content.ComponentName
import android.content.Intent
import com.google.auto.service.AutoService
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import kotlinx.coroutines.flow.first

@AutoService(AutoStartProvider::class)
class HuaweiAutoStartProvider : HuaweiProvider, AutoStartProvider {

    override fun isAvailable(): Boolean = true

    override fun isEnabled(packageName: String): Boolean? {
        // Huawei doesn't easily expose auto-start status via AppOps or public API
        return null
    }

    override suspend fun openAutoStartSettings(packageName: String) {
        val intents = listOf(
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"))
        )
        val activity = MainApplication.activityResumeFlow.first()
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                return
            } catch (e: Exception) {
                continue
            }
        }
    }
}
