package dev.sagi.monotask.domain.repository

import dev.sagi.monotask.data.model.Workspace
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
    fun getWorkspaces(userId: String): Flow<List<Workspace>>
    suspend fun createDefaultWorkspaces(userId: String)
    suspend fun createWorkspace(userId: String, name: String)
    suspend fun deleteWorkspace(userId: String, workspaceId: String)
    suspend fun renameWorkspace(userId: String, workspaceId: String, newName: String)
    suspend fun setFocusTask(userId: String, workspaceId: String, taskId: String?)
}
