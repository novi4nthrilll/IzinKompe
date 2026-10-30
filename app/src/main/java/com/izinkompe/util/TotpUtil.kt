package com.izinkompe.util

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

/**
 * TOTP (Time-based One-Time Password) implementation following RFC 6238.
 * Uses HMAC-SHA1 with a 30-second time step and 6-digit codes.
 * Compatible with Google Authenticator and other standard TOTP apps.
 */
object TotpUtil {

    private const val TIME_STEP_SECONDS = 30L
    private const val CODE_DIGITS = 6
    private const val ALGORITHM = "HmacSHA1"
    private const val SECRET_LENGTH = 20 // 160 bits

    /**
     * Generates a random Base32-encoded secret key for TOTP pairing.
     */
    fun generateSecret(): String {
        val random = java.security.SecureRandom()
        val bytes = ByteArray(SECRET_LENGTH)
        random.nextBytes(bytes)
        return Base32().encodeAsString(bytes).replace("=", "")
    }

    /**
     * Generates the current TOTP code for the given secret.
     */
    fun generateCode(secret: String, timeMillis: Long = System.currentTimeMillis()): String {
        val timeStep = timeMillis / 1000 / TIME_STEP_SECONDS
        return generateCodeForCounter(secret, timeStep)
    }

    /**
     * Verifies a TOTP code against the given secret.
     * Allows a window of ±1 time step to account for clock drift.
     */
    fun verifyCode(secret: String, code: String, timeMillis: Long = System.currentTimeMillis()): Boolean {
        if (code.length != CODE_DIGITS) return false

        val timeStep = timeMillis / 1000 / TIME_STEP_SECONDS

        // Check current step and ±1 for clock drift tolerance
        for (offset in -1..1) {
            val generated = generateCodeForCounter(secret, timeStep + offset)
            if (generated == code) return true
        }
        return false
    }

    /**
     * Generates a TOTP URI for QR code encoding.
     * Format: otpauth://totp/IzinKompe:Child?secret=SECRET&issuer=IzinKompe&algorithm=SHA1&digits=6&period=30
     */
    fun generateOtpAuthUri(secret: String, accountName: String = "Anak"): String {
        return "otpauth://totp/IzinKompe:$accountName?secret=$secret&issuer=IzinKompe&algorithm=SHA1&digits=$CODE_DIGITS&period=$TIME_STEP_SECONDS"
    }

    private fun generateCodeForCounter(secret: String, counter: Long): String {
        val base32 = Base32()
        val keyBytes = base32.decode(secret)

        // Convert counter to 8-byte big-endian
        val buffer = ByteBuffer.allocate(8)
        buffer.putLong(counter)
        val counterBytes = buffer.array()

        // HMAC-SHA1
        val mac = Mac.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(keyBytes, ALGORITHM)
        mac.init(keySpec)
        val hash = mac.doFinal(counterBytes)

        // Dynamic truncation
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val truncatedHash = (hash[offset].toInt() and 0x7F shl 24) or
                (hash[offset + 1].toInt() and 0xFF shl 16) or
                (hash[offset + 2].toInt() and 0xFF shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        val code = truncatedHash % 10.0.pow(CODE_DIGITS).toInt()
        return code.toString().padStart(CODE_DIGITS, '0')
    }
}
