package com.simple.notification.testing.data.repositories.notification

import androidx.annotation.Keep
import okhttp3.*


class NotificationRepository {

    init {
        System.loadLibrary("notitesting")
    }

    /**
     * Interface kết quả tối giản để Native gọi ngược lại
     */
    @Keep
    interface OnPushResult {
        fun onResult(success: Boolean, message: String)
    }

    private external fun sendPushNotificationNative(
        authIdToken: String,
        targetToken: String,
        message: String,
        callback: OnPushResult
    )

    fun sendPushNotification(
        authIdToken: String,
        targetToken: String,
        message: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val resultListener = object : OnPushResult {
            override fun onResult(success: Boolean, message: String) {
                callback(success, if (success) null else message)
            }
        }
        sendPushNotificationNative(authIdToken, targetToken, message, resultListener)
    }
}
