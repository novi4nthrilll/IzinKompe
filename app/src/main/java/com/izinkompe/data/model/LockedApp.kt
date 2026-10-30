package com.izinkompe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app that has been selected to be locked during study sessions.
 */
@Entity(tableName = "locked_apps")
data class LockedApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isLocked: Boolean = true
)
