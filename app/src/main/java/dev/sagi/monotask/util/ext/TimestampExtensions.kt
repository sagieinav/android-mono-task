package dev.sagi.monotask.util.ext

import androidx.compose.runtime.remember
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class FormattedDate(val text: String, val isOverdue: Boolean)

fun Timestamp.toRelativeDate(): FormattedDate {
    val now = System.currentTimeMillis()
    val diff = this.toDate().time - now
    return when {
        diff < 0 -> {
            val hrsAgo = (-diff / (1000 * 60 * 60)).toInt()
            val text = if (hrsAgo < 24) "${hrsAgo}h ago"
            else "${hrsAgo / 24}d ago"
            FormattedDate(text, isOverdue = true)
        }
        diff < 1000 * 60 * 60 * 24 -> {
            val hrs = (diff / (1000 * 60 * 60)).toInt()
            val mins = (diff / (1000 * 60) % 60).toInt()
            val text = if (hrs > 0) "in ${hrs}h ${mins}m" else "in ${mins}m"
            FormattedDate(text, isOverdue = false)
        }
        diff < 1000 * 60 * 60 * 24 * 7 -> {
            val days = (diff / (1000 * 60 * 60 * 24)).toInt()
            FormattedDate(if (days == 1) "Tomorrow" else "in $days days", isOverdue = false)
        }
        else -> FormattedDate(this.toFormattedDate(), isOverdue = false)
    }
}


fun Timestamp.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(toDate())
}

