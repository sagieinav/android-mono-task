package dev.sagi.monotask.ui.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.domain.util.BadgeEngine
import dev.sagi.monotask.domain.util.PriorityCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class KanbanUiState {
    object Loading : KanbanUiState()
    data class Ready(
        val highTasks: List<Task>,
        val mediumTasks: List<Task>,
        val lowTasks: List<Task>
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
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()

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
                    showCompleted -> taskRepository.getCompletedTasks(userId, workspace.id)
                        .map { Pair(workspace, it) }
                    else -> taskRepository.getActiveTasks(userId, workspace.id)
                        .map { Pair(workspace, it) }
                }
            }
            .onEach { (workspace, tasks) -> updateUiState(tasks, workspace) }
            .launchIn(viewModelScope)
    }

    fun toggleArchive() {
        _showCompleted.value = !_showCompleted.value
    }

    private fun updateUiState(tasks: List<Task>, workspace: Workspace?) {
        val sorted = if (workspace != null) PriorityCalculator.getSortedTasks(tasks, workspace) else tasks
        val grouped = sorted.groupBy { it.importance }
        _uiState.value = KanbanUiState.Ready(
            highTasks   = grouped[Importance.HIGH]   ?: emptyList(),
            mediumTasks = grouped[Importance.MEDIUM] ?: emptyList(),
            lowTasks    = grouped[Importance.LOW]    ?: emptyList()
        )
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { taskRepository.updateTask(userId, task) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { taskRepository.deleteTask(userId, taskId) }
    }

    fun openEditSheet(task: Task? = null) { _editingTask.value = task ?: Task() }
    fun dismissEditSheet() { _editingTask.value = null }
}
