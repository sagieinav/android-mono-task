package dev.sagi.monotask.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.service.XpEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : TaskRepository {

    private fun tasksCollection(userId: String) =
        db.collection("users").document(userId).collection("tasks")

    private fun getTasks(
        userId: String,
        workspaceId: String,
        completed: Boolean
    ): Flow<List<Task>> =
        tasksCollection(userId)
            .whereEqualTo("workspaceId", workspaceId)
            .whereEqualTo("completed", completed)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(Task::class.java)?.copy(id = it.id)
                        ?: run { Log.w("TaskRepository", "Failed to deserialize task doc ${it.id}"); null }
                }
            }

    override fun getActiveTasks(userId: String, workspaceId: String): Flow<List<Task>> =
        getTasks(userId, workspaceId, completed = false)

    override suspend fun getActiveTasksOnce(userId: String, workspaceId: String): List<Task> {
        val result = tasksCollection(userId)
            .whereEqualTo("workspaceId", workspaceId)
            .whereEqualTo("completed", false)
            .get().await()
        return result.documents.mapNotNull {
            it.toObject(Task::class.java)?.copy(id = it.id)
                ?: run { Log.w("TaskRepository", "Failed to deserialize task doc ${it.id}"); null }
        }
    }

    override fun getCompletedTasks(userId: String, workspaceId: String): Flow<List<Task>> =
        getTasks(userId, workspaceId, completed = true)

    override suspend fun getCompletedTasksOnce(userId: String, workspaceId: String): List<Task> {
        val result = tasksCollection(userId)
            .whereEqualTo("workspaceId", workspaceId)
            .whereEqualTo("completed", true)
            .get().await()
        return result.documents.mapNotNull {
            it.toObject(Task::class.java)?.copy(id = it.id)
                ?: run { Log.w("TaskRepository", "Failed to deserialize task doc ${it.id}"); null }
        }
    }

    override fun getAllActiveTasks(userId: String): Flow<List<Task>> =
        tasksCollection(userId)
            .whereEqualTo("completed", false)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(Task::class.java)?.copy(id = it.id)
                        ?: run { Log.w("TaskRepository", "Failed to deserialize task doc ${it.id}"); null }
                }
            }

    override fun getAllCompletedTasks(userId: String): Flow<List<Task>> =
        tasksCollection(userId)
            .whereEqualTo("completed", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(Task::class.java)?.copy(id = it.id)
                        ?: run { Log.w("TaskRepository", "Failed to deserialize task doc ${it.id}"); null }
                }
            }

    override suspend fun getAllCompletedTasksOnce(userId: String): List<Task> {
        val result = tasksCollection(userId)
            .whereEqualTo("completed", true)
            .get().await()
        return result.documents.mapNotNull {
            it.toObject(Task::class.java)?.copy(id = it.id)
                ?: run { Log.w("TaskRepository", "Failed to deserialize task doc ${it.id}"); null }
        }
    }

    override suspend fun markTaskCompleted(userId: String, taskId: String) {
        tasksCollection(userId).document(taskId)
            .update(
                mapOf(
                    "completed"   to true,
                    "completedAt" to com.google.firebase.Timestamp.now()
                )
            ).await()
    }

    override suspend fun restoreTask(userId: String, taskId: String) {
        tasksCollection(userId).document(taskId)
            .update(
                mapOf(
                    "completed"   to false,
                    "completedAt" to null
                )
            ).await()
    }

    override suspend fun deleteTask(userId: String, taskId: String) {
        tasksCollection(userId).document(taskId).delete().await()
    }

    override suspend fun insertNewTask(userId: String, task: Task) {
        val taskWithXp = task.copy(currentXp = XpEngine.calculateTaskXp(task))
        tasksCollection(userId).add(taskWithXp).await()
    }

    override suspend fun updateSnoozeFields(userId: String, task: Task, option: XpEngine.SnoozeOption) {
        val newXp = XpEngine.calculateXpAfterSnooze(task, option)
        tasksCollection(userId).document(task.id)
            .update(mapOf(
                "snoozeCount" to com.google.firebase.firestore.FieldValue.increment(1),
                "currentXp"   to newXp
            )).await()
    }

    override suspend fun overwriteExistingTask(userId: String, task: Task) {
        val taskWithXp = task.copy(currentXp = XpEngine.calculateTaskXp(task))
        tasksCollection(userId).document(task.id).set(taskWithXp).await()
    }

    override suspend fun undoSnoozeFields(userId: String, originalTask: Task) {
        tasksCollection(userId).document(originalTask.id)
            .update(
                mapOf(
                    "snoozeCount" to originalTask.snoozeCount,
                    "currentXp"   to originalTask.currentXp
                )
            ).await()
    }
}
