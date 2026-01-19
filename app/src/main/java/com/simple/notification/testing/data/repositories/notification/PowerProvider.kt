package com.simple.notification.testing.data.repositories.notification

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import com.simple.notification.testing.MainApplication
import kotlinx.coroutines.flow.first
import java.util.ServiceLoader

interface PowerProvider : ODEProvider {

    suspend fun isIgnoringBatteryOptimizations(packageName: String): Boolean {
        val activity = MainApplication.activityResumeFlow.first()
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    /**
     * Mở màn hình chi tiết ứng dụng (App Info) để người dùng tự cấu hình chạy ngầm/pin
     */
    suspend fun openBatteryOptimizationSettings(packageName: String) {
        val activity = MainApplication.activityResumeFlow.first()
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback nếu không mở được App Info
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
            } catch (e2: Exception) { /* Ignore */ }
        }
    }

    companion object {
        suspend fun get(): PowerProvider {
            val providers = ServiceLoader.load(PowerProvider::class.java).toList()
            return providers.firstOrNull { it.accept() } ?: providers.first { it is com.simple.notification.testing.data.repositories.notification.general.DefaultPowerProvider }
        }
    }
}
