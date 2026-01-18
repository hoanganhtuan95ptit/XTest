package com.simple.notification.testing.data.repositories.notification.oppo

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import kotlinx.coroutines.flow.first

class OppoPowerProvider : OppoProvider, PowerProvider {

    override suspend fun openBatteryOptimizationSettings(packageName: String) {
        try {
            // Vì đây là class dành riêng cho Oppo/Realme, thử mở trang đặc thù trước
            val intent = Intent().apply {
                component = ComponentName(
                    "com.coloros.oppoguardelf",
                    "com.coloros.powermanager.fusion.primary.PrimaryBatteryActivity"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            MainApplication.activityResumeFlow.first().startActivity(intent)
        } catch (e: Exception) {
            // Nếu lỗi (với các bản ColorOS khác), chuyển sang trang pin chung
            openGeneralBatterySettings()
        }
    }

    private suspend fun openGeneralBatterySettings() {
        try {
            // Tuân thủ chính sách Play Store bằng cách mở danh sách tối ưu hóa
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
