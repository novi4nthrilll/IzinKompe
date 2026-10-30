package com.izinkompe.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a study schedule with start/end times and active days.
 */
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val monday: Boolean = true,
    val tuesday: Boolean = true,
    val wednesday: Boolean = true,
    val thursday: Boolean = true,
    val friday: Boolean = true,
    val saturday: Boolean = false,
    val sunday: Boolean = false,
    val isEnabled: Boolean = true
)
