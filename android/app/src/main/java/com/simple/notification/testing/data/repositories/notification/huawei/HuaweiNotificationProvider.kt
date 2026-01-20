package com.simple.notification.testing.data.repositories.notification.huawei

import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.NotificationProvider

@AutoService(NotificationProvider::class)
class HuaweiNotificationProvider : HuaweiProvider, NotificationProvider
