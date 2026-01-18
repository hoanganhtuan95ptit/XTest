package com.simple.notification.testing.data.repositories.notification

import android.content.Context
import android.os.PowerManager
import com.simple.notification.testing.MainApplication
import kotlinx.coroutines.flow.first

interface PowerProvider : ODEProvider {

    suspend fun isIgnoringBatteryOptimizations(packageName: String): Boolean {
        val powerManager = MainApplication.activityResumeFlow.first().getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    suspend fun openBatteryOptimizationSettings(packageName: String)
}