package dev.sagi.monotask.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WorkspaceRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : WorkspaceRepository {

    private fun workspacesCollection(userId: String) =
        db.collection("users").document(userId).collection("workspaces")

    override fun getWorkspaces(userId: String): Flow<List<Workspace>> =
        workspacesCollection(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull {
                    it.toObject(Workspace::class.java)?.copy(id = it.id)
                        ?: run { Log.w("WorkspaceRepository", "Failed to deserialize workspace doc ${it.id}"); null }
                }.sortedBy { it.createdAt }
            }

    override suspend fun createDefaultWorkspaces(userId: String) {
        val now = System.currentTimeMillis()
        val defaults = listOf(
            Workspace(name = "Personal",  ownerId = userId, createdAt = now),
            Workspace(name = "Education", ownerId = userId, createdAt = now + 1)
        )
        defaults.forEach { workspace ->
            workspacesCollection(userId).add(workspace).await()
        }
    }

    override suspend fun createWorkspace(userId: String, name: String) {
        val workspace = Workspace(name = name, ownerId = userId, createdAt = System.currentTimeMillis())
        workspacesCollection(userId).add(workspace).await()
    }

    override suspend fun deleteWorkspace(userId: String, workspaceId: String) {
        workspacesCollection(userId).document(workspaceId).delete().await()
    }

    override suspend fun renameWorkspace(userId: String, workspaceId: String, newName: String) {
        workspacesCollection(userId).document(workspaceId).update("name", newName).await()
    }

    override suspend fun setFocusTask(userId: String, workspaceId: String, taskId: String?) {
        workspacesCollection(userId)
            .document(workspaceId)
            .update("currentFocusTaskId", taskId)
            .await()
    }
}
