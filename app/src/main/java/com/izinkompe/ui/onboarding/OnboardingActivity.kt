package com.izinkompe.ui.onboarding

import android.Manifest
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.izinkompe.IzinKompeApp
import com.izinkompe.databinding.ActivityOnboardingBinding
import com.izinkompe.receiver.AdminReceiver
import com.izinkompe.ui.MainActivity
import kotlinx.coroutines.launch

/**
 * Onboarding activity that guides the user through granting all required permissions:
 * - Usage Access (PACKAGE_USAGE_STATS)
 * - Display Over Other Apps (SYSTEM_ALERT_WINDOW)
 * - Device Admin
 * - Notification permission (Android 13+)
 * - Battery optimization exclusion
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val app by lazy { application as IzinKompeApp }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        updatePermissionStatuses()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatuses()
    }

    private fun setupButtons() {
        // Usage Access
        binding.btnUsageAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        // Overlay
        binding.btnOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        // Device Admin
        binding.btnDeviceAdmin.setOnClickListener {
            val componentName = ComponentName(this, AdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "IzinKompe memerlukan Device Admin untuk mencegah app di-uninstall selama jam belajar."
                )
            }
            startActivity(intent)
        }

        // Notification permission
        binding.btnNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Battery optimization
        binding.btnBattery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        // Continue
        binding.btnContinue.setOnClickListener {
            lifecycleScope.launch {
                app.preferences.setOnboardingDone(true)
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun updatePermissionStatuses() {
        // Usage Access
        val hasUsageAccess = hasUsageStatsPermission()
        binding.tvUsageStatus.text = if (hasUsageAccess) "✓ Diberikan" else "Belum diberikan"
        binding.btnUsageAccess.isEnabled = !hasUsageAccess

        // Overlay
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
        binding.tvOverlayStatus.text = if (hasOverlay) "✓ Diberikan" else "Belum diberikan"
        binding.btnOverlay.isEnabled = !hasOverlay

        // Device Admin
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, AdminReceiver::class.java)
        val hasAdmin = dpm.isAdminActive(componentName)
        binding.tvAdminStatus.text = if (hasAdmin) "✓ Aktif" else "Belum aktif"
        binding.btnDeviceAdmin.isEnabled = !hasAdmin

        // Notification
        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
        binding.tvNotifStatus.text = if (hasNotif) "✓ Diberikan" else "Belum diberikan"
        binding.btnNotification.isEnabled = !hasNotif

        // Battery optimization
        val hasBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(packageName)
        } else true
        binding.tvBatteryStatus.text = if (hasBattery) "✓ Dimatikan" else "Masih aktif"
        binding.btnBattery.isEnabled = !hasBattery

        // Enable continue button if minimum permissions are granted
        val minimumGranted = hasUsageAccess && hasOverlay
        binding.btnContinue.isEnabled = minimumGranted
        binding.btnContinue.alpha = if (minimumGranted) 1f else 0.5f
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
