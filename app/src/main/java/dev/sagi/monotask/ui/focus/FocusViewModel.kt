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
import dev.sagi.monotask.domain.util.TaskSelector
import dev.sagi.monotask.domain.util.XpEvents
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FocusViewModel(
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
    private val userId: String = MonoTaskApp.instance.auth.currentUser?.uid ?: ""
) : ViewModel() {

    private val _uiState = MutableStateFlow<FocusUiState>(FocusUiState.Loading)
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private val _showSnoozeSheet = MutableStateFlow(false)
    val showSnoozeSheet: StateFlow<Boolean> = _showSnoozeSheet.asStateFlow()

    private val _xpBadgeVisible = MutableStateFlow(false)
    val xpBadgeVisible: StateFlow<Boolean> = _xpBadgeVisible.asStateFlow()

    private val _lastXpGained = MutableStateFlow(0)
    val lastXpGained: StateFlow<Int> = _lastXpGained.asStateFlow()

//    private var lastSnoozedId: String? = null
//    private var pendingSnoozeOption: XpEvents.SnoozeOption = XpEvents.SnoozeOption.NEXT_IN_QUEUE
    private var tasksObserved = false


    @OptIn(ExperimentalCoroutinesApi::class)
    fun startObservingTasks(selectedWorkspace: StateFlow<Workspace?>) {
        if (tasksObserved) return
        tasksObserved = true

        selectedWorkspace
            .flatMapLatest { workspace ->
                if (workspace == null) flowOf(Pair(null, emptyList()))
                else taskRepository.getActiveTasks(userId, workspace.id).map { Pair(workspace, it) }
            }
            .onEach { (workspace, tasks) ->
                if (workspace == null) { _uiState.value = FocusUiState.Empty; return@onEach }

                // Source of truth is Firestore, not local computation
                val topTask = workspace.currentFocusTaskId
                    ?.let { id -> tasks.find { it.id == id } }
                    ?: TaskSelector.getTopTask(tasks, workspace) // first launch fallback

                val queue = TaskSelector.getSortedTasks(tasks, workspace)
                _uiState.value = if (topTask == null) FocusUiState.Empty
                else FocusUiState.Active(topTask, queue.drop(1), workspace)
            }

            .launchIn(viewModelScope)
    }

    fun completeTask() {
        val state = _uiState.value as? FocusUiState.Active ?: return

        // Use the stored value. No recalculation needed
        val xpGained = state.focusTask.currentXp
        _lastXpGained.value = xpGained
        _xpBadgeVisible.value = true

        viewModelScope.launch {
            taskRepository.markTaskCompleted(userId, state.focusTask.id)
            val userDoc = userRepository.getUserOnce(userId) ?: run {
                _xpBadgeVisible.value = false
                return@launch
            }
            userRepository.addXp(userId, xpGained, userDoc.xp, userDoc.level)
            delay(1000)
            _xpBadgeVisible.value = false
            userRepository.logDailyActivity(userId, xpGained, tasksCompleted = 1)
            val completedTasks = taskRepository.getCompletedTasksOnce(userId, state.workspace.id)
            BadgeEngine.evaluate(completedTasks)
        }
    }

    fun snoozeTask(option: XpEvents.SnoozeOption) {
        val state = _uiState.value as? FocusUiState.Active ?: return

        viewModelScope.launch {
            // 1. Snooze the current task
            taskRepository.updateSnoozeFields(userId, state.focusTask, option)

            // 2. Compute next task locally
            val allTasks = taskRepository.getActiveTasksOnce(userId, state.workspace.id)
            val nextTask = when (option) {
                XpEvents.SnoozeOption.BY_DUE_DATE -> TaskSelector.getTopTaskByDueDate(
                    allTasks, state.workspace, excludeId = state.focusTask.id
                )
                else -> TaskSelector.getTopTask(
                    allTasks, state.workspace, excludeId = state.focusTask.id
                )
            }

            // 3. Write the decision to Firestore — both devices now agree
            workspaceRepository.setFocusTask(userId, state.workspace.id, nextTask?.id)
            _showSnoozeSheet.value = false
        }
    }





    fun openSnoozeSheet() { _showSnoozeSheet.value = true }
    fun dismissSnoozeSheet() { _showSnoozeSheet.value = false }
}

sealed class FocusUiState {
    object Loading : FocusUiState()
    object Empty : FocusUiState()
    data class Active(val focusTask: Task, val queue: List<Task>, val workspace: Workspace) : FocusUiState()
}
