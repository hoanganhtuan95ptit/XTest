package com.simple.notification.testing.data.repositories.notification

interface ODEProvider {

    suspend fun accept(): Boolean
}