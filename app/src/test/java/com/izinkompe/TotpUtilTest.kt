package com.izinkompe

import com.izinkompe.util.TotpUtil
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the TOTP utility.
 * Tests secret generation, code generation, and code verification.
 */
class TotpUtilTest {

    @Test
    fun `generated secret is not empty`() {
        val secret = TotpUtil.generateSecret()
        assertTrue(secret.isNotEmpty())
    }

    @Test
    fun `generated secret is Base32 encoded`() {
        val secret = TotpUtil.generateSecret()
        // Base32 characters: A-Z and 2-7
        assertTrue(secret.all { it in 'A'..'Z' || it in '2'..'7' })
    }

    @Test
    fun `generated code is 6 digits`() {
        val secret = TotpUtil.generateSecret()
        val code = TotpUtil.generateCode(secret)
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun `code verification succeeds for current code`() {
        val secret = TotpUtil.generateSecret()
        val code = TotpUtil.generateCode(secret)
        assertTrue(TotpUtil.verifyCode(secret, code))
    }

    @Test
    fun `code verification fails for wrong code`() {
        val secret = TotpUtil.generateSecret()
        assertFalse(TotpUtil.verifyCode(secret, "000000"))
    }

    @Test
    fun `code verification fails for short code`() {
        val secret = TotpUtil.generateSecret()
        assertFalse(TotpUtil.verifyCode(secret, "123"))
    }

    @Test
    fun `same secret generates same code at same time`() {
        val secret = TotpUtil.generateSecret()
        val time = System.currentTimeMillis()
        val code1 = TotpUtil.generateCode(secret, time)
        val code2 = TotpUtil.generateCode(secret, time)
        assertEquals(code1, code2)
    }

    @Test
    fun `otp auth URI has correct format`() {
        val secret = "JBSWY3DPEHPK3PXP"
        val uri = TotpUtil.generateOtpAuthUri(secret)
        assertTrue(uri.startsWith("otpauth://totp/IzinKompe:"))
        assertTrue(uri.contains("secret=$secret"))
        assertTrue(uri.contains("issuer=IzinKompe"))
        assertTrue(uri.contains("algorithm=SHA1"))
        assertTrue(uri.contains("digits=6"))
        assertTrue(uri.contains("period=30"))
    }
}
