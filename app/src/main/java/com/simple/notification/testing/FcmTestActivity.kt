package com.simple.notification.testing

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.simple.notification.testing.databinding.ActivityFcmTestBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class FcmTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFcmTestBinding
    private var targetToken: String? = null

    // CHÚ Ý: Gửi FCM trực tiếp từ App yêu cầu Server Key (Legacy) hoặc OAuth2.
    // Đây là cách làm để demo, thực tế nên gọi qua Server của bạn.
    private val FCM_SERVER_KEY = "YOUR_SERVER_KEY_HERE"

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Đã hủy quét", Toast.LENGTH_LONG).show()
        } else {
            targetToken = result.contents
            binding.tvTargetToken.text = targetToken
            binding.tvTargetToken.visibility = View.VISIBLE
            binding.tvTargetTokenLabel.visibility = View.VISIBLE
            binding.btnSendNotification.isEnabled = true
            Toast.makeText(this, "Đã lấy được token!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFcmTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getMyFcmToken()

        binding.btnScanQr.setOnClickListener {
            barcodeLauncher.launch(com.journeyapps.barcodescanner.ScanOptions())
        }

        binding.btnSendNotification.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendNotification(targetToken!!, message)
        }
    }

    private fun getMyFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Lấy token thất bại", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            binding.tvMyToken.text = token
            generateQrCode(token)
        }
    }

    private fun generateQrCode(text: String) {
        val writer = MultiFormatWriter()
        try {
            val matrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val encoder = BarcodeEncoder()
            val bitmap: Bitmap = encoder.createBitmap(matrix)
            binding.ivQrCode.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun sendNotification(token: String, message: String) {
        if (FCM_SERVER_KEY == "YOUR_SERVER_KEY_HERE") {
            Toast.makeText(this, "Vui lòng cấu hình FCM_SERVER_KEY trong code", Toast.LENGTH_LONG).show()
            return
        }

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val json = JSONObject().apply {
            put("to", token)
            val notification = JSONObject().apply {
                put("title", "Test FCM")
                put("body", message)
                put("sound", "default")
            }
            put("notification", notification)
            val data = JSONObject().apply {
                put("message", message)
            }
            put("data", data)
        }

        val body = json.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .post(body)
            .addHeader("Authorization", "key=$FCM_SERVER_KEY")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@FcmTestActivity, "Gửi thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@FcmTestActivity, "Đã gửi thông báo!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@FcmTestActivity, "Lỗi: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
