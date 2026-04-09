package dev.sagi.monotask.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.fake.FakeTaskRepository
import dev.sagi.monotask.domain.fake.FakeWorkspaceRepository
import dev.sagi.monotask.domain.service.XpEngine
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SnoozeTaskUseCaseTest {

    private lateinit var fakeTaskRepo: FakeTaskRepository
    private lateinit var fakeWorkspaceRepo: FakeWorkspaceRepository
    private lateinit var useCase: SnoozeTaskUseCase

    @BeforeEach fun setUp() {
        fakeTaskRepo = FakeTaskRepository()
        fakeWorkspaceRepo = FakeWorkspaceRepository()
        useCase = SnoozeTaskUseCase(fakeTaskRepo, fakeWorkspaceRepo)
    }

    @Test fun `snooze fields are updated on the task`() = runTest {
        val task = Task(id = "t1", workspaceId = "ws1", importance = Importance.HIGH)
        fakeTaskRepo.activeTasks = listOf(task)

        useCase("u1", task, "ws1", XpEngine.SnoozeOption.MANUAL, 0.5f)

        assertThat(fakeTaskRepo.snoozedTask?.id).isEqualTo("t1")
        assertThat(fakeTaskRepo.snoozedOption).isEqualTo(XpEngine.SnoozeOption.MANUAL)
    }

    @Test fun `next highest-priority task is set as focus`() = runTest {
        val snoozed = Task(id = "t1", workspaceId = "ws1", importance = Importance.LOW)
        val next = Task(id = "t2", workspaceId = "ws1", importance = Importance.HIGH)
        fakeTaskRepo.activeTasks = listOf(snoozed, next)

        useCase("u1", snoozed, "ws1", XpEngine.SnoozeOption.MANUAL, 0.5f)

        assertThat(fakeWorkspaceRepo.focusTaskIds["ws1"]).isEqualTo("t2")
    }

    @Test fun `BY_DUE_DATE option also updates snooze fields`() = runTest {
        val task = Task(id = "t1", workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)

        useCase("u1", task, "ws1", XpEngine.SnoozeOption.BY_DUE_DATE, 0.5f)

        assertThat(fakeTaskRepo.snoozedOption).isEqualTo(XpEngine.SnoozeOption.BY_DUE_DATE)
    }
}
