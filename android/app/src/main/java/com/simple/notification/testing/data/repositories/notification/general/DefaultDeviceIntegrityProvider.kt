package com.simple.notification.testing.data.repositories.notification.general

import android.os.Build
import com.simple.notification.testing.data.repositories.notification.DeviceIntegrityProvider
import java.io.File

open class DefaultDeviceIntegrityProvider : DefaultProvider, DeviceIntegrityProvider {

    override fun checkIsGenuine(): String {
        val isRooted = isRooted()
        val isEmulator = isEmulator()
        val hasCustomRom = Build.TAGS != null && Build.TAGS.contains("test-keys")

        return when {
            isEmulator -> "❌ Emulator (Not genuine device)"
            isRooted -> "⚠️ Rooted (Modified system)"
            hasCustomRom -> "⚠️ Custom ROM (Possible modification)"
            else -> "✅ Genuine Global/International device"
        }
    }

    protected fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    protected fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}
