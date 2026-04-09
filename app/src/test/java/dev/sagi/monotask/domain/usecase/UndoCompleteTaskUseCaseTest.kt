package dev.sagi.monotask.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.fake.FakeActivityRepository
import dev.sagi.monotask.domain.fake.FakeStatsRepository
import dev.sagi.monotask.domain.fake.FakeTaskRepository
import dev.sagi.monotask.domain.fake.FakeWorkspaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UndoCompleteTaskUseCaseTest {

    private lateinit var fakeTaskRepo: FakeTaskRepository
    private lateinit var fakeStatsRepo: FakeStatsRepository
    private lateinit var fakeActivityRepo: FakeActivityRepository
    private lateinit var fakeWorkspaceRepo: FakeWorkspaceRepository
    private lateinit var useCase: UndoCompleteTaskUseCase

    @BeforeEach fun setUp() {
        fakeTaskRepo = FakeTaskRepository()
        fakeStatsRepo = FakeStatsRepository()
        fakeActivityRepo = FakeActivityRepository()
        fakeWorkspaceRepo = FakeWorkspaceRepository()
        useCase = UndoCompleteTaskUseCase(fakeTaskRepo, fakeStatsRepo, fakeActivityRepo, fakeWorkspaceRepo)
    }

    @Test fun `task is restored to active`() = runTest {
        val task = Task(id = "t1", workspaceId = "ws1", completed = true)
        fakeTaskRepo.completedTasks = listOf(task)

        useCase("u1", "t1", "ws1", xpToRemove = 100, wasAce = true)

        assertThat(fakeTaskRepo.activeTasks.map { it.id }).isEqualTo(listOf("t1"))
    }

    @Test fun `XP is removed from stats`() = runTest {
        useCase("u1", "t1", "ws1", xpToRemove = 160, wasAce = false)

        assertThat(fakeStatsRepo.removeXpCalls.first()).isEqualTo(160)
    }

    @Test fun `activity entry is removed`() = runTest {
        useCase("u1", "t1", "ws1", xpToRemove = 130, wasAce = false)

        assertThat(fakeActivityRepo.removeCalls).isNotEmpty()
        assertThat(fakeActivityRepo.removeCalls.first().xp).isEqualTo(130)
    }

    @Test fun `user stats are rolled back`() = runTest {
        useCase("u1", "t1", "ws1", xpToRemove = 100, wasAce = true)

        assertThat(fakeStatsRepo.undoStatsCalls.first()).isEqualTo(true)
    }

    @Test fun `task is re-pinned as focus before restore`() = runTest {
        useCase("u1", "t1", "ws1", xpToRemove = 100, wasAce = false)

        assertThat(fakeWorkspaceRepo.focusTaskIds["ws1"]).isEqualTo("t1")
    }
}
