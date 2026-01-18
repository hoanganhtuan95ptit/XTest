package com.simple.notification.testing.data.repositories.notification

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.simple.notification.testing.MainApplication
import com.simple.notification.testing.data.repositories.notification.general.DefaultNotificationProvider
import java.util.ServiceLoader

interface NotificationProvider : ODEProvider {

    fun isGranted(packageName: String): Boolean {
        val context = MainApplication.share
        if (packageName == context.packageName) {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState = context.packageManager.checkPermission(
                    Manifest.permission.POST_NOTIFICATIONS,
                    packageName
                )
                if (permissionState != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }

            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(packageName, 0)
            }

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
            false
        }
    }

    suspend fun openNotificationSettings(packageName: String) {
        val context = MainApplication.share
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
            val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            detailIntent.data = Uri.parse("package:$packageName")
            detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(detailIntent)
        }
    }

    companion object {
        fun get(): NotificationProvider {
            val providers = ServiceLoader.load(NotificationProvider::class.java).toList()
            return providers.firstOrNull { it.accept() } ?: DefaultNotificationProvider()
        }
    }
}
