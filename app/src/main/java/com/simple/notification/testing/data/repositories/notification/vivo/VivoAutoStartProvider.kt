package com.simple.notification.testing.data.repositories.notification.vivo

import android.content.ComponentName
import android.content.Intent
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.AutoStartUtils
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import kotlinx.coroutines.flow.first

class VivoAutoStartProvider : VivoProvider, AutoStartProvider {

    override fun isEnabled(packageName: String): Boolean {
        return AutoStartUtils.isEnabled(MainApplication.share, packageName) ?: false
    }

    override suspend fun openAutoStartSettings(packageName: String) {
        val intents = listOf(
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"))
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
