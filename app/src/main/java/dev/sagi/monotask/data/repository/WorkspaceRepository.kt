package dev.sagi.monotask.data.repository

import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Workspace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WorkspaceRepository {

    private val db = MonoTaskApp.instance.db

    private fun workspacesCollection(userId: String) =
        db.collection("users").document(userId).collection("workspaces")

    // Real-time stream of all workspaces belonging to the user
    fun getWorkspaces(userId: String): Flow<List<Workspace>> =
        workspacesCollection(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(Workspace::class.java)?.copy(id = it.id)
                }
            }

    // Creates a default workspace set on first login
    // Called alongside createUserIfNotExists in AuthViewModel
    suspend fun createDefaultWorkspaces(userId: String) {
        val defaults = listOf(
            Workspace(name = "Personal",    ownerId = userId),
            Workspace(name = "University",  ownerId = userId)
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
        dueDateWeight: Float,
        importanceWeight: Float,
        randomnessFactor: Float
    ) {
        workspacesCollection(userId).document(workspaceId).update(
            mapOf(
                "dueDateWeight"    to dueDateWeight,
                "importanceWeight" to importanceWeight,
                "randomnessFactor" to randomnessFactor
            )
        ).await()
    }

    // Adds a new custom workspace
    suspend fun addWorkspace(userId: String, name: String) {
        val workspace = Workspace(name = name, ownerId = userId)
        workspacesCollection(userId).add(workspace).await()
    }
}
