package com.simple.notification.testing.data.repositories.notification.oppo

import android.os.Build
import com.simple.notification.testing.data.repositories.notification.ODEProvider
import java.util.Locale

interface OppoProvider : ODEProvider {

    override suspend fun accept(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        return manufacturer.contains("oppo") || manufacturer.contains("realme")
    }
}
