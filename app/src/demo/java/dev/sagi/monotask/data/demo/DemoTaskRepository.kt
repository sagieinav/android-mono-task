package dev.sagi.monotask.data.demo

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.service.XpEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class DemoTaskRepository @Inject constructor() : TaskRepository {

    private val _tasks = MutableStateFlow(DemoSeedData.DEMO_TASKS.toMutableList())

    override fun getActiveTasks(userId: String, workspaceId: String): Flow<List<Task>> =
        _tasks.map { list -> list.filter { !it.completed && it.workspaceId == workspaceId } }

    override suspend fun getActiveTasksOnce(userId: String, workspaceId: String): List<Task> =
        _tasks.value.filter { !it.completed && it.workspaceId == workspaceId }

    override fun getCompletedTasks(userId: String, workspaceId: String): Flow<List<Task>> =
        _tasks.map { list -> list.filter { it.completed && it.workspaceId == workspaceId } }

    override suspend fun getCompletedTasksOnce(userId: String, workspaceId: String): List<Task> =
        _tasks.value.filter { it.completed && it.workspaceId == workspaceId }

    override fun getAllActiveTasks(userId: String): Flow<List<Task>> =
        _tasks.map { list -> list.filter { !it.completed } }

    override fun getAllCompletedTasks(userId: String): Flow<List<Task>> =
        _tasks.map { list -> list.filter { it.completed } }

    override suspend fun getAllCompletedTasksOnce(userId: String): List<Task> =
        _tasks.value.filter { it.completed }

    override suspend fun markTaskCompleted(userId: String, taskId: String) {
        _tasks.update { list -> list.map { if (it.id == taskId) it.copy(completed = true) else it }.toMutableList() }
    }

    override suspend fun restoreTask(userId: String, taskId: String) {
        _tasks.update { list -> list.map { if (it.id == taskId) it.copy(completed = false, completedAt = null) else it }.toMutableList() }
    }

    override suspend fun deleteTask(userId: String, taskId: String) {
        _tasks.update { list -> list.filter { it.id != taskId }.toMutableList() }
    }

    override suspend fun insertNewTask(userId: String, task: Task) {
        _tasks.update { list -> (list + task).toMutableList() }
    }

    override suspend fun updateSnoozeFields(userId: String, task: Task, option: XpEngine.SnoozeOption) {
        val newXp = XpEngine.calculateXpAfterSnooze(task, option)
        _tasks.update { list ->
            list.map {
                if (it.id == task.id) it.copy(snoozeCount = it.snoozeCount + 1, currentXp = newXp) else it
            }.toMutableList()
        }
    }

    override suspend fun overwriteExistingTask(userId: String, task: Task) {
        _tasks.update { list -> list.map { if (it.id == task.id) task else it }.toMutableList() }
    }

    override suspend fun undoSnoozeFields(userId: String, originalTask: Task) {
        _tasks.update { list -> list.map { if (it.id == originalTask.id) originalTask else it }.toMutableList() }
    }

    override suspend fun clearArchivedTasks(userId: String, workspaceId: String) {
        _tasks.update { list -> list.filter { !(it.completed) }.toMutableList() }
    }
}
