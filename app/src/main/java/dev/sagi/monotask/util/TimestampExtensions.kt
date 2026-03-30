package dev.sagi.monotask.util

import com.google.firebase.Timestamp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant


data class FormattedDate(val text: String, val isOverdue: Boolean)

fun Timestamp.toRelativeDate(): FormattedDate {
    val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(timeZone).date
    val due = Instant.fromEpochMilliseconds(this.toDate().time).toLocalDateTime(timeZone).date
    val diffDays = today.daysUntil(due)

    return when {
        diffDays < 0  -> {
            val daysOverdue = -diffDays
            val text = if (daysOverdue > 7) toFormattedDate() else "${daysOverdue}d ago"
            FormattedDate(text, isOverdue = true)
        }
        diffDays == 0 -> FormattedDate("Today", isOverdue = false)
        diffDays == 1 -> FormattedDate("Tomorrow", isOverdue = false)
        diffDays < 7  -> FormattedDate("in $diffDays days", isOverdue = false)
        else          -> FormattedDate(this.toFormattedDate(), isOverdue = false)
    }
}


fun Timestamp.toFormattedDate(): String {
    val due = this.toDate()
    val dueYear = Instant.fromEpochMilliseconds(due.time)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .year

    val thisYear = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .year

    val pattern = if (dueYear != thisYear) "MMM d, yyyy" else "MMM d"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(due)
}

