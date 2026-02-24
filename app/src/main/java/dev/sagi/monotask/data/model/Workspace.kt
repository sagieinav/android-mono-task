package dev.sagi.monotask.data.model

data class Workspace(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val dueDateWeight: Float = 0.5f,        // `Alpha` in the priority formula
    val importanceWeight: Float = 0.5f,     // `Beta` in the priority formula
    val randomnessFactor: Float = 0.05f,    // Add "noise" to avoid priority ties
)
