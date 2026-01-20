package com.simple.notification.testing

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simple.notification.testing.data.models.AppStatus
import com.simple.notification.testing.data.repositories.AppStatusRepository
import com.simple.notification.testing.data.repositories.notification.AutoStartProvider
import com.simple.notification.testing.data.repositories.notification.PowerProvider
import com.simple.notification.testing.data.repositories.notification.general.DefaultNotificationProvider
import com.simple.notification.testing.databinding.FragmentAppListBinding
import kotlinx.coroutines.launch

class AppListFragment : Fragment() {

    private var _binding: FragmentAppListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AppAdapter
    private lateinit var repository: AppStatusRepository
    
    private val notificationProvider = DefaultNotificationProvider()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Set padding top cho toolbar thay vì root view
            binding.toolbar.updatePadding(top = systemBars.top)
            
            // Các padding khác giữ cho root view
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

        repository = AppStatusRepository(requireContext())
        
        setupRecyclerView()
        loadApps()
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter(emptyList()) { app ->
            showFixOptionsDialog(app)
        }
        binding.rvApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApps.adapter = adapter
    }

    private fun loadApps() {
        viewLifecycleOwner.lifecycleScope.launch {
            val apps = repository.getInstalledApps()
            adapter.updateData(apps)
        }
    }

    private fun showFixOptionsDialog(app: AppStatus) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        options.add("App Info (General Settings)")
        actions.add { openAppDetails(app.packageName) }

        if (!app.isNotificationGranted) {
            options.add("Fix: Grant notification permission")
            actions.add {
                viewLifecycleOwner.lifecycleScope.launch {
                    notificationProvider.openNotificationSettings(app.packageName)
                }
            }
        }

        if (!app.isIgnoringBatteryOptimizations) {
            options.add("Fix: Allow background (Battery)")
            actions.add {
                viewLifecycleOwner.lifecycleScope.launch {
                    PowerProvider.get().openBatteryOptimizationSettings(app.packageName)
                }
            }
        }

        if (app.isAutoStartEnabled == false) {
            options.add("Fix: Enable Auto-start")
            actions.add {
                viewLifecycleOwner.lifecycleScope.launch {
                    AutoStartProvider.get().openAutoStartSettings(app.packageName)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(app.appName)
            .setItems(options.toTypedArray()) { _, which ->
                actions[which].invoke()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun openAppDetails(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
//        loadApps()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
