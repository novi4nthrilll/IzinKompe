package com.izinkompe.util

/**
 * List of apps that should never be locked.
 * These are essential communication and system apps.
 */
object BlocklistExclusions {

    /**
     * Package names of apps that cannot be locked.
     * Includes phone, SMS, and essential communication apps.
     */
    val EXCLUDED_PACKAGES = setOf(
        // Phone & Dialer
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.phone",

        // SMS / Messages
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",

        // WhatsApp (essential communication)
        "com.whatsapp",
        "com.whatsapp.w4b",

        // System Settings
        "com.android.settings",

        // IzinKompe itself
        "com.izinkompe",

        // Emergency / SOS
        "com.android.emergency",

        // Contacts
        "com.android.contacts",
        "com.google.android.contacts",
        "com.samsung.android.contacts"
    )

    /**
     * Returns true if the given package should NOT appear in the lockable apps list.
     */
    fun isExcluded(packageName: String): Boolean {
        return packageName in EXCLUDED_PACKAGES
    }
}
