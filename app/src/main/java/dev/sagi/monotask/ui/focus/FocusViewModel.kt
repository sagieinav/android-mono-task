package dev.sagi.monotask.ui.focus

import androidx.lifecycle.viewModelScope
import dev.sagi.monotask.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import dev.sagi.monotask.domain.service.TaskSelector
import dev.sagi.monotask.domain.service.XpEngine
import dev.sagi.monotask.domain.usecase.CompleteTaskUseCase
import dev.sagi.monotask.domain.usecase.SnoozeTaskUseCase
import dev.sagi.monotask.domain.usecase.UndoCompleteTaskUseCase
import dev.sagi.monotask.domain.usecase.UndoSnoozeTaskUseCase
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val taskRepository : TaskRepository,
    private val workspaceRepository : WorkspaceRepository,
    private val completeTaskUseCase : CompleteTaskUseCase,
    private val snoozeTaskUseCase : SnoozeTaskUseCase,
    private val undoCompleteTaskUseCase : UndoCompleteTaskUseCase,
    private val undoSnoozeTaskUseCase : UndoSnoozeTaskUseCase,
) : BaseViewModel<FocusUiState, FocusEvent, FocusUiEffect>() {

    override val initialState: FocusUiState = FocusUiState.Loading

    // ========== Extra State ==========

    // Gates displayedUiState in FocusScreen while a completion animation plays,
    // preventing Firestore snapshots from interrupting the card animation mid-way.
    // Cleared immediately in undoCompleteTask so restored cards appear without delay.
    private val _frozenForAnimation = MutableStateFlow(false)
    val frozenForAnimation: StateFlow<Boolean> = _frozenForAnimation.asStateFlow()

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    private val _snoozeSheetVisible = MutableStateFlow(false)
    val snoozeSheetVisible: StateFlow<Boolean> = _snoozeSheetVisible.asStateFlow()

    // ========== Internal State ==========
    // All mutable vars below are only accessed on Main (viewModelScope default)

    private var userId: String = ""

    // User flow injected once from NavGraph via setUserSource (avoids redundant getUserOnce calls)
    private var _userSource: StateFlow<User?>? = null

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Cached for undo operations
    private var lastCompletedTask : Task?    = null
    private var lastSnoozedTask : Task?    = null
    private var lastXpAwarded : Int      = 0
    private var lastCompletedTaskWasAce : Boolean  = false

    // True while undoCompleteTask() is writing to Firestore.
    // Prevents observeTasks snapshots from overwriting the manually-restored local state.
    private var undoInProgress : Boolean  = false

    // Workspace flow injected once from NavGraph via setWorkspaceSource
    private val _workspaceSource = MutableStateFlow<StateFlow<Workspace?>?>(null)
    private var lastActiveState : FocusUiState.Active? = null
    // Explicit snapshot taken at the moment completeTask() fires, isolated from the
    // continuous lastActiveState updates that arrive while Firestore observes the next task.
    // Ensures undoCompleteTask() always gets the pre-completion queue/workspace, not Task B's.
    private var savedStateForUndo : FocusUiState.Active? = null

    // ========== Init ==========

    init {
        viewModelScope.launch {
            userId = AuthUtils.awaitUid()
            observeTasks()
        }
    }

    // ========== External Wiring (call once from NavGraph) ==========

    fun setWorkspaceSource(workspaceFlow: StateFlow<Workspace?>) {
        _workspaceSource.compareAndSet(null, workspaceFlow)
    }

    fun setUserSource(userFlow: StateFlow<User?>) {
        if (_userSource == null) {
            _userSource = userFlow
            viewModelScope.launch { userFlow.collect { _currentUser.value = it } }
        }
    }

    // ========== Event Handler ==========

    override fun onEvent(event: FocusEvent) {
        when (event) {
            is FocusEvent.CompleteTask -> completeTask()
            is FocusEvent.OpenSnooze -> openSnoozeSheet()
            is FocusEvent.DismissSnooze -> dismissSnoozeSheet()
            is FocusEvent.ExecuteSnooze -> snoozeTask(event.option)
            is FocusEvent.UndoCompleteTask -> undoCompleteTask()
            is FocusEvent.UndoSnoozeTask -> undoSnoozeTask()
            is FocusEvent.OpenEditSheet -> openEditSheet()
            is FocusEvent.DismissEditSheet -> _editingTask.value = null
            is FocusEvent.UpdateTask -> updateTask(event.task)
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
                // Undo is mid-flight: ignore Firestore snapshots so they don't
                // overwrite the locally-restored task state before our writes settle.
                if (undoInProgress) return@onEach

                if (workspace == null) {
                    _uiState.value = FocusUiState.Empty
                    return@onEach
                }

                val dueDateWeight = _currentUser.value?.dueDateWeight ?: 0.5f

                val pinnedTask = workspace.currentFocusTaskId
                    ?.let { id -> tasks.find { it.id == id } }
                val topTask = pinnedTask ?: TaskSelector.getTopTask(tasks, dueDateWeight)

                // Pin the selected task so priority edits don't swap the card mid-session
                // Only write when nothing was pinned yet. snooze/complete manage updates themselves
                if (pinnedTask == null && topTask != null) {
                    viewModelScope.launch {
                        workspaceRepository.setFocusTask(userId, workspace.id, topTask.id)
                    }
                }

                val queue    = TaskSelector.getSortedTasks(tasks, dueDateWeight)
                    .filter { it.id != topTask?.id }
                val newState = if (topTask == null) FocusUiState.Empty
                else FocusUiState.Active(topTask, queue, workspace)

                if (newState is FocusUiState.Active) lastActiveState = newState
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }

    // ========== Task Actions ==========

    private fun completeTask() {
        val state = _uiState.value as? FocusUiState.Active ?: return

        lastCompletedTask = state.focusTask
        lastCompletedTaskWasAce = state.focusTask.isAce
        savedStateForUndo = state // snapshot before Firestore overwrites lastActiveState
        _frozenForAnimation.value = true

        viewModelScope.launch {
            try {
                sendEffect(FocusUiEffect.ShowUndoComplete("Task completed"))

                val user = _userSource?.value ?: run {
                    _frozenForAnimation.value = false
                    sendEffect(FocusUiEffect.ShowError("Failed to load user profile for XP update"))
                    return@launch
                }

                val result = completeTaskUseCase(userId, state.focusTask, state.workspace.id, user)
                lastXpAwarded = result.xpAwarded

                _frozenForAnimation.value = false

                if (result.newLevel > result.previousLevel) {
                    sendEffect(FocusUiEffect.ShowLevelUp(previousLevel = result.previousLevel, newLevel = result.newLevel))
                }
                result.newlyUnlocked.forEach { achievement ->
                    sendEffect(FocusUiEffect.ShowAchievementUnlocked(name = achievement.displayName, tier = achievement.earnedTier!!))
                }
            } catch (e: Exception) {
                _frozenForAnimation.value = false
                sendEffect(FocusUiEffect.ShowError("Failed to complete task: ${e.message}"))
            }
        }
    }

    private fun snoozeTask(option: XpEngine.SnoozeOption) {
        val state = _uiState.value as? FocusUiState.Active ?: return
        lastSnoozedTask = state.focusTask
        _frozenForAnimation.value = true

        viewModelScope.launch {
            try {
                sendEffect(FocusUiEffect.ShowUndoSnooze("Task snoozed"))

                // Ensure the exit animation (starts at ~80ms, runs for 280ms = done at ~360ms)
                // finishes before the new card appears. Firestore may respond faster than that.
                val minVisualTime = launch { delay(380L) }

                val dueDateWeight = _currentUser.value?.dueDateWeight ?: 0.5f
                snoozeTaskUseCase(userId, state.focusTask, state.workspace.id, option, dueDateWeight)

                minVisualTime.join()

            } catch (e: Exception) {
                sendEffect(FocusUiEffect.ShowError("Failed to snooze task: ${e.message}"))
            } finally {
                _frozenForAnimation.value = false
            }
        }
    }

    private fun undoCompleteTask() {
        val taskToRestore = lastCompletedTask ?: return
        val cachedState   = savedStateForUndo ?: return  // use pre-completion snapshot, not stale lastActiveState

        // Set local state BEFORE unfreezing: the LaunchedEffect in FocusScreen reads
        // uiState and frozenForAnimation together, so the restored card must be ready
        // before the gate opens — otherwise the brief Firestore state (Task B) flashes.
        _uiState.value = FocusUiState.Active(
            focusTask = taskToRestore,
            queue = cachedState.queue,
            workspace = cachedState.workspace,
            restoreVersion = cachedState.restoreVersion + 1
        )
        _frozenForAnimation.value = false

        undoInProgress = true
        viewModelScope.launch {
            try {
                undoCompleteTaskUseCase(
                    userId      = userId,
                    taskId      = taskToRestore.id,
                    workspaceId = cachedState.workspace.id,
                    xpToRemove  = lastXpAwarded,
                    wasAce      = lastCompletedTaskWasAce
                )
                lastCompletedTask       = null
                lastCompletedTaskWasAce = false
                savedStateForUndo       = null
            } catch (e: Exception) {
                sendEffect(FocusUiEffect.ShowError("Undo failed"))
            } finally {
                undoInProgress = false
            }
        }
    }

    private fun undoSnoozeTask() {
        val taskToRestore = lastSnoozedTask ?: return

        viewModelScope.launch {
            try {
                undoSnoozeTaskUseCase(userId, taskToRestore)
                lastSnoozedTask = null
            } catch (e: Exception) {
                sendEffect(FocusUiEffect.ShowError("Failed to undo snooze: ${e.message}"))
            }
        }
    }

    // ========== Edit Sheet Controls ==========

    private fun openEditSheet() {
        _editingTask.value = (_uiState.value as? FocusUiState.Active)?.focusTask
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.overwriteExistingTask(userId, task)
            } catch (e: Exception) {
                sendEffect(FocusUiEffect.ShowError("Failed to update task: ${e.message}"))
            }
        }
    }

    // ========== Snooze Sheet Controls ==========

    private fun openSnoozeSheet() {
        val state = _uiState.value as? FocusUiState.Active ?: return
        if (state.queue.isEmpty()) {
            viewModelScope.launch {
                sendEffect(FocusUiEffect.ShowError(
                    "This is your only active task.\nTherefore, it cannot be snoozed"
                ))
            }
        } else {
            _snoozeSheetVisible.value = true
        }
    }

    private fun dismissSnoozeSheet() {
        _snoozeSheetVisible.value = false
    }

    // ========== Lifecycle ==========

    override fun onCleared() {
        super.onCleared()
        // viewModelScope is canceled here, which stops all active Firestore streams
        // (task observation, streak observation) launched via launchIn(viewModelScope)
    }
}
