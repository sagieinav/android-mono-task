package dev.sagi.monotask.data.model

data class Workspace(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val currentFocusTaskId: String? = null
)
