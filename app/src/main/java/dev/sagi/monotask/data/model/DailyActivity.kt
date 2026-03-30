package dev.sagi.monotask.data.model

import androidx.annotation.Keep

/** Snapshot of task activity and XP earned on a single calendar day. */
@Keep
data class DailyActivity(
    val dateEpochDay: Long = 0L,
    val tasksCompleted: Int = 0,
    val xpEarned: Int = 0
)
