package com.simple.notification.testing

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.messaging.FirebaseMessaging
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.simple.notification.testing.databinding.FragmentFcmTestBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class FcmTestFragment : Fragment() {

    private var _binding: FragmentFcmTestBinding? = null
    private val binding get() = _binding!!
    private var targetToken: String? = null

    private val FCM_SERVER_KEY = "YOUR_SERVER_KEY_HERE"

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Đã hủy quét", Toast.LENGTH_LONG).show()
        } else {
            targetToken = result.contents
            binding.tvTargetToken.text = targetToken
            binding.tvTargetToken.visibility = View.VISIBLE
            binding.tvTargetTokenLabel.visibility = View.VISIBLE
            binding.btnSendNotification.isEnabled = true
            Toast.makeText(requireContext(), "Đã lấy được token!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFcmTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getMyFcmToken()

        binding.btnScanQr.setOnClickListener {
            val options = ScanOptions().apply {
                setCaptureActivity(CaptureActivityPortrait::class.java)
                setOrientationLocked(false)
                setBeepEnabled(true)
                setPrompt("Quét mã QR Token")
            }
            barcodeLauncher.launch(options)
        }

        binding.btnSendNotification.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Vui lòng cấu hình FCM_SERVER_KEY trong code", Toast.LENGTH_LONG).show()
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
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Gửi thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Đã gửi thông báo!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Lỗi: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
