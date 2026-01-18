package com.simple.notification.testing.data.repositories.notification.oppo

import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.NotificationProvider

@AutoService(NotificationProvider::class)
class OppoNotificationProvider : OppoProvider, NotificationProvider
