package dev.sagi.monotask.util.ext

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun Timestamp.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(toDate())
}
