package com.simple.notification.testing.data.repositories.notification

interface AutoStartProvider : ODEProvider {

    fun isEnabled(packageName: String): Boolean

    suspend fun openAutoStartSettings(packageName: String)
}
