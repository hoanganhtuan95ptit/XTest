package com.simple.notification.testing.data.repositories.notification.vivo

import android.os.Build
import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.DeviceIntegrityProvider

@AutoService(DeviceIntegrityProvider::class)
class VivoDeviceIntegrityProvider : VivoProvider, DeviceIntegrityProvider {

    override fun checkIsGenuine(): String {
        val fingerprint = Build.FINGERPRINT.lowercase()
        // Kiểm tra dấu hiệu ROM nội địa Vivo (thường có .cn hoặc fingerprint chứa china)
        val isChinaVersion = fingerprint.contains(".cn") || fingerprint.contains("china")

        return when {
            isChinaVersion -> "⚠️ Vivo China Domestic (OriginOS/Funtouch CN)"
            else -> "✅ Vivo Genuine Global"
        }
    }
}
