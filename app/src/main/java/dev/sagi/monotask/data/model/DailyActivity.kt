package dev.sagi.monotask.data.model


/** Snapshot of task activity and XP earned on a single calendar day. */
data class DailyActivity(
    val dateEpochDay: Long = 0L,
    val tasksCompleted: Int = 0,
    val xpEarned: Int = 0
)
