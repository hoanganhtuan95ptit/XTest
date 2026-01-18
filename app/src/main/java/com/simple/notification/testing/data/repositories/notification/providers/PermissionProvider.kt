package com.simple.notification.testing.data.repositories.notification.providers

interface PermissionProvider {

    fun isGranted(packageName: String): Boolean
}