package dev.sagi.monotask.data.model

import com.google.firebase.Timestamp

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val importance: Importance = Importance.MEDIUM,
    val dueDate: Timestamp? = null,
    val workspaceId: String = "",
    val tags: List<String> = emptyList(),
    val snoozeCount: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val ownerId: String = ""
) {
    val isAce: Boolean get() = snoozeCount == 0
}

enum class Importance(val weight: Float) {
    LOW(1f),
    MEDIUM(2f),
    HIGH(3f)
}
