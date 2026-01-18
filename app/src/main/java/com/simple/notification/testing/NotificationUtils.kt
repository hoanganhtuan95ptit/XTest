package com.simple.notification.testing

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {

    /**
     * Kiểm tra quyền thông báo cho một package bất kỳ.
     * Hỗ trợ cả quyền Runtime (Android 13+) và trạng thái Bật/Tắt trong cài đặt.
     */
    fun isGranted(context: Context, packageName: String): Boolean {
        // 1. Nếu là package của chính mình, dùng API chính thức (An toàn và chính xác nhất)
        if (packageName == context.packageName) {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

        // 2. Kiểm tra cho ứng dụng khác
        return try {
            // A. Kiểm tra Quyền Runtime (chỉ có từ Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState = context.packageManager.checkPermission(
                    Manifest.permission.POST_NOTIFICATIONS,
                    packageName
                )
                if (permissionState != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }

            // B. Kiểm tra trạng thái Bật/Tắt trong cài đặt hệ thống (AppOps)
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(packageName, 0)
            }

            // Dùng trực tiếp chuỗi "android:post_notification" để tránh lỗi unresolved reference
            val opStr = "android:post_notification"

            val method = appOpsManager.javaClass.getMethod(
                "checkOpNoThrow",
                String::class.java,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val mode = method.invoke(appOpsManager, opStr, appInfo.uid, packageName) as Int
            mode == AppOpsManager.MODE_ALLOWED

        } catch (e: Exception) {
            // Nếu không tìm thấy app hoặc gặp lỗi bảo mật, trả về false
            false
        }
    }

    /**
     * Mở cài đặt thông báo cho một ứng dụng cụ thể.
     * Hỗ trợ đa phiên bản Android.
     */
    fun openSettings(context: Context, packageName: String) {
        val intent = Intent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            else -> {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback về trang chi tiết ứng dụng nếu không mở được trang thông báo
            val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            detailIntent.data = Uri.parse("package:$packageName")
            detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(detailIntent)
        }
    }
}
