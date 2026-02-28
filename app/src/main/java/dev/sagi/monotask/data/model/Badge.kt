package dev.sagi.monotask.data.model

data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconRes: String = "",           // Icon's drawable resource name as a string
    val earned: Boolean = false,
    val earnedAt: Long? = null
)

object BadgeIds {
    const val TOP_PERFORMER = "top_performer"
    const val CONSISTENCY_KING = "consistency_king"
    const val KNOWLEDGE_SEEKER = "knowledge_seeker"
    const val FAST_FINISHER = "fast_finisher"
}