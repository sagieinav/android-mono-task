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

    // One-shot fetch of completed tasks (used by BadgeEngine after completion)
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


//    // Adds a new task. Firestore auto-generates the document ID
//    suspend fun addTask(userId: String, task: Task) {
//        tasksCollection(userId).add(task).await()
//    }
//
//    // Increments snooze count. Called when user snoozes the focus task
//    suspend fun snoozeTask(userId: String, taskId: String) {
//        tasksCollection(userId).document(taskId)
//            .update("snoozeCount", com.google.firebase.firestore.FieldValue.increment(1))
//    }
//
//    // Full task update (used by the Edit Task sheet)
//    suspend fun updateTask(userId: String, task: Task) {
//        tasksCollection(userId).document(task.id).set(task)
//    }

    // Marks a task as complete. Moves it to archive
    suspend fun markTaskCompleted(userId: String, taskId: String) {
        tasksCollection(userId).document(taskId)
            .update(
                mapOf(
                    "completed" to true,
                    "completedAt" to com.google.firebase.Timestamp.now()
                )
            )
    }

    // Permanently deletes a task
    suspend fun deleteTask(userId: String, taskId: String) {
        tasksCollection(userId).document(taskId).delete()
    }


    // insertNewTask now stores initial XP
    suspend fun insertNewTask(userId: String, task: Task) {
        val taskWithXp = task.copy(currentXp = XpEvents.xpForTask(task))
        tasksCollection(userId).add(taskWithXp).await()
    }

    // Snoozes task and recalculates its XP. No user XP touched here!
    suspend fun updateSnoozeFields(userId: String, task: Task, option: XpEvents.SnoozeOption) {
        val newXp = XpEvents.xpAfterSnooze(task, option)
        tasksCollection(userId).document(task.id)
            .update(mapOf(
                "snoozeCount" to com.google.firebase.firestore.FieldValue.increment(1),
                "currentXp"   to newXp
            )).await()
    }


    // overwriteExistingTask recalculates XP from edited properties
    suspend fun overwriteExistingTask(userId: String, task: Task) {
        val taskWithXp = task.copy(currentXp = XpEvents.xpForTask(task))
        tasksCollection(userId).document(task.id).set(taskWithXp).await()
    }

}
