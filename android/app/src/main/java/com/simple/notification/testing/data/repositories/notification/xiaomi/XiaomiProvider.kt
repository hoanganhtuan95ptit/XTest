package com.simple.notification.testing.data.repositories.notification.xiaomi

import android.os.Build
import com.simple.notification.testing.data.repositories.notification.ODEProvider
import java.util.Locale

interface XiaomiProvider : ODEProvider {
    override fun accept(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        return manufacturer.contains("xiaomi")
    }
}
