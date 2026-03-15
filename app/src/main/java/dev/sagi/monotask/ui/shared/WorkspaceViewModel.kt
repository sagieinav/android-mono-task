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
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date

class WorkspaceViewModel(
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userPrefs: UserPrefsRepository = MonoTaskApp.instance.userPrefsRepository,
) : ViewModel() {

    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private val _selectedWorkspace = MutableStateFlow<Workspace?>(null)
    val selectedWorkspace: StateFlow<Workspace?> = _selectedWorkspace.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private lateinit var userId: String

    init {
        viewModelScope.launch {
            userId = AuthUtils.awaitUid()
            observeWorkspaces()
        }
    }

    // ========== Workspace Operations ==========

    private fun observeWorkspaces() {
        workspaceRepository.getWorkspaces(userId)
            .onEach { workspaces ->
                _workspaces.value = workspaces
                val currentId = _selectedWorkspace.value?.id
                val freshCurrent = workspaces.firstOrNull { it.id == currentId }
                if (freshCurrent != null) {
                    _selectedWorkspace.value = freshCurrent
                } else {
                    val lastId = userPrefs.getLastWorkspaceId()
                    _selectedWorkspace.value = workspaces.firstOrNull { it.id == lastId }
                        ?: workspaces.firstOrNull()
                }
            }
            .catch { e -> _errorEvent.emit("Failed to load workspaces: ${e.message}") }
            .launchIn(viewModelScope)
    }

    fun selectWorkspace(workspace: Workspace) {
        _selectedWorkspace.value = workspace
        viewModelScope.launch { userPrefs.saveLastWorkspaceId(workspace.id) }
    }

    fun createWorkspace(name: String) {
        viewModelScope.launch {
            try {
                workspaceRepository.createWorkspace(userId, name)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to create workspace: ${e.message}")
            }
        }
    }

    fun deleteWorkspace(workspace: Workspace) {
        if (_workspaces.value.size <= 1) return
        viewModelScope.launch {
            try {
                workspaceRepository.deleteWorkspace(userId, workspace.id)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to delete workspace: ${e.message}")
            }
        }
    }

    // ========== Task Operations ==========
    // TODO: Consider moving to a dedicated TaskViewModel

    fun createTask(
        title: String,
        description: String,
        importance: Importance,
        tags: List<String>,
        dueDateMillis: Long?
    ) {
        val workspaceId = _selectedWorkspace.value?.id ?: return
        viewModelScope.launch {
            try {
                taskRepository.insertNewTask(userId, Task(
                    title = title,
                    description = description,
                    importance = importance,
                    tags = tags,
                    dueDate = dueDateMillis?.let { Timestamp(Date(it)) },
                    workspaceId = workspaceId,
                    ownerId = userId,
                    createdAt = Timestamp.now()
                ))
            } catch (e: Exception) {
                _errorEvent.emit("Failed to create task: ${e.message}")
            }
        }
    }
}