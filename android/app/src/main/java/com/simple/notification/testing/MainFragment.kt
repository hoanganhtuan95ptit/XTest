package com.simple.notification.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.simple.notification.testing.data.repositories.notification.DeviceIntegrityProvider
import com.simple.notification.testing.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val targetToken = result.contents
            showSendBottomSheet(targetToken)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        setupDeviceIntegrity()
        setupQuickScanButton()
//        setupAppListButton()
        setupFcmButton()
    }

    private fun setupDeviceIntegrity() {
        binding.tvDeviceIntegrity.text = DeviceIntegrityProvider.get().checkIsGenuine()
    }

    private fun setupQuickScanButton() {
        binding.btnQuickScan.setOnClickListener {
            val options = ScanOptions().apply {
                setCaptureActivity(CaptureActivityPortrait::class.java)
                setOrientationLocked(false)
                setBeepEnabled(true)
                setPrompt(getString(R.string.quick_scan_title))
            }
            barcodeLauncher.launch(options)
        }
    }

    private fun showSendBottomSheet(token: String) {
        val bottomSheet = SendNotificationBottomSheet(token)
        bottomSheet.show(parentFragmentManager, "QuickSendBottomSheet")
    }

    private fun setupAppListButton() {
        binding.btnFixOtherApps.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(AppListFragment())
        }
    }

    private fun setupFcmButton() {
        binding.btnFcmTest.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(FcmTestFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
