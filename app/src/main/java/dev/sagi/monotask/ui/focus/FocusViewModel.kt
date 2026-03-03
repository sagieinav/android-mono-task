package dev.sagi.monotask.ui.focus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserPrefsRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import dev.sagi.monotask.domain.util.BadgeEngine
import dev.sagi.monotask.domain.util.PriorityCalculator
import dev.sagi.monotask.ui.auth.AuthUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FocusViewModel(
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
    private val userPrefs: UserPrefsRepository = MonoTaskApp.instance.userPrefsRepository,
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    // ========== STATE DECLARATIONS ==========
    // Private, writeable state-flow (only used by the ViewModel):
    private val _uiState = MutableStateFlow<FocusUiState>(FocusUiState.Loading)
    // Public, immutable state-flow (used/observed by the UI):
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private val _currentWorkspace = MutableStateFlow<Workspace?>(null)
    val currentWorkspace: StateFlow<Workspace?> = _currentWorkspace.asStateFlow()
    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    // Snooze options sheet state (separate from the main UI state)
    private val _showSnoozeSheet = MutableStateFlow(false)
    val showSnoozeSheet: StateFlow<Boolean> = _showSnoozeSheet.asStateFlow()

    private val _xpBadgeVisible = MutableStateFlow(false)
    val xpBadgeVisible: StateFlow<Boolean> = _xpBadgeVisible.asStateFlow()
    private val _lastXpGained = MutableStateFlow(0)
    val lastXpGained: StateFlow<Int> = _lastXpGained.asStateFlow()


    init {
        // Only observe tasks if user is logged in
        if (userId.isNotEmpty()) {
            observeTasks()
        }
    }

    // Combines the task list and current workspace into one stream (for task ordering)
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTasks() {
        // Flow 1: keep workspace list updated, set default if current becomes invalid
        workspaceRepository.getWorkspaces(userId)
            .onEach { workspaces ->
                _workspaces.value = workspaces
                val isCurrentValid = workspaces.any { it.id == _currentWorkspace.value?.id }
                if (!isCurrentValid) {
                    // Try to restore last used, fallback to first
                    val lastId = userPrefs.getLastWorkspaceId()
                    val restored = workspaces.firstOrNull { it.id == lastId }
                        ?: workspaces.firstOrNull()
                    _currentWorkspace.value = restored
                }
            }
            .launchIn(viewModelScope)

        // Flow 2: re-fetch tasks whenever _currentWorkspace changes
        _currentWorkspace
            .flatMapLatest { workspace ->
                if (workspace == null) flowOf(emptyList())
                else taskRepository.getActiveTasks(userId, workspace.id)
            }
            .onEach { tasks ->
                val workspace = _currentWorkspace.value
                if (workspace == null) {
                    _uiState.value = FocusUiState.Empty
                    return@onEach
                }
                val topTask = PriorityCalculator.getTopTask(tasks, workspace)
                val queue = PriorityCalculator.getSortedTasks(tasks, workspace)
                _uiState.value = if (topTask == null) FocusUiState.Empty
                else FocusUiState.Active(topTask, queue.drop(1), workspace)
            }
            .launchIn(viewModelScope)
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
            // XP Badge animation:
            _lastXpGained.value = xpGained
            _xpBadgeVisible.value = true
            delay(2000)
            _xpBadgeVisible.value = false

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

    fun createTask(
        title: String,
        description: String,
        importance: Importance,
        tags: List<String>,
        dueDateMillis: Long?,
        workspaceId: String
    ) {
        viewModelScope.launch {
            try {
                val task = Task(
                    title = title,
                    description = description,
                    importance = importance,
                    tags = tags,
                    dueDate = dueDateMillis?.let { com.google.firebase.Timestamp(java.util.Date(it)) },
                    workspaceId = workspaceId,
                    ownerId = userId,
                    createdAt = com.google.firebase.Timestamp.now()
                )
                Log.d("CreateTask", "Task created successfully")
                taskRepository.addTask(userId, task)
            }
            catch (e: Exception) {
                Log.e("CreateTask", "Failed: ${e.message}", e)
            }
        }
    }


    fun createWorkspace(name: String) {
        viewModelScope.launch {
            try {
                workspaceRepository.createWorkspace(userId, name.trim())
            } catch (e: Exception) {
                Log.e("CreateWorkspace", "❌ Failed: ${e.message}", e)
            }
        }
    }

    fun deleteWorkspace(workspace: Workspace) {
        if (_workspaces.value.size <= 1) return  // silently block, or show a snackbar
        viewModelScope.launch {
            try {
                workspaceRepository.deleteWorkspace(userId, workspace.id)
            } catch (e: Exception) {
                Log.e("DeleteWorkspace", "Failed: ${e.message}", e)
            }
        }
    }


    fun selectWorkspace(workspace: Workspace) {
        _currentWorkspace.value = workspace
        viewModelScope.launch {
            userPrefs.saveLastWorkspaceId(workspace.id)
        }
//        observeTasks()
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
