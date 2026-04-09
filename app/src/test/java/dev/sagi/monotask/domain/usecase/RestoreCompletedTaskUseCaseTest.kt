package dev.sagi.monotask.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.fake.FakeActivityRepository
import dev.sagi.monotask.domain.fake.FakeStatsRepository
import dev.sagi.monotask.domain.fake.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RestoreCompletedTaskUseCaseTest {

    private lateinit var fakeTaskRepo: FakeTaskRepository
    private lateinit var fakeActivityRepo: FakeActivityRepository
    private lateinit var fakeStatsRepo: FakeStatsRepository
    private lateinit var useCase: RestoreCompletedTaskUseCase

    @BeforeEach fun setUp() {
        fakeTaskRepo = FakeTaskRepository()
        fakeActivityRepo = FakeActivityRepository()
        fakeStatsRepo = FakeStatsRepository()
        useCase = RestoreCompletedTaskUseCase(fakeTaskRepo, fakeActivityRepo, fakeStatsRepo)
    }

    @Test fun `task moves back to active list`() = runTest {
        val task = Task(id = "t1", currentXp = 100, completed = true)
        fakeTaskRepo.completedTasks = listOf(task)

        useCase("u1", task)

        assertThat(fakeTaskRepo.activeTasks.map { it.id }).isEqualTo(listOf("t1"))
    }

    @Test fun `XP is removed from stats`() = runTest {
        val task = Task(id = "t1", currentXp = 160, completed = true)
        fakeTaskRepo.completedTasks = listOf(task)

        useCase("u1", task)

        assertThat(fakeStatsRepo.removeXpCalls.first()).isEqualTo(160)
    }

    @Test fun `activity entry is removed for original completion date`() = runTest {
        val completedAt = Timestamp(1_000_000L, 0) // known epoch seconds
        val task = Task(id = "t1", currentXp = 130, completed = true, completedAt = completedAt)
        fakeTaskRepo.completedTasks = listOf(task)

        useCase("u1", task)

        assertThat(fakeActivityRepo.removeCalls).isNotEmpty()
        assertThat(fakeActivityRepo.removeCalls.first().xp).isEqualTo(130)
    }

    @Test fun `user stats are rolled back`() = runTest {
        val task = Task(id = "t1", currentXp = 100, snoozeCount = 0) // isAce = true
        fakeTaskRepo.completedTasks = listOf(task)

        useCase("u1", task)

        assertThat(fakeStatsRepo.undoStatsCalls.first()).isEqualTo(true) // wasAce = isAce = true
    }
}
