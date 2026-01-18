package com.simple.notification.testing.data.models

import android.graphics.drawable.Drawable

data class AppStatus(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val isNotificationGranted: Boolean,
    val isAutoStartEnabled: Boolean?, // null if not applicable/determinable
    val isIgnoringBatteryOptimizations: Boolean,
    val hasIssues: Boolean = !isNotificationGranted || isAutoStartEnabled == false || !isIgnoringBatteryOptimizations
)
