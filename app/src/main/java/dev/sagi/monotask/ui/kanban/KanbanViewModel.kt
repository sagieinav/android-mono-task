package dev.sagi.monotask.ui.kanban

import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.service.XpEngine
import dev.sagi.monotask.domain.usecase.RestoreCompletedTaskUseCase
import dev.sagi.monotask.domain.usecase.SetTaskFocusUseCase
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KanbanViewModel @Inject constructor(
    private val taskRepository : TaskRepository,
    private val setTaskFocusUseCase : SetTaskFocusUseCase,
    private val restoreCompletedTaskUseCase : RestoreCompletedTaskUseCase,
) : BaseViewModel<KanbanUiState, KanbanEvent, KanbanUiEffect>() {

    override val initialState: KanbanUiState = KanbanUiState.Loading

    // KanbanViewModel derives its public uiState from three internal flows combined,
    // so it overrides BaseViewModel's uiState with a custom StateFlow.
    private val _internalUiState = MutableStateFlow<KanbanUiState>(KanbanUiState.Loading)
    private val _isLocked = MutableStateFlow(false)
    private val _sortOrder = MutableStateFlow(SortOrder.CREATED_DESC)

    override val uiState: StateFlow<KanbanUiState> = combine(_internalUiState, _isLocked, _sortOrder) { state, locked, sortOrder ->
        if (locked) KanbanUiState.Locked
        else when (state) {
            is KanbanUiState.Ready -> state.copy(
                highTasks = state.highTasks.applySortOrder(sortOrder),
                mediumTasks = state.mediumTasks.applySortOrder(sortOrder),
                lowTasks = state.lowTasks.applySortOrder(sortOrder),
                sortOrder = sortOrder
            )
            else -> state
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KanbanUiState.Loading)

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    private val _showCompleted = MutableStateFlow(false)

    // Stored so focusNow() can reference the current workspace without extra params
    private var currentWorkspace: Workspace? = null

    private var userId: String = ""

    // Holds the workspace flow. Set once via [setWorkspaceSource], observation starts automatically.
    private val _workspaceSource = MutableStateFlow<StateFlow<Workspace?>?>(null)

    private var _userSource: StateFlow<User?>? = null

    init {
        viewModelScope.launch {
            userId = AuthUtils.awaitUid()
            observeTasks()
        }
    }

    // ========== Event Dispatcher ==========

    override fun onEvent(event: KanbanEvent) {
        when (event) {
            is KanbanEvent.ToggleArchive -> toggleArchive()
            is KanbanEvent.ResetArchive -> resetArchive()
            is KanbanEvent.OpenEditSheet -> openEditSheet(event.task)
            is KanbanEvent.DismissEditSheet -> dismissEditSheet()
            is KanbanEvent.UpdateTask -> updateTask(event.task)
            is KanbanEvent.DeleteTask -> deleteTask(event.taskId)
            is KanbanEvent.FocusNow -> focusNow(event.task)
            is KanbanEvent.RestoreTask -> restoreTask(event.task)
        }
    }

    // ========== Workspace & User Wiring ==========

    fun setLocked(locked: Boolean) { _isLocked.value = locked }

    // Connects this ViewModel to the shared workspace selection.
    // Call once right after creation (in NavGraph).
    fun setWorkspaceSource(workspaceFlow: StateFlow<Workspace?>) {
        _workspaceSource.compareAndSet(null, workspaceFlow)
    }

    fun setUserSource(userFlow: StateFlow<User?>) {
        if (_userSource == null) {
            _userSource = userFlow
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
                    showCompleted -> taskRepository.getCompletedTasks(userId, workspace.id).map { Pair(workspace, it) }
                    else -> taskRepository.getActiveTasks(userId, workspace.id).map { Pair(workspace, it) }
                }
            }
            .onEach { (workspace, tasks) -> updateUiState(tasks, workspace) }
            .launchIn(viewModelScope)
    }

    private fun toggleArchive() {
        _internalUiState.value = KanbanUiState.Loading
        _showCompleted.value = !_showCompleted.value
    }

    private fun resetArchive() {
        if (_showCompleted.value) {
            _internalUiState.value = KanbanUiState.Loading
            _showCompleted.value = false
        }
    }

    private fun updateUiState(tasks: List<Task>, workspace: Workspace?) {
        val grouped = tasks.groupBy { it.importance }
        _internalUiState.value = KanbanUiState.Ready(
            highTasks = grouped[Importance.HIGH] ?: emptyList(),
            mediumTasks = grouped[Importance.MEDIUM] ?: emptyList(),
            lowTasks = grouped[Importance.LOW] ?: emptyList(),
            isArchive = _showCompleted.value
        )
    }

    // ========== Sort Order ==========

    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }

    private fun List<Task>.applySortOrder(order: SortOrder): List<Task> = when (order) {
        SortOrder.DUE_ASC  -> {
            val hasDue = filter { it.dueDate != null }.sortedBy { it.dueDate!!.seconds }
            val noDue = filter { it.dueDate == null }.sortedByDescending { it.createdAt.seconds }
            hasDue + noDue
        }
        SortOrder.DUE_DESC -> {
            val hasDue = filter { it.dueDate != null }.sortedByDescending { it.dueDate!!.seconds }
            val noDue = filter { it.dueDate == null }.sortedByDescending { it.createdAt.seconds }
            hasDue + noDue
        }
        SortOrder.CREATED_ASC  -> sortedBy { it.createdAt.seconds }
        SortOrder.CREATED_DESC -> sortedByDescending { it.createdAt.seconds }
    }

    // ========== Task Actions ==========

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.overwriteExistingTask(userId, task.copy(currentXp = XpEngine.calculateTaskXp(task)))
            } catch (e: Exception) {
                sendEffect(KanbanUiEffect.ShowError("Failed to update task: ${e.message}"))
            }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(userId, taskId)
            } catch (e: Exception) {
                sendEffect(KanbanUiEffect.ShowError("Failed to delete task: ${e.message}"))
            }
        }
    }

    private fun focusNow(task: Task) {
        val workspace = currentWorkspace ?: return
        viewModelScope.launch {
            try {
                setTaskFocusUseCase(userId, task, workspace)
                sendEffect(KanbanUiEffect.NavigateToFocus)
            } catch (e: Exception) {
                sendEffect(KanbanUiEffect.ShowError("Failed to set focus task: ${e.message}"))
            }
        }
    }

    private fun restoreTask(task: Task) {
        viewModelScope.launch {
            try {
                restoreCompletedTaskUseCase(userId, task)
            } catch (e: Exception) {
                sendEffect(KanbanUiEffect.ShowError("Failed to restore task: ${e.message}"))
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
