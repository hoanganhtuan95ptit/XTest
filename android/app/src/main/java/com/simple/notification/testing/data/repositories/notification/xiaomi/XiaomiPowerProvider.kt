package com.simple.notification.testing.data.repositories.notification.xiaomi

import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.PowerProvider

@AutoService(PowerProvider::class)
class XiaomiPowerProvider : XiaomiProvider, PowerProvider {
    // Sử dụng implementation mặc định từ PowerProvider để mở màn hình chi tiết ứng dụng
}
