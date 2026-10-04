package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReminderScheduleTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private fun time(year: Int, month: Int, day: Int, hour: Int, minute: Int, zone: TimeZone = utc) =
        Calendar.getInstance(zone).apply { clear(); set(year, month - 1, day, hour, minute, 0) }.timeInMillis

    @Test fun emptyDaysHaveNoReminder() {
        assertNull(ReminderSchedule.next(time(2026, 10, 5, 10, 0), 18, 0, emptySet(), utc))
    }
    @Test fun laterTodayUsesSelectedTime() {
        assertEquals(time(2026, 10, 5, 18, 30), ReminderSchedule.next(time(2026, 10, 5, 10, 0), 18, 30, setOf(Calendar.MONDAY), utc))
    }
    @Test fun passedTimeMovesToNextSelectedDay() {
        assertEquals(time(2026, 10, 7, 18, 0), ReminderSchedule.next(time(2026, 10, 5, 19, 0), 18, 0, setOf(Calendar.MONDAY, Calendar.WEDNESDAY), utc))
    }
    @Test fun firingAtScheduledTimeDoesNotScheduleItAgain() {
        assertEquals(time(2026, 10, 12, 18, 0), ReminderSchedule.next(time(2026, 10, 5, 18, 0), 18, 0, setOf(Calendar.MONDAY), utc))
    }
    @Test fun dailyReminderRollsIntoNextYear() {
        assertEquals(time(2027, 1, 1, 9, 0), ReminderSchedule.next(time(2026, 12, 31, 20, 0), 9, 0, (1..7).toSet(), utc))
    }
    @Test fun localTimeIsPreservedAcrossDaylightSavingsChange() {
        val zone = TimeZone.getTimeZone("America/New_York")
        val before = time(2026, 3, 7, 19, 0, zone)
        val next = ReminderSchedule.next(before, 18, 0, setOf(Calendar.SUNDAY), zone)
        assertEquals(time(2026, 3, 8, 18, 0, zone), next)
        assertEquals(22L * 60 * 60 * 1000, next!! - before)
    }
    @Test fun scheduleUsesTheSelectedTimezone() {
        val zone = TimeZone.getTimeZone("Asia/Shanghai")
        assertEquals(time(2026, 10, 5, 18, 0, zone), ReminderSchedule.next(time(2026, 10, 5, 10, 0, zone), 18, 0, setOf(Calendar.MONDAY), zone))
    }
    @Test(expected = IllegalArgumentException::class) fun invalidTimeIsRejected() {
        ReminderSchedule.next(0, 24, 0, setOf(Calendar.MONDAY), utc)
    }

    @Test fun delayedReminderOnUnselectedDayIsSkipped() {
        assertFalse(ReminderSchedule.isDue(time(2026, 10, 6, 19, 0), 18, 0, setOf(Calendar.MONDAY), utc))
    }

    @Test fun queuedAlarmCannotDeliverBeforeNewReminderTime() {
        assertFalse(ReminderSchedule.isDue(time(2026, 10, 5, 18, 0), 19, 0, setOf(Calendar.MONDAY), utc))
    }

    @Test fun selectedDayAllowsLateDeliveryAndScheduledMinute() {
        assertTrue(ReminderSchedule.isDue(time(2026, 10, 5, 18, 30), 18, 30, setOf(Calendar.MONDAY), utc))
        assertTrue(ReminderSchedule.isDue(time(2026, 10, 5, 23, 59), 18, 30, setOf(Calendar.MONDAY), utc))
    }

    @Test fun deliveryEligibilityUsesPhoneTimezone() {
        val zone = TimeZone.getTimeZone("Asia/Shanghai")
        val now = time(2026, 10, 5, 18, 30, zone)
        assertTrue(ReminderSchedule.isDue(now, 18, 30, setOf(Calendar.MONDAY), zone))
        assertFalse(ReminderSchedule.isDue(now, 18, 30, setOf(Calendar.MONDAY), utc))
    }
}
