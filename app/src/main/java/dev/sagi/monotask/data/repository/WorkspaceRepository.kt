package dev.sagi.monotask.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.data.model.Workspace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WorkspaceRepository(private val db: FirebaseFirestore) {

    private fun workspacesCollection(userId: String) =
        db.collection("users").document(userId).collection("workspaces")

    // Real-time stream of all workspaces belonging to the user
    fun getWorkspaces(userId: String): Flow<List<Workspace>> =
        workspacesCollection(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(Workspace::class.java)?.copy(id = it.id)
                        ?: run { Log.w("WorkspaceRepository", "Failed to deserialize workspace doc ${it.id}"); null }
                }
            }

    // Creates a default workspace set on first login
    // Called alongside createUserIfNotExists in AuthViewModel
    suspend fun createDefaultWorkspaces(userId: String) {
        val defaults = listOf(
            Workspace(name = "Personal",   ownerId = userId),
            Workspace(name = "Education",  ownerId = userId)
        )
        defaults.forEach { workspace ->
            workspacesCollection(userId).add(workspace).await()
        }
    }

    // Updates the priority weights for a workspace
    // Called from the Settings screen sliders
    suspend fun updateWeights(
        userId: String,
        workspaceId: String,
        dueDateWeight: Float? = null,
        importanceWeight: Float? = null
    ) {
        val updates = mutableMapOf<String, Any>()

        // Only add to the map if the value is NOT null (only selected parameters)
        dueDateWeight?.let { updates["dueDateWeight"] = it }
        importanceWeight?.let { updates["importanceWeight"] = it }

        if (updates.isNotEmpty()) {
            workspacesCollection(userId).document(workspaceId).update(updates).await()
        }
    }

    // Adds a new custom workspace
    suspend fun createWorkspace(userId: String, name: String) {
        val workspace = Workspace(name = name, ownerId = userId)
        workspacesCollection(userId).add(workspace).await()
    }

    suspend fun deleteWorkspace(userId: String, workspaceId: String) {
        workspacesCollection(userId).document(workspaceId).delete().await()
    }

    suspend fun setFocusTask(userId: String, workspaceId: String, taskId: String?) {
        workspacesCollection(userId)
            .document(workspaceId)
            .update("currentFocusTaskId", taskId).await()
    }
}
