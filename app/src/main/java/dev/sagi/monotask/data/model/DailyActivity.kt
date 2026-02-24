package dev.sagi.monotask.data.model

data class DailyActivity(
    val dateEpochDay: Long = 0L,
    val tasksCompleted: Int = 0,
    val xpEarned: Int = 0
)
