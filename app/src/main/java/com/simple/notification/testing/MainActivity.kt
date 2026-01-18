package com.simple.notification.testing

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import java.util.Locale

class MainActivity : FragmentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Quyền thông báo bị từ chối", Toast.LENGTH_SHORT).show()
        }
        updateButtonStates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNotificationButton()
        setupBatteryButton()
        setupAutoStartButton()
        setupAppListButton()
        setupFcmButton()
    }

    private fun setupNotificationButton() {
        val btn = findViewById<Button>(R.id.btnNotificationPermission)
        btn.setOnClickListener {
            if (NotificationUtils_Legacy.isGranted(this, packageName)) {
                Toast.makeText(this, "Thông báo đã được bật", Toast.LENGTH_SHORT).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    NotificationUtils_Legacy.openSettings(this, packageName)
                }
            }
        }
    }

    private fun setupBatteryButton() {
        val btn = findViewById<Button>(R.id.btnOpenBackgroundSettings)
        btn.setOnClickListener {
            if (PowerUtils.isIgnoringBatteryOptimizations(this, packageName)) {
                Toast.makeText(this, "Ứng dụng đã được phép chạy ngầm (Pin)", Toast.LENGTH_SHORT).show()
            } else {
                PowerUtils.openBatteryOptimizationSettings(this, packageName)
            }
        }
    }

    private fun setupAutoStartButton() {
        val btn = findViewById<Button>(R.id.btnOpenAutoStart)
        if (AutoStartUtils.isSettingAvailable()) {
            btn.visibility = View.VISIBLE
            btn.setOnClickListener {
                val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
                if (manufacturer.contains("oppo") || manufacturer.contains("realme")) {
                    showOppoGuidanceDialog {
                        AutoStartUtils.openSettings(this, packageName)
                    }
                } else {
                    if (AutoStartUtils.isEnabled(this, packageName) == true) {
                        Toast.makeText(this, "Tự khởi chạy có vẻ đã được bật", Toast.LENGTH_SHORT).show()
                    }
                    AutoStartUtils.openSettings(this, packageName)
                }
            }
        } else {
            btn.visibility = View.GONE
        }
    }

    private fun setupAppListButton() {
        findViewById<Button>(R.id.btnFixOtherApps).setOnClickListener {
            startActivity(Intent(this, AppListActivity::class.java))
        }
    }

    private fun setupFcmButton() {
        findViewById<Button>(R.id.btnFcmTest).setOnClickListener {
            startActivity(Intent(this, FcmTestActivity::class.java))
        }
    }

    private fun showOppoGuidanceDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Hướng dẫn Tự khởi chạy")
            .setMessage("Để ứng dụng hoạt động ổn định trên thiết bị Oppo/Realme, vui lòng thực hiện trong màn hình tiếp theo:\n\n1. Chọn mục 'Sử dụng pin' (Battery usage).\n2. Bật 'Cho phép hoạt động tự khởi chạy' (Allow auto-launch).\n3. Bật 'Cho phép hoạt động dưới nền' (Allow background activity).")
            .setPositiveButton("Đã hiểu và Mở") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Để sau", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
    }

    private fun updateButtonStates() {
        val btnNoti = findViewById<Button>(R.id.btnNotificationPermission)
        if (NotificationUtils_Legacy.isGranted(this, packageName)) {
            btnNoti.text = "✅ Thông báo đã bật"
            btnNoti.alpha = 0.6f
        } else {
            btnNoti.text = "Cấp quyền thông báo"
            btnNoti.alpha = 1.0f
        }

        val btnBattery = findViewById<Button>(R.id.btnOpenBackgroundSettings)
        if (PowerUtils.isIgnoringBatteryOptimizations(this, packageName)) {
            btnBattery.text = "✅ Đã cho phép chạy ngầm (Pin)"
            btnBattery.alpha = 0.6f
        } else {
            btnBattery.text = "Cho phép chạy ngầm (Pin)"
            btnBattery.alpha = 1.0f
        }

        val btnAutoStart = findViewById<Button>(R.id.btnOpenAutoStart)
        if (AutoStartUtils.isEnabled(this, packageName) == true) {
            btnAutoStart.text = "✅ Đã bật tự khởi chạy"
            btnAutoStart.alpha = 0.6f
        } else {
            btnAutoStart.text = "Bật tự động khởi chạy"
            btnAutoStart.alpha = 1.0f
        }
    }
}

object NotificationUtils_Legacy {
    fun isGranted(context: android.content.Context, packageName: String): Boolean {
        return androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    fun openSettings(context: android.content.Context, packageName: String) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
        }
        context.startActivity(intent)
    }
}
