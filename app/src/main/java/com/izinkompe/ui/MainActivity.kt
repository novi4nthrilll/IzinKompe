package com.izinkompe.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.izinkompe.IzinKompeApp
import com.izinkompe.R
import com.izinkompe.databinding.ActivityMainBinding
import com.izinkompe.service.AppMonitorService
import com.izinkompe.ui.applist.AppListActivity
import com.izinkompe.ui.onboarding.OnboardingActivity
import com.izinkompe.ui.pairing.PairingActivity
import com.izinkompe.ui.schedule.ScheduleActivity
import com.izinkompe.util.ScheduleChecker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val app by lazy { application as IzinKompeApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check onboarding
        lifecycleScope.launch {
            val onboardingDone = app.preferences.isOnboardingDone.first()
            if (!onboardingDone) {
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                finish()
                return@launch
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun setupUI() {
        // Navigate to app list
        binding.cardLockedApps.setOnClickListener {
            requireOtpIfSessionActive {
                startActivity(Intent(this, AppListActivity::class.java))
            }
        }

        // Navigate to schedule
        binding.cardSchedule.setOnClickListener {
            requireOtpIfSessionActive {
                startActivity(Intent(this, ScheduleActivity::class.java))
            }
        }

        // Navigate to pairing
        binding.cardPairing.setOnClickListener {
            requireOtpIfSessionActive {
                startActivity(Intent(this, PairingActivity::class.java))
            }
        }

        // Toggle service
        binding.btnToggleService.setOnClickListener {
            requireOtpIfSessionActive {
                lifecycleScope.launch {
                    val isEnabled = app.preferences.isServiceEnabled.first()
                    if (isEnabled) {
                        AppMonitorService.stop(this@MainActivity)
                        app.preferences.setServiceEnabled(false)
                    } else {
                        AppMonitorService.start(this@MainActivity)
                        app.preferences.setServiceEnabled(true)
                    }
                    updateServiceButton(!isEnabled)
                }
            }
        }
    }

    private fun observeData() {
        // Observe locked apps count
        lifecycleScope.launch {
            app.database.lockedAppDao().getLockedCount().collectLatest { count ->
                binding.tvLockedAppsDetail.text = "$count app"
                binding.tvLockedAppsCount.text = if (count > 0) {
                    "$count aplikasi dikunci"
                } else {
                    getString(R.string.main_no_apps)
                }
            }
        }

        // Observe schedule
        lifecycleScope.launch {
            app.database.scheduleDao().getSchedule().collectLatest { schedule ->
                if (schedule != null) {
                    val startTime = String.format("%02d:%02d", schedule.startHour, schedule.startMinute)
                    val endTime = String.format("%02d:%02d", schedule.endHour, schedule.endMinute)
                    binding.tvScheduleDetail.text = "$startTime - $endTime"
                    binding.tvScheduleInfo.text = "Jadwal: $startTime - $endTime"
                } else {
                    binding.tvScheduleDetail.text = "Belum diatur"
                    binding.tvScheduleInfo.text = getString(R.string.main_no_schedule)
                }
            }
        }

        // Observe pairing status
        lifecycleScope.launch {
            app.preferences.isPairingDone.collectLatest { done ->
                binding.tvPairingStatus.text = if (done) "✓ Paired" else "Belum pairing"
            }
        }

        // Observe service state
        lifecycleScope.launch {
            app.preferences.isServiceEnabled.collectLatest { enabled ->
                updateServiceButton(enabled)
            }
        }
    }

    private fun updateStatus() {
        lifecycleScope.launch {
            val schedule = app.database.scheduleDao().getScheduleOnce()
            val isActive = ScheduleChecker.isSessionActive(schedule)
            binding.tvStatusTitle.text = if (isActive) {
                getString(R.string.main_status_active)
            } else {
                getString(R.string.main_status_inactive)
            }
        }
    }

    private fun updateServiceButton(enabled: Boolean) {
        binding.btnToggleService.text = if (enabled) {
            "Nonaktifkan IzinKompe"
        } else {
            "Aktifkan IzinKompe"
        }
    }

    private fun requireOtpIfSessionActive(action: () -> Unit) {
        lifecycleScope.launch {
            val schedule = app.database.scheduleDao().getScheduleOnce()
            val isActive = ScheduleChecker.isSessionActive(schedule)
            val isUnlocked = app.preferences.isCurrentlyUnlocked()
            if (isActive && !isUnlocked) {
                showOtpDialog(action)
            } else {
                action()
            }
        }
    }

    private fun showOtpDialog(onSuccess: () -> Unit) {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Kode OTP 6 Digit"
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setPadding(50, 40, 50, 40)
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Pengaturan Terkunci")
            .setMessage("Sesi belajar sedang berlangsung. Masukkan kode OTP dari orang tua untuk mengubah pengaturan.")
            .setView(input)
            .setPositiveButton("Buka Kunci") { _, _ ->
                val code = input.text.toString().trim()
                lifecycleScope.launch {
                    val secret = app.preferences.getTotpSecretOnce()
                    if (!secret.isNullOrEmpty() && com.izinkompe.util.TotpUtil.verifyCode(secret, code)) {
                        // Grant 15 minutes unlock
                        app.preferences.setUnlockUntil(System.currentTimeMillis() + 15 * 60 * 1000L)
                        android.widget.Toast.makeText(this@MainActivity, "Akses diizinkan (15 menit)", android.widget.Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } else {
                        android.widget.Toast.makeText(this@MainActivity, "Kode OTP salah!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
