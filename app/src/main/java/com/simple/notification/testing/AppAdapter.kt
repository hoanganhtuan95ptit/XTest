package com.simple.notification.testing

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simple.notification.testing.data.models.AppStatus
import com.simple.notification.testing.databinding.ItemAppStatusBinding

class AppAdapter(
    private var apps: List<AppStatus>,
    private val onItemClick: (AppStatus) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    fun updateData(newApps: List<AppStatus>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount() = apps.size

    inner class AppViewHolder(private val binding: ItemAppStatusBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: AppStatus) {
            binding.tvAppName.text = app.appName
            binding.tvAppPackage.text = app.packageName
            binding.ivAppIcon.setImageDrawable(app.icon)

            binding.tvNotiStatus.apply {
                text = "Thông báo: ${if (app.isNotificationGranted) "Bật" else "Tắt"}"
                setTextColor(if (app.isNotificationGranted) Color.GREEN else Color.RED)
            }

            binding.tvPowerStatus.apply {
                text = "Chạy ngầm: ${if (app.isIgnoringBatteryOptimizations) "Bật" else "Tắt"}"
                setTextColor(if (app.isIgnoringBatteryOptimizations) Color.GREEN else Color.RED)
            }

            if (app.isAutoStartEnabled != null) {
                binding.tvAutoStartStatus.apply {
                    visibility = View.VISIBLE
                    text = "Tự khởi chạy: ${if (app.isAutoStartEnabled) "Bật" else "Tắt"}"
                    setTextColor(if (app.isAutoStartEnabled) Color.GREEN else Color.RED)
                }
            } else {
                binding.tvAutoStartStatus.visibility = View.GONE
            }

            binding.ivStatusWarning.visibility = if (app.hasIssues) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onItemClick(app) }
        }
    }
}
