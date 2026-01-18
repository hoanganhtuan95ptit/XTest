package com.simple.notification.testing.data.repositories.notification.xiaomi

import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.NotificationProvider

@AutoService(NotificationProvider::class)
class XiaomiNotificationProvider : XiaomiProvider, NotificationProvider
