package dev.sagi.monotask.domain.fake

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.service.XpEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTaskRepository : TaskRepository {

    private val _activeTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _completedTasks = MutableStateFlow<List<Task>>(emptyList())

    var activeTasks: List<Task>
        get() = _activeTasks.value
        set(value) { _activeTasks.value = value }

    var completedTasks: List<Task>
        get() = _completedTasks.value
        set(value) { _completedTasks.value = value }

    // Call tracking
    var snoozedTask: Task? = null
    var snoozedOption: XpEngine.SnoozeOption? = null
    var undoneSnoozeTask: Task? = null

    override fun getActiveTasks(userId: String, workspaceId: String): Flow<List<Task>> =
        _activeTasks.map { list -> list.filter { it.workspaceId == workspaceId } }

    override suspend fun getActiveTasksOnce(userId: String, workspaceId: String): List<Task> =
        _activeTasks.value.filter { it.workspaceId == workspaceId }

    override fun getCompletedTasks(userId: String, workspaceId: String): Flow<List<Task>> =
        _completedTasks.map { list -> list.filter { it.workspaceId == workspaceId } }

    override suspend fun getCompletedTasksOnce(userId: String, workspaceId: String): List<Task> =
        _completedTasks.value.filter { it.workspaceId == workspaceId }

    override fun getAllActiveTasks(userId: String): Flow<List<Task>> = _activeTasks

    override fun getAllCompletedTasks(userId: String): Flow<List<Task>> = _completedTasks

    override suspend fun getAllCompletedTasksOnce(userId: String): List<Task> = _completedTasks.value

    override suspend fun markTaskCompleted(userId: String, taskId: String) {
        val task = _activeTasks.value.find { it.id == taskId } ?: return
        _activeTasks.update { it.filter { t -> t.id != taskId } }
        _completedTasks.update { it + task.copy(completed = true) }
    }

    override suspend fun restoreTask(userId: String, taskId: String) {
        val task = _completedTasks.value.find { it.id == taskId } ?: return
        _completedTasks.update { it.filter { t -> t.id != taskId } }
        _activeTasks.update { it + task.copy(completed = false) }
    }

    override suspend fun deleteTask(userId: String, taskId: String) {
        _activeTasks.update { it.filter { t -> t.id != taskId } }
        _completedTasks.update { it.filter { t -> t.id != taskId } }
    }

    override suspend fun insertNewTask(userId: String, task: Task) {
        _activeTasks.update { it + task }
    }

    override suspend fun updateSnoozeFields(userId: String, task: Task, option: XpEngine.SnoozeOption) {
        snoozedTask = task
        snoozedOption = option
        _activeTasks.update { list ->
            list.map { if (it.id == task.id) it.copy(snoozeCount = it.snoozeCount + 1) else it }
        }
    }

    override suspend fun overwriteExistingTask(userId: String, task: Task) {
        _activeTasks.update { list ->
            list.map { if (it.id == task.id) task else it }
        }
    }

    override suspend fun undoSnoozeFields(userId: String, originalTask: Task) {
        undoneSnoozeTask = originalTask
        _activeTasks.update { list ->
            list.map { if (it.id == originalTask.id) originalTask else it }
        }
    }
}
