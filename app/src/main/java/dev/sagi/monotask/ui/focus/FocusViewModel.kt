package dev.sagi.monotask.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.MonoTaskApp
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.domain.util.BadgeEngine
import dev.sagi.monotask.domain.util.PriorityCalculator
import dev.sagi.monotask.domain.util.XpEvents
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FocusViewModel(
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
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

    private var lastSnoozedId: String? = null
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
                val topTask = PriorityCalculator.getTopTask(tasks, workspace, excludeId = lastSnoozedId)
                val queue = PriorityCalculator.getSortedTasks(tasks, workspace)
                _uiState.value = if (topTask == null) FocusUiState.Empty
                else FocusUiState.Active(topTask, queue.drop(1), workspace)
            }
            .launchIn(viewModelScope)
    }

    fun completeTask() {
        val state = _uiState.value as? FocusUiState.Active ?: return

        // Calculate XP synchronously and raise the gate BEFORE any coroutine/Firestore
        val xpGained = XpEvents.calculateCompletionXp(state.focusTask)
        _lastXpGained.value = xpGained
        _xpBadgeVisible.value = true

        viewModelScope.launch {
            taskRepository.completeTask(userId, state.focusTask.id)
            val userDoc = userRepository.getUserOnce(userId) ?: run {
                _xpBadgeVisible.value = false  // safety fallback
                return@launch
            }
            userRepository.addXp(userId, xpGained, userDoc.xp, userDoc.level)
            delay(2000)
            _xpBadgeVisible.value = false
            userRepository.logDailyActivity(userId, xpGained, tasksCompleted = 1)
            val completedTasks = taskRepository.getCompletedTasksOnce(userId, state.workspace.id)
            BadgeEngine.evaluate(completedTasks)
        }
    }


    fun snoozeTask(penalty: Int) {
        val state = _uiState.value as? FocusUiState.Active ?: return
        viewModelScope.launch {
            val userDoc = userRepository.getUserOnce(userId) ?: return@launch
            userRepository.addXp(userId, penalty, userDoc.xp, userDoc.level)
            taskRepository.snoozeTask(userId, state.focusTask.id)
            lastSnoozedId = state.focusTask.id
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
