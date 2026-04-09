package dev.sagi.monotask.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotEmpty
import dev.sagi.monotask.data.model.AchievementCategory
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.fake.FakeActivityRepository
import dev.sagi.monotask.domain.fake.FakeStatsRepository
import dev.sagi.monotask.domain.fake.FakeTaskRepository
import dev.sagi.monotask.domain.fake.FakeWorkspaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CompleteTaskUseCaseTest {

    private lateinit var fakeTaskRepo: FakeTaskRepository
    private lateinit var fakeStatsRepo: FakeStatsRepository
    private lateinit var fakeActivityRepo: FakeActivityRepository
    private lateinit var fakeWorkspaceRepo: FakeWorkspaceRepository
    private lateinit var useCase: CompleteTaskUseCase

    @BeforeEach fun setUp() {
        fakeTaskRepo = FakeTaskRepository()
        fakeStatsRepo = FakeStatsRepository()
        fakeActivityRepo = FakeActivityRepository()
        fakeWorkspaceRepo = FakeWorkspaceRepository()
        useCase = CompleteTaskUseCase(fakeTaskRepo, fakeStatsRepo, fakeActivityRepo, fakeWorkspaceRepo)
    }

    @Test fun `xpAwarded equals task currentXp`() = runTest {
        val task = Task(id = "t1", currentXp = 180, workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)
        val user = User(id = "u1", level = 1, xp = 0)

        val result = useCase("u1", task, "ws1", user)

        assertThat(result.xpAwarded).isEqualTo(180)
    }

    @Test fun `addXp is called with task currentXp`() = runTest {
        val task = Task(id = "t1", currentXp = 160, workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)
        val user = User(id = "u1", level = 1, xp = 0)

        useCase("u1", task, "ws1", user)

        assertThat(fakeStatsRepo.addXpCalls.first().amount).isEqualTo(160)
    }

    @Test fun `activity is logged with correct XP and task count`() = runTest {
        val task = Task(id = "t1", currentXp = 130, workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)
        val user = User(id = "u1", level = 1, xp = 0)

        useCase("u1", task, "ws1", user)

        val logged = fakeActivityRepo.logCalls.first()
        assertThat(logged.xp).isEqualTo(130)
        assertThat(logged.tasks).isEqualTo(1)
    }

    @Test fun `level up is detected when XP crosses threshold`() = runTest {
        // xpRequiredForLevel(2) = 100; user at xp=0, task gives 100 → newLevel = 2
        val task = Task(id = "t1", currentXp = 100, workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)
        val user = User(id = "u1", level = 1, xp = 0)

        val result = useCase("u1", task, "ws1", user)

        assertThat(result.previousLevel).isEqualTo(1)
        assertThat(result.newLevel).isGreaterThan(result.previousLevel)
    }

    @Test fun `no level up when XP does not cross threshold`() = runTest {
        val task = Task(id = "t1", currentXp = 50, workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)
        val user = User(id = "u1", level = 1, xp = 0)

        val result = useCase("u1", task, "ws1", user)

        assertThat(result.newLevel).isEqualTo(result.previousLevel)
    }

    @Test fun `TASK_VOLUME achievement unlocked when completing 5th task`() = runTest {
        // 4 already completed; this is the 5th → TASK_VOLUME BRONZE threshold
        fakeTaskRepo.completedTasks = (1..4).map { Task(id = "prev$it") }
        val task = Task(id = "t5", currentXp = 100, workspaceId = "ws1", importance = Importance.LOW)
        fakeTaskRepo.activeTasks = listOf(task)
        val user = User(id = "u1", level = 1, xp = 0)

        val result = useCase("u1", task, "ws1", user)

        assertThat(result.newlyUnlocked).isNotEmpty()
        assertThat(result.newlyUnlocked.first().category).isEqualTo(AchievementCategory.TASK_VOLUME)
    }

    @Test fun `updateUserStats exception does not propagate`() = runTest {
        fakeStatsRepo.shouldThrowOnUpdateStats = true
        val task = Task(id = "t1", currentXp = 100, workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)
        val user = User(id = "u1", level = 1, xp = 0)

        // Should complete without throwing
        useCase("u1", task, "ws1", user)
    }

    @Test fun `focus task is cleared after completion`() = runTest {
        val task = Task(id = "t1", currentXp = 100, workspaceId = "ws1")
        fakeTaskRepo.activeTasks = listOf(task)
        val user = User(id = "u1", level = 1, xp = 0)

        useCase("u1", task, "ws1", user)

        assertThat(fakeWorkspaceRepo.focusTaskIds["ws1"]).isEqualTo(null)
    }
}
