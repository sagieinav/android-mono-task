package dev.sagi.monotask.ui.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import dev.sagi.monotask.domain.util.TaskSelector
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ========== UI States ==========
sealed class KanbanUiState {
    object Loading : KanbanUiState()
    data class Ready(
        val highTasks: List<Task>,
        val mediumTasks: List<Task>,
        val lowTasks: List<Task>,
        val isArchive: Boolean
    ) : KanbanUiState()
}

class KanbanViewModel(
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<KanbanUiState>(KanbanUiState.Loading)
    val uiState: StateFlow<KanbanUiState> = _uiState.asStateFlow()

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    private val _showCompleted = MutableStateFlow(false)

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    // Stored so focusNow() can reference the current workspace without extra params
    private var currentWorkspace: Workspace? = null

    private lateinit var userId: String

    // Holds the workspace flow. Set once via [setWorkspaceSource], observation starts automatically.
    private val _workspaceSource = MutableStateFlow<StateFlow<Workspace?>?>(null)

    init {
        viewModelScope.launch {
            userId = AuthUtils.awaitUid()
            observeTasks()
        }
    }

    // ========== Workspace Wiring ==========

    // Connects this ViewModel to the shared workspace selection.
    // Call once right after creation (in NavGraph).
    fun setWorkspaceSource(workspaceFlow: StateFlow<Workspace?>) {
        _workspaceSource.compareAndSet(null, workspaceFlow)
    }

    // ========== Task Observation ==========

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTasks() {
        _workspaceSource
            .filterNotNull()
            .flatMapLatest { workspaceFlow ->
                combine(workspaceFlow, _showCompleted) { workspace, showCompleted ->
                    Pair(workspace, showCompleted)
                }
            }
            .flatMapLatest { (workspace, showCompleted) ->
                currentWorkspace = workspace
                when {
                    workspace == null -> flowOf(Pair(null, emptyList()))
                    showCompleted     -> taskRepository.getCompletedTasks(userId, workspace.id).map { Pair(workspace, it) }
                    else              -> taskRepository.getActiveTasks(userId, workspace.id).map { Pair(workspace, it) }
                }
            }
            .onEach { (workspace, tasks) -> updateUiState(tasks, workspace) }
            .launchIn(viewModelScope)
    }

    fun toggleArchive() {
        _uiState.value = KanbanUiState.Loading
        _showCompleted.value = !_showCompleted.value
    }

    private fun updateUiState(tasks: List<Task>, workspace: Workspace?) {
        val sorted  = if (workspace != null) TaskSelector.getSortedTasks(tasks, workspace) else tasks
        val grouped = sorted.groupBy { it.importance }
        _uiState.value = KanbanUiState.Ready(
            highTasks   = grouped[Importance.HIGH]   ?: emptyList(),
            mediumTasks = grouped[Importance.MEDIUM] ?: emptyList(),
            lowTasks    = grouped[Importance.LOW]    ?: emptyList(),
            isArchive   = _showCompleted.value
        )
    }

// ========== Task Actions ==========

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.overwriteExistingTask(userId, task)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to update task: ${e.message}")
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(userId, taskId)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to delete task: ${e.message}")
            }
        }
    }

    fun focusNow(task: Task) {
        val workspace = currentWorkspace ?: return
        viewModelScope.launch {
            try {
                workspace.currentFocusTaskId?.let { currentId ->
                    if (currentId != task.id) {
                        val allTasks = taskRepository.getActiveTasksOnce(userId, workspace.id)
                        val currentTask = allTasks.find { it.id == currentId }
                        currentTask?.let {
                            taskRepository.updateSnoozeFields(userId, it, XpEvents.SnoozeOption.MANUAL)
                        }
                    }
                }
                workspaceRepository.setFocusTask(userId, workspace.id, task.id)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to set focus task: ${e.message}")
            }
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            try {
                val xpToRemove = task.currentXp
                taskRepository.restoreTask(userId, task.id)
                val user = userRepository.getUserOnce(userId) ?: run {
                    _errorEvent.emit("Failed to load user profile for XP rollback")
                    return@launch
                }
//                userRepository.addXp(userId, -task.currentXp, user.xp, user.level)
                userRepository.removeDailyActivity(userId, xpToRemove)
                userRepository.removeXp(userId, xpToRemove)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to restore task: ${e.message}")
            }
        }
    }

// ========== Edit Sheet ==========

    fun openEditSheet(task: Task? = null) { _editingTask.value = task ?: Task() }
    fun dismissEditSheet()               { _editingTask.value = null }
}
