package com.izinkompe.ui.lock

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.izinkompe.IzinKompeApp
import com.izinkompe.R
import com.izinkompe.databinding.ActivityLockBinding
import com.izinkompe.util.TotpUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full-screen lock activity displayed when a locked app is opened during a study session.
 * Requires TOTP code from parent to temporarily unlock (15 minutes).
 * Prevents back navigation.
 */
class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding
    private val app by lazy { application as IzinKompeApp }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        private const val UNLOCK_DURATION_MS = 15 * 60 * 1000L // 15 minutes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        showBlockedAppName(blockedPackage)
        setupVerifyButton()
        checkUnlockStatus()
    }

    private fun showBlockedAppName(packageName: String) {
        if (packageName == "com.android.settings") {
            binding.tvBlockedApp.text = "Pengaturan Sistem (Settings)"
            return
        }
        try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val appName = pm.getApplicationLabel(appInfo).toString()
            binding.tvBlockedApp.text = "Aplikasi terblokir: $appName"
        } catch (e: Exception) {
            binding.tvBlockedApp.text = "Aplikasi terblokir: $packageName"
        }
    }

    private fun setupVerifyButton() {
        binding.btnVerifyOtp.setOnClickListener {
            val code = binding.etOtp.text.toString().trim()
            if (code.length != 6) {
                showStatus(getString(R.string.otp_incorrect), false)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val secret = app.preferences.totpSecret.first()
                if (secret.isNullOrEmpty()) {
                    showStatus("Belum melakukan pairing. Lakukan pairing terlebih dahulu.", false)
                    return@launch
                }

                val isValid = TotpUtil.verifyCode(secret, code)
                if (isValid) {
                    // Unlock for 15 minutes
                    val unlockUntil = System.currentTimeMillis() + UNLOCK_DURATION_MS
                    app.preferences.setUnlockUntil(unlockUntil)

                    showStatus(getString(R.string.otp_correct), true)

                    // Show unlock timer
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    binding.tvUnlockTimer.text = getString(
                        R.string.lock_unlocked_until,
                        timeFormat.format(Date(unlockUntil))
                    )
                    binding.tvUnlockTimer.visibility = View.VISIBLE

                    // Auto-close after a brief delay
                    delay(1500)
                    finish()
                } else {
                    showStatus(getString(R.string.otp_incorrect), false)
                    binding.etOtp.text?.clear()

                    // Vibrate for feedback
                    @Suppress("DEPRECATION")
                    val vibrator = getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
                    vibrator.vibrate(200)
                }
            }
        }
    }

    private fun checkUnlockStatus() {
        lifecycleScope.launch {
            val isUnlocked = app.preferences.isCurrentlyUnlocked()
            if (isUnlocked) {
                finish()
            }
        }
    }

    private fun showStatus(message: String, success: Boolean) {
        binding.tvOtpStatus.text = message
        binding.tvOtpStatus.visibility = View.VISIBLE
        binding.tvOtpStatus.setTextColor(
            if (success) getColor(R.color.status_active) else getColor(R.color.status_warning)
        )
    }

    // Prevent back button from dismissing the lock screen
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing — prevent dismissing lock screen
    }
}
