package com.simple.notification.testing

import android.Manifest
import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import com.simple.notification.testing.data.repositories.notification.general.DefaultNotificationProvider
import com.simple.notification.testing.databinding.FragmentFcmTestBinding
import kotlinx.coroutines.launch
import java.util.Locale

class FcmTestFragment : Fragment() {

    private var _binding: FragmentFcmTestBinding? = null
    private val binding get() = _binding!!
    private var targetToken: String? = null
    private var myToken: String? = null

    private val notificationProvider = DefaultNotificationProvider()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val msg = if (isGranted) getString(R.string.noti_permission_granted) else getString(R.string.noti_permission_denied)
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        updateButtonStates()
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            targetToken = result.contents
            binding.llTargetInfo.visibility = View.VISIBLE
            binding.tvTargetToken.text = targetToken
            showSendBottomSheet(targetToken!!)
        } else {
            Toast.makeText(requireContext(), getString(R.string.qr_scan_cancelled), Toast.LENGTH_SHORT).show()
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.updatePadding(top = systemBars.top)

            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupNotificationButton()
        setupBatteryButton()
        setupAutoStartButton()
        
        getMyFcmToken()

        binding.btnScanQr.setOnClickListener {
            val options = ScanOptions().apply {
                setCaptureActivity(CaptureActivityPortrait::class.java)
                setOrientationLocked(false)
                setBeepEnabled(true)
                setPrompt(getString(R.string.scan_qr_prompt))
            }
            barcodeLauncher.launch(options)
        }

        binding.btnTestSelf.setOnClickListener {
            if (myToken != null) {
                showSendBottomSheet(myToken!!)
            } else {
                Toast.makeText(requireContext(), getString(R.string.waiting_for_token), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSendBottomSheet(token: String) {
        val bottomSheet = SendNotificationBottomSheet(token)
        bottomSheet.show(parentFragmentManager, "SendNotificationBottomSheet")
    }

    private fun setupNotificationButton() {
        binding.btnNotificationPermission.setOnClickListener {
            if (notificationProvider.isGranted(requireContext().packageName)) {
                Toast.makeText(requireContext(), getString(R.string.noti_already_enabled), Toast.LENGTH_SHORT).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewLifecycleOwner.lifecycleScope.launch {
                        notificationProvider.openNotificationSettings(requireContext().packageName)
                    }
                }
            }
        }
    }

    private fun setupBatteryButton() {
        binding.btnOpenBackgroundSettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val provider = PowerProvider.get()
                val packageName = requireContext().packageName
                if (provider.isIgnoringBatteryOptimizations(packageName)) {
                    Toast.makeText(requireContext(), getString(R.string.bg_already_allowed), Toast.LENGTH_SHORT).show()
                } else {
                    provider.openBatteryOptimizationSettings(packageName)
                }
            }
        }
    }

    private fun setupAutoStartButton() {
        val provider = AutoStartProvider.get()
        if (provider.isAvailable()) {
            binding.btnOpenAutoStart.visibility = View.VISIBLE
            binding.btnOpenAutoStart.setOnClickListener {
                val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
                if (manufacturer.contains("oppo") || manufacturer.contains("realme")) {
                    showOppoGuidanceDialog {
                        viewLifecycleOwner.lifecycleScope.launch {
                            provider.openAutoStartSettings(requireContext().packageName)
                        }
                    }
                } else {
                    if (provider.isEnabled(requireContext().packageName) == true) {
                        Toast.makeText(requireContext(), getString(R.string.auto_start_already_enabled), Toast.LENGTH_SHORT).show()
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        provider.openAutoStartSettings(requireContext().packageName)
                    }
                }
            }
        } else {
            binding.btnOpenAutoStart.visibility = View.GONE
        }
    }

    private fun showOppoGuidanceDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.oppo_guidance_title))
            .setMessage(getString(R.string.oppo_guidance_message))
            .setPositiveButton(getString(R.string.understand_open)) { _, _ ->
                onConfirm()
            }
            .setNegativeButton(getString(R.string.later), null)
            .show()
    }

    private fun getMyFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", getString(R.string.token_fetch_failed), task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            myToken = token
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

    override fun onResume() {
        super.onResume()
        updateButtonStates()
    }

    private fun updateButtonStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            val packageName = requireContext().packageName
            if (notificationProvider.isGranted(packageName)) {
                binding.btnNotificationPermission.text = getString(R.string.noti_enabled_success)
                binding.btnNotificationPermission.alpha = 0.6f
            } else {
                binding.btnNotificationPermission.text = getString(R.string.grant_noti_permission)
                binding.btnNotificationPermission.alpha = 1.0f
            }

            val pProvider = PowerProvider.get()
            if (pProvider.isIgnoringBatteryOptimizations(packageName)) {
                binding.btnOpenBackgroundSettings.text = getString(R.string.background_allowed_success)
                binding.btnOpenBackgroundSettings.alpha = 0.6f
            } else {
                binding.btnOpenBackgroundSettings.text = getString(R.string.allow_background)
                binding.btnOpenBackgroundSettings.alpha = 1.0f
            }

            val asProvider = AutoStartProvider.get()
            if (asProvider.isEnabled(packageName) == true) {
                binding.btnOpenAutoStart.text = getString(R.string.auto_start_enabled_success)
                binding.btnOpenAutoStart.alpha = 0.6f
            } else {
                binding.btnOpenAutoStart.text = getString(R.string.enable_auto_start)
                binding.btnOpenAutoStart.alpha = 1.0f
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
