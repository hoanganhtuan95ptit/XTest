package com.simple.notification.testing.data.repositories.notification.oppo

import android.os.Build
import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.DeviceIntegrityProvider
import com.simple.notification.testing.data.repositories.notification.general.DefaultDeviceIntegrityProvider

@AutoService(DeviceIntegrityProvider::class)
class OppoDeviceIntegrityProvider : OppoProvider, DeviceIntegrityProvider {

    override fun checkIsGenuine(): String {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val isChinaVersion = fingerprint.contains(".cn") || fingerprint.contains("china")

        return when {
            isChinaVersion -> "⚠️ Oppo/Realme China Domestic"
            else -> "✅ Oppo/Realme Genuine Global"
        }
    }
}
