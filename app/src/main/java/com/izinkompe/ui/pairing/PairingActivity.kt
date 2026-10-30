package com.izinkompe.ui.pairing

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.izinkompe.IzinKompeApp
import com.izinkompe.R
import com.izinkompe.databinding.ActivityPairingBinding
import com.izinkompe.util.TotpUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Activity for pairing with the parent's TOTP app (e.g., Google Authenticator).
 * Generates a TOTP secret, displays it as a QR code, and allows verification.
 */
class PairingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPairingBinding
    private val app by lazy { application as IzinKompeApp }
    private var currentSecret: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButtons()
        loadOrGenerateSecret()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        // Generate new secret
        binding.btnGenerateNew.setOnClickListener {
            lifecycleScope.launch {
                currentSecret = TotpUtil.generateSecret()
                app.preferences.setTotpSecret(currentSecret)
                app.preferences.setPairingDone(false)
                displaySecret(currentSecret)
                binding.tvPairingResult.visibility = View.GONE
            }
        }

        // Verify pairing
        binding.btnVerifyPairing.setOnClickListener {
            val code = binding.etVerifyOtp.text.toString().trim()
            if (code.length != 6) {
                Toast.makeText(this, "Masukkan kode 6 digit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isValid = TotpUtil.verifyCode(currentSecret, code)
            binding.tvPairingResult.visibility = View.VISIBLE

            if (isValid) {
                binding.tvPairingResult.text = getString(R.string.pairing_success)
                binding.tvPairingResult.setTextColor(getColor(R.color.status_active))

                lifecycleScope.launch {
                    app.preferences.setPairingDone(true)
                }

                Toast.makeText(this, "Pairing berhasil! ✓", Toast.LENGTH_LONG).show()
            } else {
                binding.tvPairingResult.text = getString(R.string.pairing_failed)
                binding.tvPairingResult.setTextColor(getColor(R.color.status_locked))
                binding.etVerifyOtp.text?.clear()
            }
        }
    }

    private fun loadOrGenerateSecret() {
        lifecycleScope.launch {
            val existingSecret = app.preferences.totpSecret.first()
            if (existingSecret.isNullOrEmpty()) {
                currentSecret = TotpUtil.generateSecret()
                app.preferences.setTotpSecret(currentSecret)
            } else {
                currentSecret = existingSecret
            }
            displaySecret(currentSecret)
        }
    }

    private fun displaySecret(secret: String) {
        // Show secret key
        binding.tvSecret.text = getString(R.string.pairing_secret, secret)

        // Generate QR code
        val otpUri = TotpUtil.generateOtpAuthUri(secret)
        val qrBitmap = generateQrCode(otpUri, 512)
        binding.ivQrCode.setImageBitmap(qrBitmap)
    }

    /**
     * Generates a QR code bitmap from the given text.
     */
    private fun generateQrCode(text: String, size: Int): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }
}
