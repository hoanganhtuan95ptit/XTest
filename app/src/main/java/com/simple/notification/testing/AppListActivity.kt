package com.simple.notification.testing

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.simple.notification.testing.data.models.AppStatus
import com.simple.notification.testing.data.repositories.AppStatusRepository
import com.simple.notification.testing.databinding.ActivityAppListBinding

class AppListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppListBinding
    private lateinit var adapter: AppAdapter
    private lateinit var repository: AppStatusRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Sửa lỗi thông báo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        repository = AppStatusRepository(this)
        
        setupRecyclerView()
        loadApps()
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter(emptyList()) { app ->
            showFixOptionsDialog(app)
        }
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
    }

    private fun loadApps() {
        val apps = repository.getInstalledApps()
        adapter.updateData(apps)
    }

    private fun showFixOptionsDialog(app: AppStatus) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        // 1. Thông tin ứng dụng (Mặc định)
        options.add("Thông tin ứng dụng (Cài đặt chung)")
        actions.add { openAppDetails(app.packageName) }

        // 2. Sửa quyền thông báo
        if (!app.isNotificationGranted) {
            options.add("Sửa lỗi: Cấp quyền thông báo")
            actions.add { NotificationUtils_Legacy.openSettings(this, app.packageName) }
        }

        // 3. Sửa chạy ngầm (Pin)
        if (!app.isIgnoringBatteryOptimizations) {
            options.add("Sửa lỗi: Cho phép chạy nền (Pin)")
            actions.add { PowerUtils.openBatteryOptimizationSettings(this, app.packageName) }
        }

        // 4. Sửa Tự khởi chạy
        if (app.isAutoStartEnabled == false) {
            options.add("Sửa lỗi: Bật tự động khởi chạy")
            actions.add { AutoStartUtils.openSettings(this, app.packageName) }
        }

        AlertDialog.Builder(this)
            .setTitle(app.appName)
            .setItems(options.toTypedArray()) { _, which ->
                actions[which].invoke()
            }
            .setNegativeButton("Đóng", null)
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
        // Cập nhật lại danh sách khi quay lại từ cài đặt
        loadApps()
    }
}
