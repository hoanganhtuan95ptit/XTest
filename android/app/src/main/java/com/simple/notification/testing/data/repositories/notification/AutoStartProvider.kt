package com.simple.notification.testing.data.repositories.notification

import com.simple.notification.testing.data.repositories.notification.general.DefaultAutoStartProvider
import java.util.ServiceLoader

interface AutoStartProvider : ODEProvider {

    /**
     * Trả về true nếu máy hỗ trợ cài đặt tự khởi chạy
     */
    fun isAvailable(): Boolean

    /**
     * Trả về true nếu đã bật, false nếu chưa, null nếu không thể xác định
     */
    fun isEnabled(packageName: String): Boolean?

    suspend fun openAutoStartSettings(packageName: String)

    companion object {
        fun get(): AutoStartProvider {
            val providers = ServiceLoader.load(AutoStartProvider::class.java).toList()
            return providers.firstOrNull { it.accept() } ?: DefaultAutoStartProvider()
        }
    }
}
