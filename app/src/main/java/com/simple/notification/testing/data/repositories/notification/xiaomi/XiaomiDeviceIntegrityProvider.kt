package com.simple.notification.testing.data.repositories.notification.xiaomi

import android.os.Build
import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.DeviceIntegrityProvider

@AutoService(DeviceIntegrityProvider::class)
class XiaomiDeviceIntegrityProvider : XiaomiProvider, DeviceIntegrityProvider {

    override fun checkIsGenuine(): String {
        val buildDesc = Build.DISPLAY.lowercase()
        val miuiVersion = getSystemProperty("ro.miui.ui.version.name")

        val isChinaVersion = miuiVersion.isNotEmpty() && !buildDesc.contains("global")

        return when {
            isChinaVersion -> "⚠️ Xiaomi China Domestic (No Global ROM)"
            else -> "✅ Xiaomi Genuine Global"
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java)
            get.invoke(c, key) as String
        } catch (e: Exception) {
            ""
        }
    }
}
