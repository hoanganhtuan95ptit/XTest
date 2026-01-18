package com.simple.notification.testing.data.repositories.notification.oppo

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.AutoStartUtils
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import kotlinx.coroutines.flow.first

class OppoAutoStartProvider : OppoProvider, AutoStartProvider {

    override fun isEnabled(packageName: String): Boolean {
        return AutoStartUtils.isEnabled(MainApplication.share, packageName) ?: false
    }

    override suspend fun openAutoStartSettings(packageName: String) {
        val intents = listOf(
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startupapp.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fusion.battery.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.athena", "com.coloros.athena.main.MainActivity"))
        )

        var success = false
        val activity = MainApplication.activityResumeFlow.first()
        
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                success = true
                break
            } catch (e: Exception) {
                continue
            }
        }

        if (!success) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
            } catch (e: Exception) { /* Ignore */ }
        }
    }
}
