package dev.sagi.monotask.ui.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
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
import javax.inject.Inject

@HiltViewModel
class KanbanViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val workspaceRepository: WorkspaceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<KanbanUiState>(KanbanUiState.Loading)
    val uiState: StateFlow<KanbanUiState> = _uiState.asStateFlow()

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    private val _showCompleted = MutableStateFlow(false)

    private val _uiEffect = MutableSharedFlow<KanbanUiEffect>()
    val uiEffect: SharedFlow<KanbanUiEffect> = _uiEffect.asSharedFlow()

    // Stored so focusNow() can reference the current workspace without extra params
    private var currentWorkspace: Workspace? = null

    private var userId: String = ""

    // Holds the workspace flow. Set once via [setWorkspaceSource], observation starts automatically.
    private val _workspaceSource = MutableStateFlow<StateFlow<Workspace?>?>(null)

    private var _userSource: StateFlow<User?>? = null
    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        viewModelScope.launch {
            userId = AuthUtils.awaitUid()
            observeTasks()
        }
    }

    // ========== Event Dispatcher ==========

    fun onEvent(event: KanbanEvent) {
        when (event) {
            is KanbanEvent.ToggleArchive   -> toggleArchive()
            is KanbanEvent.OpenEditSheet   -> openEditSheet(event.task)
            is KanbanEvent.DismissEditSheet -> dismissEditSheet()
            is KanbanEvent.UpdateTask      -> updateTask(event.task)
            is KanbanEvent.DeleteTask      -> deleteTask(event.taskId)
            is KanbanEvent.FocusNow        -> focusNow(event.task)
            is KanbanEvent.RestoreTask     -> restoreTask(event.task)
        }
    }

    // ========== Workspace & User Wiring ==========

    // Connects this ViewModel to the shared workspace selection.
    // Call once right after creation (in NavGraph).
    fun setWorkspaceSource(workspaceFlow: StateFlow<Workspace?>) {
        _workspaceSource.compareAndSet(null, workspaceFlow)
    }

    fun setUserSource(userFlow: StateFlow<User?>) {
        if (_userSource == null) {
            _userSource = userFlow
            viewModelScope.launch { userFlow.collect { _currentUser.value = it } }
        }
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

    private fun toggleArchive() {
        _uiState.value = KanbanUiState.Loading
        _showCompleted.value = !_showCompleted.value
    }

    private fun updateUiState(tasks: List<Task>, workspace: Workspace?) {
        val dueDateWeight = _currentUser.value?.dueDateWeight ?: 0.5f
        val sorted  = if (workspace != null) TaskSelector.getSortedTasks(tasks, dueDateWeight) else tasks
        val grouped = sorted.groupBy { it.importance }
        _uiState.value = KanbanUiState.Ready(
            highTasks   = grouped[Importance.HIGH]   ?: emptyList(),
            mediumTasks = grouped[Importance.MEDIUM] ?: emptyList(),
            lowTasks    = grouped[Importance.LOW]    ?: emptyList(),
            isArchive   = _showCompleted.value
        )
    }

    // ========== Task Actions ==========

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.overwriteExistingTask(userId, task)
            } catch (e: Exception) {
                _uiEffect.emit(KanbanUiEffect.ShowError("Failed to update task: ${e.message}"))
            }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(userId, taskId)
            } catch (e: Exception) {
                _uiEffect.emit(KanbanUiEffect.ShowError("Failed to delete task: ${e.message}"))
            }
        }
    }

    private fun focusNow(task: Task) {
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
                _uiEffect.emit(KanbanUiEffect.NavigateToFocus)
            } catch (e: Exception) {
                _uiEffect.emit(KanbanUiEffect.ShowError("Failed to set focus task: ${e.message}"))
            }
        }
    }

    private fun restoreTask(task: Task) {
        viewModelScope.launch {
            try {
                val xpToRemove       = task.currentXp
                val completionEpoch  = task.completedAt
                    ?.toDate()
                    ?.toInstant()
                    ?.atZone(java.time.ZoneId.systemDefault())
                    ?.toLocalDate()
                    ?.toEpochDay()
                    ?: java.time.LocalDate.now().toEpochDay()
                taskRepository.restoreTask(userId, task.id)
                val user = userRepository.getUserOnce(userId) ?: run {
                    _uiEffect.emit(KanbanUiEffect.ShowError("Failed to load user profile for XP rollback"))
                    return@launch
                }
                userRepository.removeDailyActivity(userId, xpToRemove, dateEpochDay = completionEpoch)
                userRepository.removeXp(userId, xpToRemove)
            } catch (e: Exception) {
                _uiEffect.emit(KanbanUiEffect.ShowError("Failed to restore task: ${e.message}"))
            }
        }
    }

    // ========== Edit Sheet ==========

    private fun openEditSheet(task: Task?) { _editingTask.value = task ?: Task() }
    private fun dismissEditSheet()         { _editingTask.value = null }

    // ========== Lifecycle ==========

    override fun onCleared() {
        super.onCleared()
        // viewModelScope is canceled here, stopping the active Firestore task stream
        // launched via launchIn(viewModelScope) in observeTasks()
    }
}
