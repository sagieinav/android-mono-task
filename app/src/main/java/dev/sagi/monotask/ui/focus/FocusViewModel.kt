package dev.sagi.monotask.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import dev.sagi.monotask.domain.util.BadgeEngine
import dev.sagi.monotask.domain.util.PriorityCalculator
import dev.sagi.monotask.ui.auth.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FocusViewModel(
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    // Private, writeable state-flow (only used by the ViewModel):
    private val _uiState = MutableStateFlow<FocusUiState>(FocusUiState.Loading)
    // Public, immutable state-flow (used/observed by the UI):
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    // Snooze options sheet state (separate from the main UI state)
    private val _showSnoozeSheet = MutableStateFlow(false)
    val showSnoozeSheet: StateFlow<Boolean> = _showSnoozeSheet.asStateFlow()

    init {
        observeTasks()
    }

    // Combines the task list and current workspace into one stream (for task ordering)
    private fun observeTasks() {
        viewModelScope.launch {
            // First get the active workspace (for now use first workspace)
            workspaceRepository.getWorkspaces(userId).collect { workspaces ->
                val workspace = workspaces.firstOrNull() ?: return@collect

                taskRepository.getActiveTasks(userId, workspace.id).collect { tasks ->
                    val topTask = PriorityCalculator.getTopTask(tasks, workspace)
                    val queue   = PriorityCalculator.getSortedTasks(tasks, workspace)

                    _uiState.value = if (topTask == null) {
                        FocusUiState.Empty
                    } else {
                        FocusUiState.Active(
                            focusTask  = topTask,
                            queue      = queue.drop(1), // exclude top task from queue preview
                            workspace  = workspace
                        )
                    }
                }
            }
        }
    }

    // User tapped "Complete" on the Focus Card:
    fun completeTask() {
        val state = _uiState.value as? FocusUiState.Active ?: return
        val workspace = state.workspace
        val task  = state.focusTask

        viewModelScope.launch {
            taskRepository.completeTask(userId, task.id)

            val xpGained = XpEvents.calculateCompletionXp(task)
            val userDoc  = userRepository.getUserOnce(userId) ?: return@launch

            // Add XP:
            userRepository.addXp(userId, xpGained, userDoc.xp, userDoc.level)

            // Log daily activity:
            userRepository.logDailyActivity(userId, xpGained, tasksCompleted = 1)

            // Fetch completed task history and evaluate badges
            val completedTasks = taskRepository.getCompletedTasksOnce(userId, workspace.id)
            val newBadges = BadgeEngine.evaluate(completedTasks)
            // TODO: persist newBadges to Firestore in a later task
        }
    }

    fun openSnoozeSheet() {
        _showSnoozeSheet.value = true
    }

    fun dismissSnoozeSheet() {
        _showSnoozeSheet.value = false
    }

    // User picked a snooze reason from the sheet.
    fun snoozeTask(penalty: Int) {
        val state = _uiState.value as? FocusUiState.Active ?: return
        val task  = state.focusTask

        viewModelScope.launch {
            // Deduct XP first (penalty is already negative)
            val userDoc = userRepository.getUserOnce(userId) ?: return@launch
            userRepository.addXp(userId, penalty, userDoc.xp, userDoc.level)

            // Increment snooze count. `isAce` becomes false after this
            taskRepository.snoozeTask(userId, task.id)

            _showSnoozeSheet.value = false
        }
    }
}

// ========== UI States ==========
sealed class FocusUiState {
    object Loading : FocusUiState()
    object Empty   : FocusUiState()  // all tasks done. Show celebration screen or something
    data class Active(
        val focusTask : Task,
        val queue     : List<Task>,
        val workspace : Workspace
    ) : FocusUiState()
}
