package com.simple.notification.testing.data.repositories.notification.vivo

import android.os.Build
import com.simple.notification.testing.data.repositories.notification.ODEProvider
import java.util.Locale

interface VivoProvider : ODEProvider {
    override suspend fun accept(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        return manufacturer.contains("vivo")
    }
}
