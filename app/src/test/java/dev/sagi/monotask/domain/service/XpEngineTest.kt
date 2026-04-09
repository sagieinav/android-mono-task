package dev.sagi.monotask.domain.service

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class XpEngineTest {

    private fun task(
        importance: Importance = Importance.MEDIUM,
        snoozeCount: Int = 0,
        currentXp: Int = 0
    ) = Task(importance = importance, snoozeCount = snoozeCount, currentXp = currentXp)

    @Nested inner class CalculateTaskXp {

        // BASE=100, HIGH=+30, MEDIUM=+10, LOW=+0, ACE=+50

        @Test fun `HIGH importance ace returns 180`() {
            assertThat(XpEngine.calculateTaskXp(task(Importance.HIGH, snoozeCount = 0))).isEqualTo(180)
        }

        @Test fun `HIGH importance non-ace returns 130`() {
            assertThat(XpEngine.calculateTaskXp(task(Importance.HIGH, snoozeCount = 1))).isEqualTo(130)
        }

        @Test fun `MEDIUM importance ace returns 160`() {
            assertThat(XpEngine.calculateTaskXp(task(Importance.MEDIUM, snoozeCount = 0))).isEqualTo(160)
        }

        @Test fun `MEDIUM importance non-ace returns 110`() {
            assertThat(XpEngine.calculateTaskXp(task(Importance.MEDIUM, snoozeCount = 1))).isEqualTo(110)
        }

        @Test fun `LOW importance ace returns 150`() {
            assertThat(XpEngine.calculateTaskXp(task(Importance.LOW, snoozeCount = 0))).isEqualTo(150)
        }

        @Test fun `LOW importance non-ace returns 100`() {
            assertThat(XpEngine.calculateTaskXp(task(Importance.LOW, snoozeCount = 1))).isEqualTo(100)
        }
    }

    @Nested inner class CalculateXpAfterSnooze {

        // Ace penalty: option.penalty - 50 (ace tasks carry a steeper penalty)
        // Non-ace penalty: option.penalty only

        @Test fun `ace task MANUAL snooze loses 80 XP`() {
            val t = task(snoozeCount = 0, currentXp = 180)
            assertThat(XpEngine.calculateXpAfterSnooze(t, XpEngine.SnoozeOption.MANUAL)).isEqualTo(100)
        }

        @Test fun `non-ace task MANUAL snooze loses 30 XP`() {
            val t = task(snoozeCount = 1, currentXp = 130)
            assertThat(XpEngine.calculateXpAfterSnooze(t, XpEngine.SnoozeOption.MANUAL)).isEqualTo(100)
        }

        @Test fun `result is clamped to minimum 10`() {
            val t = task(snoozeCount = 0, currentXp = 10) // penalty = -80; 10 - 80 → clamped to 10
            assertThat(XpEngine.calculateXpAfterSnooze(t, XpEngine.SnoozeOption.MANUAL)).isEqualTo(10)
        }

        @Test fun `BY_DUE_DATE penalty is smaller than MANUAL`() {
            val t = task(snoozeCount = 1, currentXp = 130)
            val manual = XpEngine.calculateXpAfterSnooze(t, XpEngine.SnoozeOption.MANUAL)
            val byDueDate = XpEngine.calculateXpAfterSnooze(t, XpEngine.SnoozeOption.BY_DUE_DATE)
            assertThat(byDueDate).isGreaterThan(manual)
        }
    }

    @Nested inner class LevelForXp {

        @Test fun `0 XP is level 1`() {
            assertThat(XpEngine.levelForXp(0)).isEqualTo(1)
        }

        @Test fun `100 XP is level 2`() {
            assertThat(XpEngine.levelForXp(100)).isEqualTo(2)
        }

        @Test fun `347 XP is still level 2`() {
            assertThat(XpEngine.levelForXp(347)).isEqualTo(2)
        }

        @Test fun `348 XP is level 3`() {
            assertThat(XpEngine.levelForXp(348)).isEqualTo(3)
        }

        @Test fun `5230 XP is level 10`() {
            assertThat(XpEngine.levelForXp(5230)).isEqualTo(10)
        }
    }

    @Nested inner class ProgressToNextLevel {

        @Test fun `at level threshold progress is 0`() {
            val xpForL2 = XpEngine.xpRequiredForLevel(2) // 100
            assertThat(XpEngine.progressToNextLevel(xpForL2)).isEqualTo(0f)
        }

        @Test fun `progress is between 0 and 1`() {
            assertThat(XpEngine.progressToNextLevel(200)).isBetween(0f, 1f)
        }

        @Test fun `progress increases with more XP within a level`() {
            val low = XpEngine.progressToNextLevel(110)
            val high = XpEngine.progressToNextLevel(200)
            assertThat(high).isGreaterThan(low)
        }
    }
}
