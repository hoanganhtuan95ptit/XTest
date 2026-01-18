package com.simple.notification.testing

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.Locale

object AutoStartUtils {

    fun isSettingAvailable(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        val supported = listOf("xiaomi", "oppo", "realme", "vivo", "huawei", "honor")
        return supported.any { manufacturer.contains(it) }
    }

    /**
     * Thử kiểm tra trạng thái tự khởi chạy.
     * Trả về true nếu đã bật, false nếu chưa, null nếu không thể xác định (trên các dòng máy không hỗ trợ check)
     */
    fun isEnabled(context: Context, packageName: String): Boolean? {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        
        // Hiện tại chỉ Xiaomi và một số bản ColorOS/FuntouchOS cũ hỗ trợ check qua AppOps 10008
        val isSupportedBrand = manufacturer.contains("xiaomi") || 
                               manufacturer.contains("oppo") || 
                               manufacturer.contains("realme") ||
                               manufacturer.contains("vivo")

        if (!isSupportedBrand) return null

        return try {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val method = appOpsManager.javaClass.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            // 10008 là mã OP_AUTO_START nội bộ phổ biến trên các máy Trung Quốc
            val result = method.invoke(
                appOpsManager,
                10008,
                android.os.Process.myUid(),
                packageName
            ) as Int
            result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            null
        }
    }

    fun openSettings(context: Context, packageName: String) {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        val intents = mutableListOf<Intent>()

        when {
            manufacturer.contains("xiaomi") -> {
                intents.add(Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")))
            }
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                intents.add(Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")))
                intents.add(Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")))
                intents.add(Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startupapp.StartupAppListActivity")))
                intents.add(Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")))
                intents.add(Intent().setComponent(ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fusion.battery.StartupAppListActivity")))
                intents.add(Intent().setComponent(ComponentName("com.coloros.athena", "com.coloros.athena.main.MainActivity")))
            }
            manufacturer.contains("vivo") -> {
                intents.add(Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")))
                intents.add(Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")))
            }
            manufacturer.contains("huawei") -> {
                intents.add(Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")))
                intents.add(Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")))
            }
        }

        var success = false
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
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
                context.startActivity(intent)
            } catch (e: Exception) { /* Ignore */ }
        }
    }
}
