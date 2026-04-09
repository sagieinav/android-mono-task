package dev.sagi.monotask.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.fake.FakeTaskRepository
import dev.sagi.monotask.domain.fake.FakeWorkspaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UndoSnoozeTaskUseCaseTest {

    private lateinit var fakeTaskRepo: FakeTaskRepository
    private lateinit var fakeWorkspaceRepo: FakeWorkspaceRepository
    private lateinit var useCase: UndoSnoozeTaskUseCase

    @BeforeEach fun setUp() {
        fakeTaskRepo = FakeTaskRepository()
        fakeWorkspaceRepo = FakeWorkspaceRepository()
        useCase = UndoSnoozeTaskUseCase(fakeTaskRepo, fakeWorkspaceRepo)
    }

    @Test fun `snooze fields are reverted to original task`() = runTest {
        val original = Task(id = "t1", workspaceId = "ws1", snoozeCount = 0)
        fakeTaskRepo.activeTasks = listOf(original.copy(snoozeCount = 1))

        useCase("u1", original)

        assertThat(fakeTaskRepo.undoneSnoozeTask?.id).isEqualTo("t1")
        assertThat(fakeTaskRepo.undoneSnoozeTask?.snoozeCount).isEqualTo(0)
    }

    @Test fun `task is re-pinned as focus`() = runTest {
        val task = Task(id = "t1", workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)

        useCase("u1", task)

        assertThat(fakeWorkspaceRepo.focusTaskIds["ws1"]).isEqualTo("t1")
    }
}
