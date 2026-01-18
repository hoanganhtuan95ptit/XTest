package com.simple.notification.testing.data.repositories.notification.huawei

import android.os.Build
import com.simple.notification.testing.data.repositories.notification.ODEProvider
import java.util.Locale

interface HuaweiProvider : ODEProvider {
    override suspend fun accept(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        return manufacturer.contains("huawei") || manufacturer.contains("honor")
    }
}
