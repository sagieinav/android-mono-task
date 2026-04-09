package dev.sagi.monotask.data.demo

import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class DemoWorkspaceRepository @Inject constructor() : WorkspaceRepository {

    private val _workspaces = MutableStateFlow(DemoSeedData.DEMO_WORKSPACES.toMutableList())

    override fun getWorkspaces(userId: String): Flow<List<Workspace>> =
        _workspaces.map { it.toList() }

    override suspend fun createDefaultWorkspaces(userId: String) = Unit

    override suspend fun createWorkspace(userId: String, name: String) {
        val newWs = Workspace(
            id = "demo_ws_${System.currentTimeMillis()}",
            name = name,
            ownerId = DemoSeedData.DEMO_USER_ID,
            createdAt = System.currentTimeMillis()
        )
        _workspaces.update { list -> (list + newWs).toMutableList() }
    }

    override suspend fun deleteWorkspace(userId: String, workspaceId: String) {
        _workspaces.update { list -> list.filter { it.id != workspaceId }.toMutableList() }
    }

    override suspend fun renameWorkspace(userId: String, workspaceId: String, newName: String) {
        _workspaces.update { list ->
            list.map { if (it.id == workspaceId) it.copy(name = newName) else it }.toMutableList()
        }
    }

    override suspend fun setFocusTask(userId: String, workspaceId: String, taskId: String?) {
        _workspaces.update { list ->
            list.map { if (it.id == workspaceId) it.copy(currentFocusTaskId = taskId) else it }.toMutableList()
        }
    }
}
