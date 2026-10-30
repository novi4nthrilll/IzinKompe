package com.izinkompe.util

import com.izinkompe.data.model.Schedule
import java.util.Calendar

/**
 * Checks whether the current time falls within an active study session.
 * This function is extracted for testability.
 */
object ScheduleChecker {

    /**
     * Returns true if the given [now] calendar falls within the schedule's active time and day.
     */
    fun isSessionActive(schedule: Schedule?, now: Calendar = Calendar.getInstance()): Boolean {
        if (schedule == null || !schedule.isEnabled) return false

        // Check if today is an active day
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val isDayActive = when (dayOfWeek) {
            Calendar.MONDAY -> schedule.monday
            Calendar.TUESDAY -> schedule.tuesday
            Calendar.WEDNESDAY -> schedule.wednesday
            Calendar.THURSDAY -> schedule.thursday
            Calendar.FRIDAY -> schedule.friday
            Calendar.SATURDAY -> schedule.saturday
            Calendar.SUNDAY -> schedule.sunday
            else -> false
        }
        if (!isDayActive) return false

        // Check time range
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = schedule.startHour * 60 + schedule.startMinute
        val endMinutes = schedule.endHour * 60 + schedule.endMinute

        return if (startMinutes <= endMinutes) {
            // Normal range: e.g., 08:00 to 12:00
            currentMinutes in startMinutes until endMinutes
        } else {
            // Overnight range: e.g., 22:00 to 06:00
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }
}
