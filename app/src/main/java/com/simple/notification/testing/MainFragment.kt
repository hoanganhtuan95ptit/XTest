package com.simple.notification.testing

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import com.simple.notification.testing.data.repositories.notification.general.DefaultNotificationProvider
import com.simple.notification.testing.databinding.FragmentMainBinding
import kotlinx.coroutines.launch
import java.util.Locale

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val notificationProvider = DefaultNotificationProvider()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
        updateButtonStates()
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

        setupNotificationButton()
        setupBatteryButton()
        setupAutoStartButton()
        setupAppListButton()
        setupFcmButton()
    }

    private fun setupNotificationButton() {
        binding.btnNotificationPermission.setOnClickListener {
            if (notificationProvider.isGranted(requireContext().packageName)) {
                Toast.makeText(requireContext(), "Notifications are already enabled", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), "App is already allowed to run in background", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(requireContext(), "Auto-start seems to be enabled", Toast.LENGTH_SHORT).show()
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

    private fun showOppoGuidanceDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Auto-start Guidance")
            .setMessage("To ensure the app works stably on Oppo/Realme devices, please perform the following in the next screen:\n\n1. Select 'Battery usage'.\n2. Enable 'Allow auto-launch'.\n3. Enable 'Allow background activity'.")
            .setPositiveButton("I understand & Open") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
    }

    private fun updateButtonStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (notificationProvider.isGranted(requireContext().packageName)) {
                binding.btnNotificationPermission.text = "✅ Notifications enabled"
                binding.btnNotificationPermission.alpha = 0.6f
            } else {
                binding.btnNotificationPermission.text = "Grant notification permission"
                binding.btnNotificationPermission.alpha = 1.0f
            }

            val pProvider = PowerProvider.get()
            if (pProvider.isIgnoringBatteryOptimizations(requireContext().packageName)) {
                binding.btnOpenBackgroundSettings.text = "✅ Background allowed (Battery)"
                binding.btnOpenBackgroundSettings.alpha = 0.6f
            } else {
                binding.btnOpenBackgroundSettings.text = "Allow background (Battery)"
                binding.btnOpenBackgroundSettings.alpha = 1.0f
            }

            val asProvider = AutoStartProvider.get()
            if (asProvider.isEnabled(requireContext().packageName) == true) {
                binding.btnOpenAutoStart.text = "✅ Auto-start enabled"
                binding.btnOpenAutoStart.alpha = 0.6f
            } else {
                binding.btnOpenAutoStart.text = "Enable Auto-start"
                binding.btnOpenAutoStart.alpha = 1.0f
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
