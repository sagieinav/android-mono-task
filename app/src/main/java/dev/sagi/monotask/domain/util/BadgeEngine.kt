package dev.sagi.monotask.domain.util

import dev.sagi.monotask.data.model.Badge
import dev.sagi.monotask.data.model.BadgeIds
import dev.sagi.monotask.data.model.Task

object BadgeEngine {

    /**
     * Evaluates which badges the user has earned after a task completion.
     * Returns a list of newly earned badge IDs.
     */
    fun evaluate(completedTasks: List<Task>): List<String> {
        val earned = mutableListOf<String>()

        if (hasTopPerformer(completedTasks))    earned.add(BadgeIds.TOP_PERFORMER)
        if (hasConsistencyKing(completedTasks)) earned.add(BadgeIds.CONSISTENCY_KING)
        if (hasFastFinisher(completedTasks))    earned.add(BadgeIds.FAST_FINISHER)
        if (hasKnowledgeSeeker(completedTasks)) earned.add(BadgeIds.KNOWLEDGE_SEEKER)

        return earned
    }

    // 10 ACE completions in a row (never snoozed)
    private fun hasTopPerformer(tasks: List<Task>): Boolean {
        val recent = tasks.takeLast(10)
        return recent.size == 10 && recent.all { it.isAce }
    }

    // Completed at least 1 task per day for 7 consecutive days
    private fun hasConsistencyKing(tasks: List<Task>): Boolean {
        val days = tasks
            .mapNotNull { it.createdAt.toDate() }
            .map { java.time.Instant.ofEpochMilli(it.time)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay()
            }
            .toSortedSet()

        if (days.size < 7) return false

        // Check if any 7 consecutive days exist in the set
        val daysList = days.toList()
        for (i in 0..daysList.size - 7) {
            val window = daysList.subList(i, i + 7)
            if (window.last() - window.first() == 6L &&
                window.zipWithNext().all { (a, b) -> b - a == 1L }) {
                return true
            }
        }
        return false
    }

    // Completed a HIGH importance task without ever snoozing it
    private fun hasFastFinisher(tasks: List<Task>): Boolean =
        tasks.any { it.importance == dev.sagi.monotask.data.model.Importance.HIGH && it.isAce }

    // Completed tasks across all 3 importance levels
    private fun hasKnowledgeSeeker(tasks: List<Task>): Boolean {
        val importanceLevels = tasks.map { it.importance }.toSet()
        return importanceLevels.size == 3
    }

    // Converts a list of earned badge IDs into full Badge objects for the UI.
    fun buildBadgeList(earnedIds: List<String>): List<Badge> =
        allBadges.map { it.copy(
            earned = it.id in earnedIds,
            earnedAt = if (it.id in earnedIds) System.currentTimeMillis() else null
        )}

    // Master list of all badges in the app
    private val allBadges = listOf(
        Badge(
            id          = BadgeIds.TOP_PERFORMER,
            name        = "Top Performer",
            description = "Complete 10 tasks in a row without snoozing",
            iconRes     = "badge_top_performer"
        ),
        Badge(
            id          = BadgeIds.CONSISTENCY_KING,
            name        = "Consistency King",
            description = "Complete at least one task every day for 7 days",
            iconRes     = "badge_consistency_king"
        ),
        Badge(
            id          = BadgeIds.FAST_FINISHER,
            name        = "Fast Finisher",
            description = "Complete a high importance task on the first try",
            iconRes     = "badge_fast_finisher"
        ),
        Badge(
            id          = BadgeIds.KNOWLEDGE_SEEKER,
            name        = "Knowledge Seeker",
            description = "Complete tasks across all three importance levels",
            iconRes     = "badge_knowledge_seeker"
        )
    )
}
