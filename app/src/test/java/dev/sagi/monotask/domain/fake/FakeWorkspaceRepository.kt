package dev.sagi.monotask.domain.fake

import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeWorkspaceRepository : WorkspaceRepository {

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())

    var workspaces: List<Workspace>
        get() = _workspaces.value
        set(value) { _workspaces.value = value }

    // workspaceId → focused taskId
    val focusTaskIds = mutableMapOf<String, String?>()

    override fun getWorkspaces(userId: String): Flow<List<Workspace>> = _workspaces

    override suspend fun createDefaultWorkspaces(userId: String) {}

    override suspend fun createWorkspace(userId: String, name: String) {}

    override suspend fun deleteWorkspace(userId: String, workspaceId: String) {
        _workspaces.update { it.filter { w -> w.id != workspaceId } }
    }

    override suspend fun renameWorkspace(userId: String, workspaceId: String, newName: String) {}

    override suspend fun setFocusTask(userId: String, workspaceId: String, taskId: String?) {
        focusTaskIds[workspaceId] = taskId
    }
}
