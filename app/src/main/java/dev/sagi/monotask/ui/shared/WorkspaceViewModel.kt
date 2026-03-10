package dev.sagi.monotask.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserPrefsRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date

class WorkspaceViewModel(
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userPrefs: UserPrefsRepository = MonoTaskApp.instance.userPrefsRepository,
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private val _selectedWorkspace = MutableStateFlow<Workspace?>(null)
    val selectedWorkspace: StateFlow<Workspace?> = _selectedWorkspace.asStateFlow()

    init {
        if (userId.isNotEmpty()) observeWorkspaces()
    }


    // ========== WORKSPACE OPERATIONS ==========
    private fun observeWorkspaces() {
        workspaceRepository.getWorkspaces(userId)
            .onEach { workspaces ->
                _workspaces.value = workspaces
                val currentId = _selectedWorkspace.value?.id
                val freshCurrent = workspaces.firstOrNull { it.id == currentId }
                if (freshCurrent != null) {
                    // Always sync selected workspace with latest Firestore data
                    _selectedWorkspace.value = freshCurrent
                } else {
                    val lastId = userPrefs.getLastWorkspaceId()
                    _selectedWorkspace.value = workspaces.firstOrNull { it.id == lastId }
                        ?: workspaces.firstOrNull()
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectWorkspace(workspace: Workspace) {
        _selectedWorkspace.value = workspace
        viewModelScope.launch { userPrefs.saveLastWorkspaceId(workspace.id) }
    }

    fun createWorkspace(name: String) {
        viewModelScope.launch { workspaceRepository.createWorkspace(userId, name) }
    }

    fun deleteWorkspace(workspace: Workspace) {
        if (_workspaces.value.size <= 1) return  // prevent deleting last workspace
        viewModelScope.launch { workspaceRepository.deleteWorkspace(userId, workspace.id) }
    }


    // ========== TASK OPERATIONS ==========
    fun createTask(
        title: String,
        description: String,
        importance: Importance,
        tags: List<String>,
        dueDateMillis: Long?
    ) {
        val workspaceId = _selectedWorkspace.value?.id ?: return
        viewModelScope.launch {
            taskRepository.insertNewTask(userId, Task(
                title = title,
                description = description,
                importance = importance,
                tags = tags,
                dueDate = dueDateMillis?.let { Timestamp(Date(it)) },
                workspaceId = workspaceId,
                ownerId = userId,
                createdAt = Timestamp.now()
            )
            )
        }
    }
}