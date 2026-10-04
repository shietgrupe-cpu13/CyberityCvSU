package com.cyberity.cvsu

import java.util.Calendar
import java.util.TimeZone

/** Calendar weekdays (Sunday = 1), using the phone's local wall-clock time. */
internal object ReminderSchedule {
    /** Ignore a delayed alarm once its selected local day has passed, or after the time was moved later. */
    fun isDue(now: Long, hour: Int, minute: Int, days: Set<Int>, zone: TimeZone = TimeZone.getDefault()): Boolean {
        require(hour in 0..23 && minute in 0..59 && days.all { it in 1..7 })
        val local = Calendar.getInstance(zone).apply { timeInMillis = now }
        return local.get(Calendar.DAY_OF_WEEK) in days &&
            local.get(Calendar.HOUR_OF_DAY) * 60 + local.get(Calendar.MINUTE) >= hour * 60 + minute
    }

    fun next(now: Long, hour: Int, minute: Int, days: Set<Int>, zone: TimeZone = TimeZone.getDefault()): Long? {
        require(hour in 0..23 && minute in 0..59 && days.all { it in 1..7 })
        if (days.isEmpty()) return null
        val today = Calendar.getInstance(zone).apply { timeInMillis = now }
        for (offset in 0..7) {
            val candidate = (today.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.get(Calendar.DAY_OF_WEEK) in days && candidate.timeInMillis > now) return candidate.timeInMillis
        }
        return null
    }
}
