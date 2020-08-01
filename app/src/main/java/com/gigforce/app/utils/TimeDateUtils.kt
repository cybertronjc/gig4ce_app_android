package com.gigforce.app.utils

import java.util.*

fun getStartOfDayInMillis(): Long {
    val calendar: Calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun getEndOfDayInMillis(): Long {
    // Add one day's time to the beginning of the day.
    // 24 hours * 60 minutes * 60 seconds * 1000 milliseconds = 1 day
    return getStartOfDayInMillis() + 24 * 60 * 60 * 1000
}