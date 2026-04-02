package dev.sagi.monotask.domain.repository

import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.service.XpEngine
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getActiveTasks(userId: String, workspaceId: String): Flow<List<Task>>
    suspend fun getActiveTasksOnce(userId: String, workspaceId: String): List<Task>
    fun getCompletedTasks(userId: String, workspaceId: String): Flow<List<Task>>
    suspend fun getCompletedTasksOnce(userId: String, workspaceId: String): List<Task>
    fun getAllActiveTasks(userId: String): Flow<List<Task>>
    fun getAllCompletedTasks(userId: String): Flow<List<Task>>
    suspend fun getAllCompletedTasksOnce(userId: String): List<Task>
    suspend fun markTaskCompleted(userId: String, taskId: String)
    suspend fun restoreTask(userId: String, taskId: String)
    suspend fun deleteTask(userId: String, taskId: String)
    suspend fun insertNewTask(userId: String, task: Task)
    suspend fun updateSnoozeFields(userId: String, task: Task, option: XpEngine.SnoozeOption)
    suspend fun overwriteExistingTask(userId: String, task: Task)
    suspend fun undoSnoozeFields(userId: String, originalTask: Task)
}
