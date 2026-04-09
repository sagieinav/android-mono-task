package dev.sagi.monotask.domain.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TaskSelectorTest {

    // Deterministic timestamps far from "now" to avoid time-sensitivity in tests
    private val FAR_PAST = Timestamp(0L, 0)            // 1970 → always overdue → urgency ≈ 1.0
    private val FAR_FUTURE = Timestamp(9_999_999_999L, 0) // year ~2286 → urgency ≈ 0.0

    private fun task(
        id: String,
        importance: Importance = Importance.MEDIUM,
        snoozeCount: Int = 0,
        dueDate: Timestamp? = null,
        workspaceId: String = "ws1"
    ) = Task(id = id, importance = importance, snoozeCount = snoozeCount, dueDate = dueDate, workspaceId = workspaceId)

    @Nested inner class GetTopTask {

        @Test fun `empty list returns null`() {
            assertThat(TaskSelector.getTopTask(emptyList(), 0.5f)).isNull()
        }

        @Test fun `single task is returned`() {
            val t = task("a")
            assertThat(TaskSelector.getTopTask(listOf(t), 0.5f)).isEqualTo(t)
        }

        @Test fun `highest importance wins when no due dates`() {
            val low = task("low", Importance.LOW)
            val mid = task("mid", Importance.MEDIUM)
            val high = task("high", Importance.HIGH)
            assertThat(TaskSelector.getTopTask(listOf(low, mid, high), 0.5f)).isEqualTo(high)
        }

        @Test fun `snoozed task ranks below non-snoozed same importance`() {
            val fresh = task("fresh", Importance.HIGH, snoozeCount = 0)
            val snoozed = task("snoozed", Importance.HIGH, snoozeCount = 3)
            assertThat(TaskSelector.getTopTask(listOf(snoozed, fresh), 0.5f)).isEqualTo(fresh)
        }

        @Test fun `excludeId task is not returned when another task exists`() {
            val a = task("a", Importance.HIGH)
            val b = task("b", Importance.LOW)
            assertThat(TaskSelector.getTopTask(listOf(a, b), 0.5f, excludeId = "a")).isEqualTo(b)
        }

        @Test fun `overdue task beats far-future same importance with high due-date weight`() {
            val overdue = task("overdue", Importance.MEDIUM, dueDate = FAR_PAST)
            val future = task("future", Importance.MEDIUM, dueDate = FAR_FUTURE)
            assertThat(TaskSelector.getTopTask(listOf(future, overdue), dueDateWeight = 0.9f)).isEqualTo(overdue)
        }
    }

    @Nested inner class GetTopTaskByDueDate {

        @Test fun `task with soonest due date wins regardless of importance`() {
            val urgent = task("urgent", Importance.LOW, dueDate = FAR_PAST)
            val distant = task("distant", Importance.HIGH, dueDate = FAR_FUTURE)
            assertThat(TaskSelector.getTopTaskByDueDate(listOf(urgent, distant), 0.5f)).isEqualTo(urgent)
        }

        @Test fun `falls back to normal priority when no tasks have a due date`() {
            val high = task("high", Importance.HIGH)
            val low = task("low", Importance.LOW)
            assertThat(TaskSelector.getTopTaskByDueDate(listOf(high, low), 0.5f)).isEqualTo(high)
        }
    }

    @Nested inner class GetSortedTasks {

        @Test fun `returns tasks in descending priority order`() {
            val low = task("low", Importance.LOW)
            val mid = task("mid", Importance.MEDIUM)
            val high = task("high", Importance.HIGH)
            val sorted = TaskSelector.getSortedTasks(listOf(low, mid, high), 0.5f)
            assertThat(sorted.map { it.id }).isEqualTo(listOf("high", "mid", "low"))
        }

        @Test fun `empty list returns empty list`() {
            assertThat(TaskSelector.getSortedTasks(emptyList(), 0.5f)).isEqualTo(emptyList())
        }
    }
}
