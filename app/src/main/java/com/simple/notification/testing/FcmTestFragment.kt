package com.simple.notification.testing

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.simple.notification.testing.databinding.FragmentFcmTestBinding
import okhttp3.*
import java.io.IOException

class FcmTestFragment : Fragment() {

    private var _binding: FragmentFcmTestBinding? = null
    private val binding get() = _binding!!
    private var targetToken: String? = null
    private var myToken: String? = null

    private val auth = FirebaseAuth.getInstance()

    init {
        System.loadLibrary("notitesting")
    }

    /**
     * Interface kết quả không chứa đối tượng Call để đảm bảo bảo mật
     */
    @Keep
    interface OnPushResult {
        fun onResult(success: Boolean, message: String)
    }

    /**
     * Lớp Bridge trung gian xử lý OkHttp Callback và chỉ trả về kết quả tối giản cho Interface
     */
    @Keep
    private class CallbackBridge(private val listener: OnPushResult) : Callback {

        override fun onFailure(call: Call, e: IOException) {
            listener.onResult(false, "Gửi thất bại: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                listener.onResult(true, "Đã gửi thông báo thành công (Native Secure)!")
            } else {
                listener.onResult(false, "Lỗi API: ${response.code}")
            }
        }
    }

    private external fun sendNotificationNative(token: String, message: String, authIdToken: String, callback: OnPushResult)

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Đã hủy quét", Toast.LENGTH_LONG).show()
        } else {
            targetToken = result.contents
            binding.tvTargetToken.text = targetToken
            binding.tvTargetToken.visibility = View.VISIBLE
            binding.tvTargetTokenLabel.visibility = View.VISIBLE
            binding.btnSendNotification.isEnabled = true
            Toast.makeText(requireContext(), "Đã lấy được token từ QR!", Toast.LENGTH_SHORT).show()
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

            val tokenToSend = targetToken ?: myToken
            if (tokenToSend == null) {
                Toast.makeText(requireContext(), "Chưa có token để gửi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            ensureAuthAndSend(message, tokenToSend)
        }
    }

    private fun getMyFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Lấy token thất bại", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            myToken = token
            binding.tvMyToken.text = token
            binding.btnSendNotification.isEnabled = true
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

    private fun ensureAuthAndSend(message: String, token: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    getIdTokenAndSend(message, token)
                } else {
                    Toast.makeText(requireContext(), "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            getIdTokenAndSend(message, token)
        }
    }

    private fun getIdTokenAndSend(message: String, token: String) {
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result.token
                if (idToken != null) {
                    sendNotificationViaNative(token, message, idToken)
                }
            } else {
                Toast.makeText(requireContext(), "Lấy Auth Token thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendNotificationViaNative(token: String, message: String, authIdToken: String) {
        val resultListener = object : OnPushResult {
            override fun onResult(success: Boolean, message: String) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        sendNotificationNative(token, message, authIdToken, resultListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
