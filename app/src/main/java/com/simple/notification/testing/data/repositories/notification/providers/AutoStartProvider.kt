package com.simple.notification.testing.data.repositories.notification.providers

interface AutoStartProvider {

    fun isEnabled(packageName: String): Boolean
}