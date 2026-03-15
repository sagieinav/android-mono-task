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
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FocusViewModel(
    private val taskRepository: TaskRepository = MonoTaskApp.instance.taskRepository,
    private val userRepository: UserRepository = MonoTaskApp.instance.userRepository,
    private val workspaceRepository: WorkspaceRepository = MonoTaskApp.instance.workspaceRepository,
) : ViewModel() {

    // ========== State ==========

    private val _uiState = MutableStateFlow<FocusUiState>(FocusUiState.Loading)
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    // Gates displayedUiState in FocusScreen while a completion animation plays,
    // preventing Firestore snapshots from interrupting the card animation mid-way.
    // Cleared immediately in undoCompleteTask so restored cards appear without delay.
    private val _frozenForAnimation = MutableStateFlow(false)
    val frozenForAnimation: StateFlow<Boolean> = _frozenForAnimation.asStateFlow()

    // One-shot UI effects collected by FocusScreen (snackbars)
    private val _uiEffect = MutableSharedFlow<FocusUiEffect>()
    val uiEffect: SharedFlow<FocusUiEffect> = _uiEffect.asSharedFlow()

    // ========== Internal State ==========
    // All mutable vars below are only accessed on Main (viewModelScope default)

    private lateinit var userId: String

    // Workspace flow injected once from NavGraph via setWorkspaceSource
    private val _workspaceSource = MutableStateFlow<StateFlow<Workspace?>?>(null)

    // Cached for undo operations
    private var lastCompletedTask: Task? = null
    private var lastSnoozedTask: Task? = null
    private var lastXpAwarded: Int = 0
    private var lastActiveState: FocusUiState.Active? = null

    // ========== Init ==========

    init {
        viewModelScope.launch {
            userId = AuthUtils.awaitUid()
            observeTasks()
        }
    }

    // ========== Workspace Wiring ==========

    // Connects this ViewModel to the shared workspace selection
    // Call once right after creation (in NavGraph)
    fun setWorkspaceSource(workspaceFlow: StateFlow<Workspace?>) {
        _workspaceSource.compareAndSet(null, workspaceFlow)
    }

    // ========== Event Handler ==========

    fun onEvent(event: FocusEvent) {
        when (event) {
            is FocusEvent.CompleteTask    -> completeTask()
            is FocusEvent.OpenSnooze      -> openSnoozeSheet()
            is FocusEvent.DismissSnooze   -> dismissSnoozeSheet()
            is FocusEvent.ExecuteSnooze   -> snoozeTask(event.option)
            is FocusEvent.UndoCompleteTask -> undoCompleteTask()
            is FocusEvent.UndoSnoozeTask  -> undoSnoozeTask()
        }
    }

    // ========== Task Observation ==========

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTasks() {
        _workspaceSource
            .filterNotNull()
            .flatMapLatest { workspaceFlow ->
                workspaceFlow.flatMapLatest { workspace ->
                    if (workspace == null) flowOf(Pair(null, emptyList()))
                    else taskRepository.getActiveTasks(userId, workspace.id).map { Pair(workspace, it) }
                }
            }
            .onEach { (workspace, tasks) ->
                if (workspace == null) {
                    _uiState.value = FocusUiState.Empty
                    return@onEach
                }

                val topTask = workspace.currentFocusTaskId
                    ?.let { id -> tasks.find { it.id == id } }
                    ?: TaskSelector.getTopTask(tasks, workspace)

                val queue    = TaskSelector.getSortedTasks(tasks, workspace)
                val newState = if (topTask == null) FocusUiState.Empty
                else FocusUiState.Active(topTask, queue.drop(1), workspace)

                if (newState is FocusUiState.Active) lastActiveState = newState
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }

    // ========== Task Actions ==========

    private fun completeTask() {
        val state    = _uiState.value as? FocusUiState.Active ?: return
        val xpGained = state.focusTask.currentXp

        lastCompletedTask    = state.focusTask
        lastXpAwarded        = xpGained
        _frozenForAnimation.value = true

        viewModelScope.launch {
            try {
                _uiEffect.emit(FocusUiEffect.ShowUndoComplete("Task completed"))

                taskRepository.markTaskCompleted(userId, state.focusTask.id)
                val userDoc = userRepository.getUserOnce(userId) ?: run {
                    _frozenForAnimation.value = false
                    _uiEffect.emit(FocusUiEffect.ShowError("Failed to load user profile for XP update"))
                    return@launch
                }

                userRepository.addXp(userId, xpGained, userDoc.xp, userDoc.level)

                delay(300)
                _frozenForAnimation.value = false

                userRepository.logDailyActivity(userId, xpGained, tasksCompleted = 1)
                val completedTasks = taskRepository.getCompletedTasksOnce(userId, state.workspace.id)
                // TODO: Persist badge evaluation results once badges are wired to Firestore
                BadgeEngine.evaluate(completedTasks)

            } catch (e: Exception) {
                _frozenForAnimation.value = false
                _uiEffect.emit(FocusUiEffect.ShowError("Failed to complete task: ${e.message}"))
            }
        }
    }

    private fun snoozeTask(option: XpEvents.SnoozeOption) {
        val state = _uiState.value as? FocusUiState.Active ?: return
        lastSnoozedTask = state.focusTask

        viewModelScope.launch {
            try {
                _uiEffect.emit(FocusUiEffect.ShowUndoSnooze("Task snoozed"))

                taskRepository.updateSnoozeFields(userId, state.focusTask, option)

                val allTasks = taskRepository.getActiveTasksOnce(userId, state.workspace.id)
                val nextTask = when (option) {
                    XpEvents.SnoozeOption.BY_DUE_DATE -> TaskSelector.getTopTaskByDueDate(
                        allTasks, state.workspace, excludeId = state.focusTask.id
                    )
                    else -> TaskSelector.getTopTask(
                        allTasks, state.workspace, excludeId = state.focusTask.id
                    )
                }
                workspaceRepository.setFocusTask(userId, state.workspace.id, nextTask?.id)

            } catch (e: Exception) {
                _uiEffect.emit(FocusUiEffect.ShowError("Failed to snooze task: ${e.message}"))
            }
        }
    }

    private fun undoCompleteTask() {
        // Open the animation gate first so the restored card appears immediately
        _frozenForAnimation.value = false

        val taskToRestore = lastCompletedTask ?: return
        val cachedState   = lastActiveState   ?: return

        _uiState.value = FocusUiState.Active(
            focusTask      = taskToRestore,
            queue          = cachedState.queue,
            workspace      = cachedState.workspace,
            restoreVersion = cachedState.restoreVersion + 1
        )

        viewModelScope.launch {
            try {
                taskRepository.restoreTask(userId, taskToRestore.id)
                userRepository.removeXp(userId, lastXpAwarded)
                userRepository.removeDailyActivity(userId, lastXpAwarded)
                lastCompletedTask = null
            } catch (e: Exception) {
                _uiEffect.emit(FocusUiEffect.ShowError("Undo failed"))
            }
        }
    }

    private fun undoSnoozeTask() {
        val taskToRestore = lastSnoozedTask ?: return

        viewModelScope.launch {
            try {
                taskRepository.undoSnoozeFields(userId, taskToRestore)
                workspaceRepository.setFocusTask(userId, taskToRestore.workspaceId, taskToRestore.id)
                lastSnoozedTask = null
            } catch (e: Exception) {
                _uiEffect.emit(FocusUiEffect.ShowError("Failed to undo snooze: ${e.message}"))
            }
        }
    }

    // ========== Snooze Sheet Controls ==========

    private fun openSnoozeSheet() {
        val state = _uiState.value as? FocusUiState.Active ?: return
        if (state.queue.isEmpty()) {
            viewModelScope.launch {
                _uiEffect.emit(FocusUiEffect.ShowError(
                    "This is your only active task.\nTherefore, it cannot be snoozed"
                ))
            }
        } else {
            _uiState.value = state.copy(showSnoozeSheet = true)
        }
    }

    private fun dismissSnoozeSheet() {
        val state = _uiState.value as? FocusUiState.Active ?: return
        _uiState.value = state.copy(showSnoozeSheet = false)
    }
}
