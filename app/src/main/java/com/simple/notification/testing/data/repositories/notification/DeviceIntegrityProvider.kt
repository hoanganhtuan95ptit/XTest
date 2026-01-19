package com.simple.notification.testing.data.repositories.notification

import com.simple.notification.testing.data.repositories.notification.general.DefaultDeviceIntegrityProvider
import java.util.ServiceLoader

interface DeviceIntegrityProvider : ODEProvider {

    fun checkIsGenuine(): String

    companion object {
        fun get(): DeviceIntegrityProvider {
            val providers = ServiceLoader.load(DeviceIntegrityProvider::class.java).toList()
            return providers.firstOrNull { it.accept() } ?: DefaultDeviceIntegrityProvider()
        }
    }
}
