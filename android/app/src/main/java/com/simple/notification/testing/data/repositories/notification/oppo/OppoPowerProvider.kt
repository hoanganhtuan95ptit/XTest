package com.simple.notification.testing.data.repositories.notification.oppo

import com.google.auto.service.AutoService
import com.simple.notification.testing.data.repositories.notification.PowerProvider

@AutoService(PowerProvider::class)
class OppoPowerProvider : OppoProvider, PowerProvider {
    // Sử dụng implementation mặc định từ PowerProvider để mở màn hình chi tiết ứng dụng
}
