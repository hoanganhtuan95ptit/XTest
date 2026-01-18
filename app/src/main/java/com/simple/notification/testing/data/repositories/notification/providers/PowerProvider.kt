package com.simple.notification.testing.data.repositories.notification.providers

interface PowerProvider {

    fun isIgnoringBatteryOptimizations(packageName: String): Boolean

    fun openBatteryOptimizationSettings(packageName: String)
}