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
import dev.sagi.monotask.domain.util.BadgeEngine
import dev.sagi.monotask.domain.util.PriorityCalculator
import dev.sagi.monotask.ui.focus.FocusUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KanbanViewModel(
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    private val _uiState = MutableStateFlow<KanbanUiState>(KanbanUiState.Loading)
    val uiState: StateFlow<KanbanUiState> = _uiState.asStateFlow()

    // Currently selected workspace: drives all task queries
    private val _selectedWorkspace = MutableStateFlow<Workspace?>(null)
    val selectedWorkspace: StateFlow<Workspace?> = _selectedWorkspace.asStateFlow()

    // All workspaces for the top bar dropdown
    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    // Controls the Add/Edit task bottom sheet
    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    init {
        observeWorkspaces()
    }

    private fun observeWorkspaces() {
        viewModelScope.launch {
            workspaceRepository.getWorkspaces(userId).collect { workspaces ->
                _workspaces.value = workspaces
                // Auto-select first workspace if none selected yet
                if (_selectedWorkspace.value == null && workspaces.isNotEmpty()) {
                    selectWorkspace(workspaces.first())
                }
            }
        }
    }

    fun selectWorkspace(workspace: Workspace) {
        _selectedWorkspace.value = workspace
        observeTasks(workspace)
    }

    // Observes tasks for the selected workspace. Groups them by Importance for the Kanban column layout
    private fun observeTasks(workspace: Workspace) {
        viewModelScope.launch {
            taskRepository.getActiveTasks(userId, workspace.id).collect { tasks ->
                val sorted = PriorityCalculator.getSortedTasks(tasks, workspace)
                val grouped = sorted.groupBy { it.importance }

                _uiState.value = KanbanUiState.Ready(
                    highTasks   = grouped[Importance.HIGH]   ?: emptyList(),
                    mediumTasks = grouped[Importance.MEDIUM] ?: emptyList(),
                    lowTasks    = grouped[Importance.LOW]    ?: emptyList()
                )
            }
        }
    }

// ========== Task Operations ==========

    fun addTask(
        title: String,
        description: String,
        importance: Importance,
        dueDate: com.google.firebase.Timestamp?,
        tags: List<String>
    ) {
        val workspace = _selectedWorkspace.value ?: return
        val task = Task(
            title       = title,
            description = description,
            importance  = importance,
            dueDate     = dueDate,
            workspaceId = workspace.id,
            tags        = tags,
            ownerId     = userId
        )
        viewModelScope.launch {
            taskRepository.addTask(userId, task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(userId, task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(userId, taskId)
        }
    }

    // User tapped "Complete" on a Kanban task:
    fun completeTask(task: Task) {
        val workspace = _selectedWorkspace.value ?: return  // get workspace from its own state

        viewModelScope.launch {
            taskRepository.completeTask(userId, task.id)

            val xpGained = XpEvents.calculateCompletionXp(task)
            val userDoc  = userRepository.getUserOnce(userId) ?: return@launch

            userRepository.addXp(userId, xpGained, userDoc.xp, userDoc.level)
            userRepository.logDailyActivity(userId, xpGained, tasksCompleted = 1)

            val completedTasks = taskRepository.getCompletedTasksOnce(userId, workspace.id)
            val newBadges = BadgeEngine.evaluate(completedTasks)
        }
    }



// ========== Edit Sheet ==========

    // Pass null to open a blank "Add Task" sheet
    fun openEditSheet(task: Task? = null) {
        _editingTask.value = task ?: Task()
    }

    fun dismissEditSheet() {
        _editingTask.value = null
    }
}

// ========== UI States ==========
sealed class KanbanUiState {
    object Loading : KanbanUiState()
    data class Ready(
        val highTasks   : List<Task>,
        val mediumTasks : List<Task>,
        val lowTasks    : List<Task>
    ) : KanbanUiState()
}
