package com.simple.notification.testing.data.repositories.notification

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class NotificationRepository {

    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val url = "https://us-central1-detect-translate-8.cloudfunctions.net/sendPushNotification"

    fun sendPushNotification(
        authIdToken: String,
        targetToken: String,
        message: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val json = JSONObject().apply {
            put("token", targetToken)
            put("title", "Test 🔔")
            put("body", message)
        }

        val body = json.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $authIdToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, "Error: ${response.code}")
                }
            }
        })
    }
}
