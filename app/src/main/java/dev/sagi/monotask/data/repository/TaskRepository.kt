package dev.sagi.monotask.data.repository

import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.domain.util.XpEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TaskRepository {

    private val db = MonoTaskApp.instance.db

    // Returns the Firestore collection for a specific user's tasks
    // All task operations are scoped under the user's document
    private fun tasksCollection(userId: String) =
        db.collection("users").document(userId).collection("tasks")


    // Internal function: single source of truth for task queries
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
                }
            }

    // Returns live stream of active tasks for the Focus Hub + Kanban
    fun getActiveTasks(userId: String, workspaceId: String): Flow<List<Task>> =
        getTasks(userId, workspaceId, completed = false)

    suspend fun getActiveTasksOnce(userId: String, workspaceId: String): List<Task> {
        val result = tasksCollection(userId)
            .whereEqualTo("workspaceId", workspaceId)
            .whereEqualTo("completed", false)
            .get().await()
        return result.documents.mapNotNull {
            it.toObject(Task::class.java)?.copy(id = it.id)
        }
    }

    // Returns completed tasks for the Archive view
    fun getCompletedTasks(userId: String, workspaceId: String): Flow<List<Task>> =
        getTasks(userId, workspaceId, completed = true)

    // One-shot fetch of completed tasks for a specific workspace (used by BadgeEngine)
    suspend fun getCompletedTasksOnce(userId: String, workspaceId: String): List<Task> {
        val result = tasksCollection(userId)
            .whereEqualTo("workspaceId", workspaceId)
            .whereEqualTo("completed", true)
            .get()
            .await()
        return result.documents.mapNotNull {
            it.toObject(Task::class.java)?.copy(id = it.id)
        }
    }

    // Live stream of ALL completed tasks across every workspace
    fun getAllCompletedTasks(userId: String): Flow<List<Task>> =
        tasksCollection(userId)
            .whereEqualTo("completed", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(Task::class.java)?.copy(id = it.id)
                }
            }

    // One-shot fetch of ALL completed tasks across every workspace
    suspend fun getAllCompletedTasksOnce(userId: String): List<Task> {
        val result = tasksCollection(userId)
            .whereEqualTo("completed", true)
            .get()
            .await()
        return result.documents.mapNotNull {
            it.toObject(Task::class.java)?.copy(id = it.id)
        }
    }

    // Marks a task as complete. Moves it to archive
    suspend fun markTaskCompleted(userId: String, taskId: String) {
        tasksCollection(userId).document(taskId)
            .update(
                mapOf(
                    "completed"   to true,
                    "completedAt" to com.google.firebase.Timestamp.now()
                )
            )
            .await()
    }

    // Restores a completed task back to active. Inverse of markTaskCompleted
    suspend fun restoreTask(userId: String, taskId: String) {
        tasksCollection(userId).document(taskId)
            .update(
                mapOf(
                    "completed"   to false,
                    "completedAt" to null
                )
            ).await()
    }

    // Permanently deletes a task
    suspend fun deleteTask(userId: String, taskId: String) {
        tasksCollection(userId).document(taskId)
            .delete()
            .await()
    }

    // insertNewTask now stores initial XP
    suspend fun insertNewTask(userId: String, task: Task) {
        val taskWithXp = task.copy(currentXp = XpEvents.calculateTaskXp(task))
        tasksCollection(userId).add(taskWithXp).await()
    }

    // Snoozes task and recalculates its XP. No user XP touched here!
    suspend fun updateSnoozeFields(userId: String, task: Task, option: XpEvents.SnoozeOption) {
        val newXp = XpEvents.calculateXpAfterSnooze(task, option)
        tasksCollection(userId).document(task.id)
            .update(mapOf(
                "snoozeCount" to com.google.firebase.firestore.FieldValue.increment(1),
                "currentXp"   to newXp
            )).await()
    }

    // overwriteExistingTask recalculates XP from edited properties
    suspend fun overwriteExistingTask(userId: String, task: Task) {
        val taskWithXp = task.copy(currentXp = XpEvents.calculateTaskXp(task))
        tasksCollection(userId).document(task.id).set(taskWithXp).await()
    }

    // Reverts a snooze action by restoring the previous snooze count and XP from the cached task
    suspend fun undoSnoozeFields(userId: String, originalTask: Task) {
        tasksCollection(userId).document(originalTask.id)
            .update(
                mapOf(
                    "snoozeCount" to originalTask.snoozeCount,
                    "currentXp"   to originalTask.currentXp
                )
            ).await()
    }

}
