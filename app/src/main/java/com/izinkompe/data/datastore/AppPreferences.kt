package com.izinkompe.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "izinkompe_prefs")

/**
 * DataStore-based preferences for app settings.
 * Stores TOTP secret, onboarding status, unlock timer, and service state.
 */
class AppPreferences(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_TOTP_SECRET = stringPreferencesKey("totp_secret")
        private val KEY_PAIRING_DONE = booleanPreferencesKey("pairing_done")
        private val KEY_SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        private val KEY_UNLOCK_UNTIL = longPreferencesKey("unlock_until")
    }

    // Onboarding
    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = done
        }
    }

    // TOTP Secret
    val totpSecret: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOTP_SECRET]
    }

    suspend fun getTotpSecretOnce(): String? {
        return context.dataStore.data.first()[KEY_TOTP_SECRET]
    }

    suspend fun setTotpSecret(secret: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOTP_SECRET] = secret
        }
    }

    // Pairing
    val isPairingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PAIRING_DONE] ?: false
    }

    suspend fun setPairingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PAIRING_DONE] = done
        }
    }

    // Service enabled
    val isServiceEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVICE_ENABLED] ?: false
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVICE_ENABLED] = enabled
        }
    }

    // Unlock timer — stores the epoch millis until which apps are unlocked
    val unlockUntil: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_UNLOCK_UNTIL] ?: 0L
    }

    suspend fun setUnlockUntil(epochMillis: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_UNLOCK_UNTIL] = epochMillis
        }
    }

    suspend fun isCurrentlyUnlocked(): Boolean {
        val unlockTime = context.dataStore.data.first()[KEY_UNLOCK_UNTIL] ?: 0L
        return System.currentTimeMillis() < unlockTime
    }
}
