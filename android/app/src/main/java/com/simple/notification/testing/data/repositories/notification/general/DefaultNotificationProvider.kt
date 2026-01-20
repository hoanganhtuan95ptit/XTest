package com.simple.notification.testing.data.repositories.notification.general

import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.NotificationProvider

@AutoService(NotificationProvider::class)
class DefaultNotificationProvider : DefaultProvider, NotificationProvider
