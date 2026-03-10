package dev.sagi.monotask.ui.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.domain.util.TaskSelector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    private val _uiState = MutableStateFlow<KanbanUiState>(KanbanUiState.Loading)
    val uiState: StateFlow<KanbanUiState> = _uiState.asStateFlow()

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    private val _showCompleted = MutableStateFlow(false)

    private var tasksObserved = false

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startObservingTasks(selectedWorkspace: StateFlow<Workspace?>) {
        if (tasksObserved) return
        tasksObserved = true

        combine(selectedWorkspace, _showCompleted) { workspace, showCompleted ->
            Pair(workspace, showCompleted)
        }
            .flatMapLatest { (workspace, showCompleted) ->
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

    fun updateTask(task: Task) {
        viewModelScope.launch { taskRepository.overwriteExistingTask(userId, task) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { taskRepository.deleteTask(userId, taskId) }
    }

    fun openEditSheet(task: Task? = null) { _editingTask.value = task ?: Task() }
    fun dismissEditSheet()                { _editingTask.value = null }
}
