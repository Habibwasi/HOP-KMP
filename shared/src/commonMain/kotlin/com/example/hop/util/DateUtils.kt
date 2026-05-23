package com.example.hop.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Converts an ISO 8601 departure timestamp (e.g. "2026-05-20T08:30:00.000Z") into a
 * short, human-friendly string relative to today in the device's local timezone.
 *
 * Examples:
 *  - Same day   → "Today · 08:30"
 *  - Next day   → "Tomorrow · 08:30"
 *  - Within 6 d → "Wed · 08:30"
 *  - Further    → "Wed 20 May · 08:30"
 *
 * Falls back to the raw string if parsing fails.
 */
fun formatDeparture(isoString: String): String {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now()
    val today = now.toLocalDateTime(tz).date

    val instant = try {
        Instant.parse(isoString)
    } catch (_: Exception) {
        return isoString
    }

    val ldt = instant.toLocalDateTime(tz)
    val date = ldt.date
    val hour = ldt.hour.toString().padStart(2, '0')
    val minute = ldt.minute.toString().padStart(2, '0')
    val timeStr = "$hour:$minute"

    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    val in7Days = today.plus(7, DateTimeUnit.DAY)

    return when {
        date == today    -> "Today · $timeStr"
        date == tomorrow -> "Tomorrow · $timeStr"
        date < in7Days   -> "${date.dayOfWeek.shortName()} · $timeStr"
        else             -> "${date.dayOfWeek.shortName()} ${date.dayOfMonth} ${date.month.shortName()} · $timeStr"
    }
}

private fun DayOfWeek.shortName(): String = when (this) {
    DayOfWeek.MONDAY    -> "Mon"
    DayOfWeek.TUESDAY   -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY  -> "Thu"
    DayOfWeek.FRIDAY    -> "Fri"
    DayOfWeek.SATURDAY  -> "Sat"
    DayOfWeek.SUNDAY    -> "Sun"
    else                -> name.take(3).lowercase().replaceFirstChar { it.uppercase() }
}

private fun Month.shortName(): String = when (this) {
    Month.JANUARY   -> "Jan"
    Month.FEBRUARY  -> "Feb"
    Month.MARCH     -> "Mar"
    Month.APRIL     -> "Apr"
    Month.MAY       -> "May"
    Month.JUNE      -> "Jun"
    Month.JULY      -> "Jul"
    Month.AUGUST    -> "Aug"
    Month.SEPTEMBER -> "Sep"
    Month.OCTOBER   -> "Oct"
    Month.NOVEMBER  -> "Nov"
    Month.DECEMBER  -> "Dec"
    else            -> name.take(3).lowercase().replaceFirstChar { it.uppercase() }
}
