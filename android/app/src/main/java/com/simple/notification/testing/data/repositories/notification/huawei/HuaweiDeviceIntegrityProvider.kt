package com.simple.notification.testing.data.repositories.notification.huawei

import android.os.Build
import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.DeviceIntegrityProvider
import com.simple.notification.testing.data.repositories.notification.general.DefaultDeviceIntegrityProvider

@AutoService(DeviceIntegrityProvider::class)
class HuaweiDeviceIntegrityProvider : HuaweiProvider, DeviceIntegrityProvider {

    override fun checkIsGenuine(): String {
        val fingerprint = Build.FINGERPRINT.lowercase()
        // Huawei nội địa thường không có Google Play Services và fingerprint chứa .cn
        val isChinaVersion = fingerprint.contains(".cn") || fingerprint.contains("china")

        return when {
            isChinaVersion -> "⚠️ Huawei China Domestic (HarmonyOS/EMUI CN)"
            else -> "✅ Huawei Genuine Global"
        }
    }
}
