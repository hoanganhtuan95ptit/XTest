package com.simple.notification.testing.data.repositories.notification.xiaomi

import android.content.ComponentName
import android.content.Intent
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.AutoStartUtils
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import kotlinx.coroutines.flow.first

class XiaomiAutoStartProvider : XiaomiProvider, AutoStartProvider {

    override fun isEnabled(packageName: String): Boolean {
        return AutoStartUtils.isEnabled(MainApplication.share, packageName) ?: false
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
