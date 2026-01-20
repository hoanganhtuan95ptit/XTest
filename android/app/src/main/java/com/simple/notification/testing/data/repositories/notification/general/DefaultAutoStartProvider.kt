package com.simple.notification.testing.data.repositories.notification.general

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import kotlinx.coroutines.flow.first

class DefaultAutoStartProvider : DefaultProvider, AutoStartProvider {

    override fun isAvailable(): Boolean = false

    override fun isEnabled(packageName: String): Boolean? {
        // Mặc định các máy không phải hãng Trung Quốc thường không chặn tự khởi chạy
        return true
    }

    override suspend fun openAutoStartSettings(packageName: String) {
        val activity = MainApplication.activityResumeFlow.first()
        try {
            // Mở trang thông tin ứng dụng làm dự phòng
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
