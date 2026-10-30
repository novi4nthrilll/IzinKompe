package com.izinkompe

import com.izinkompe.data.model.Schedule
import com.izinkompe.util.ScheduleChecker
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for ScheduleChecker.isSessionActive().
 * Tests time ranges, day-of-week logic, overnight schedules, and edge cases.
 */
class ScheduleCheckerTest {

    private fun createCalendar(dayOfWeek: Int, hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
    }

    private val defaultSchedule = Schedule(
        startHour = 8, startMinute = 0,
        endHour = 12, endMinute = 0,
        monday = true, tuesday = true, wednesday = true,
        thursday = true, friday = true,
        saturday = false, sunday = false,
        isEnabled = true
    )

    @Test
    fun `active during schedule time on weekday`() {
        val now = createCalendar(Calendar.MONDAY, 10, 0)
        assertTrue(ScheduleChecker.isSessionActive(defaultSchedule, now))
    }

    @Test
    fun `inactive before schedule start`() {
        val now = createCalendar(Calendar.MONDAY, 7, 59)
        assertFalse(ScheduleChecker.isSessionActive(defaultSchedule, now))
    }

    @Test
    fun `inactive at schedule end time`() {
        val now = createCalendar(Calendar.MONDAY, 12, 0)
        assertFalse(ScheduleChecker.isSessionActive(defaultSchedule, now))
    }

    @Test
    fun `inactive after schedule end`() {
        val now = createCalendar(Calendar.MONDAY, 14, 0)
        assertFalse(ScheduleChecker.isSessionActive(defaultSchedule, now))
    }

    @Test
    fun `inactive on weekend when not scheduled`() {
        val now = createCalendar(Calendar.SATURDAY, 10, 0)
        assertFalse(ScheduleChecker.isSessionActive(defaultSchedule, now))
    }

    @Test
    fun `inactive when schedule is null`() {
        assertFalse(ScheduleChecker.isSessionActive(null))
    }

    @Test
    fun `inactive when schedule is disabled`() {
        val disabled = defaultSchedule.copy(isEnabled = false)
        val now = createCalendar(Calendar.MONDAY, 10, 0)
        assertFalse(ScheduleChecker.isSessionActive(disabled, now))
    }

    @Test
    fun `active at exact start time`() {
        val now = createCalendar(Calendar.TUESDAY, 8, 0)
        assertTrue(ScheduleChecker.isSessionActive(defaultSchedule, now))
    }

    @Test
    fun `overnight schedule - active before midnight`() {
        val overnightSchedule = defaultSchedule.copy(
            startHour = 22, startMinute = 0,
            endHour = 6, endMinute = 0
        )
        val now = createCalendar(Calendar.MONDAY, 23, 0)
        assertTrue(ScheduleChecker.isSessionActive(overnightSchedule, now))
    }

    @Test
    fun `overnight schedule - active after midnight`() {
        val overnightSchedule = defaultSchedule.copy(
            startHour = 22, startMinute = 0,
            endHour = 6, endMinute = 0
        )
        val now = createCalendar(Calendar.MONDAY, 3, 0)
        assertTrue(ScheduleChecker.isSessionActive(overnightSchedule, now))
    }

    @Test
    fun `overnight schedule - inactive during day`() {
        val overnightSchedule = defaultSchedule.copy(
            startHour = 22, startMinute = 0,
            endHour = 6, endMinute = 0
        )
        val now = createCalendar(Calendar.MONDAY, 14, 0)
        assertFalse(ScheduleChecker.isSessionActive(overnightSchedule, now))
    }
}
