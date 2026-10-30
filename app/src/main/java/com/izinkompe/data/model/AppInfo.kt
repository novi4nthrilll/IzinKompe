package com.izinkompe.data.model

import android.graphics.drawable.Drawable

/**
 * Data class representing an installed app for display in the app picker.
 * Not stored in Room — used only in the UI layer.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    var isSelected: Boolean = false
)
