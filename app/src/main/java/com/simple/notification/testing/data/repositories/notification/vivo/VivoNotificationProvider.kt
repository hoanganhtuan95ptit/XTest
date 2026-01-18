package com.simple.notification.testing.data.repositories.notification.vivo

import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.NotificationProvider

@AutoService(NotificationProvider::class)
class VivoNotificationProvider : VivoProvider, NotificationProvider
