package com.simple.notification.testing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.simple.notification.testing.data.repositories.notification.NotificationRepository
import com.simple.notification.testing.databinding.LayoutSendNotificationSheetBinding

class SendNotificationBottomSheet(
    private val targetToken: String
) : BottomSheetDialogFragment() {

    private var _binding: LayoutSendNotificationSheetBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val notificationRepository = NotificationRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutSendNotificationSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTargetInfo.text = "Gửi tới: ${targetToken.take(20)}..."

        binding.btnConfirmSend.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập lời nhắn", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            binding.btnConfirmSend.isEnabled = false
            ensureAuthAndSend(message)
        }
    }

    private fun ensureAuthAndSend(message: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    getIdTokenAndSend(message)
                } else {
                    binding.btnConfirmSend.isEnabled = true
                    Toast.makeText(context, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            getIdTokenAndSend(message)
        }
    }

    private fun getIdTokenAndSend(message: String) {
        val ctx = context ?: return
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result.token
                if (idToken != null) {
                    notificationRepository.sendPushNotification(ctx, idToken, targetToken, message) { success, error ->
                        activity?.runOnUiThread {
                            binding.btnConfirmSend.isEnabled = true
                            if (success) {
                                Toast.makeText(context, "Đã gửi thông báo thành công!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, error ?: "Lỗi không xác định", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                binding.btnConfirmSend.isEnabled = true
                Toast.makeText(context, "Lấy Auth Token thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
