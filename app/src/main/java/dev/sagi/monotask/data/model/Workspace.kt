package dev.sagi.monotask.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Workspace(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val currentFocusTaskId: String? = null,
    val createdAt: Long = 0L
)
