package dev.sagi.monotask.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import dev.sagi.monotask.domain.util.ActivityStats
import dev.sagi.monotask.domain.util.AchievementEngine
import dev.sagi.monotask.domain.util.TaskSelector
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val taskRepository      : TaskRepository,
    private val userRepository      : UserRepository,
    private val workspaceRepository : WorkspaceRepository,
) : ViewModel() {

    // ========== State ==========

    private val _uiState = MutableStateFlow<FocusUiState>(FocusUiState.Loading)
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    // Gates displayedUiState in FocusScreen while a completion animation plays,
    // preventing Firestore snapshots from interrupting the card animation mid-way.
    // Cleared immediately in undoCompleteTask so restored cards appear without delay.
    private val _frozenForAnimation = MutableStateFlow(false)
    val frozenForAnimation: StateFlow<Boolean> = _frozenForAnimation.asStateFlow()

    // One-shot UI effects collected by FocusScreen (snackbars).
    // extraBufferCapacity prevents the VM coroutine from suspending on emit while
    // the undo snackbar collector is blocked in showSnackbar (up to ~10 s).
    private val _uiEffect = MutableSharedFlow<FocusUiEffect>(extraBufferCapacity = 8)
    val uiEffect: SharedFlow<FocusUiEffect> = _uiEffect.asSharedFlow()

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

    // Current streak — independent of task state, drives the streak chip
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    // Cached for undo operations
    private var lastCompletedTask       : Task?    = null
    private var lastSnoozedTask         : Task?    = null
    private var lastXpAwarded           : Int      = 0
    private var lastCompletedTaskWasAce : Boolean  = false

    // True while undoCompleteTask() is writing to Firestore.
    // Prevents observeTasks snapshots from overwriting the manually-restored local state.
    private var undoInProgress          : Boolean  = false

    // Workspace flow injected once from NavGraph via setWorkspaceSource
    private val _workspaceSource  = MutableStateFlow<StateFlow<Workspace?>?>(null)
    private var lastActiveState   : FocusUiState.Active? = null
    // Explicit snapshot taken at the moment completeTask() fires, isolated from the
    // continuous lastActiveState updates that arrive while Firestore observes the next task.
    // Ensures undoCompleteTask() always gets the pre-completion queue/workspace, not Task B's.
    private var savedStateForUndo : FocusUiState.Active? = null

    // ========== Init ==========

    init {
        viewModelScope.launch {
            userId = AuthUtils.awaitUid()
            observeTasks()
            observeStats()
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

    fun onEvent(event: FocusEvent) {
        when (event) {
            is FocusEvent.CompleteTask     -> completeTask()
            is FocusEvent.OpenSnooze       -> openSnoozeSheet()
            is FocusEvent.DismissSnooze    -> dismissSnoozeSheet()
            is FocusEvent.ExecuteSnooze    -> snoozeTask(event.option)
            is FocusEvent.UndoCompleteTask -> undoCompleteTask()
            is FocusEvent.UndoSnoozeTask   -> undoSnoozeTask()
            is FocusEvent.OpenEditSheet    -> openEditSheet()
            is FocusEvent.DismissEditSheet -> _editingTask.value = null
            is FocusEvent.UpdateTask       -> updateTask(event.task)
        }
    }

    // ========== Stats Observation ==========

    private fun observeStats() {
        // Live streak: re-emits on every Firestore snapshot for the current month
        userRepository.getActivity(userId, UserRepository.thisMonthRange)
            .onEach { activity ->
                _currentStreak.value = ActivityStats.computeCurrentStreak(activity)
            }
            .launchIn(viewModelScope)
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
        val state    = _uiState.value as? FocusUiState.Active ?: return
        val xpGained = state.focusTask.currentXp

        lastCompletedTask           = state.focusTask
        lastXpAwarded               = xpGained
        lastCompletedTaskWasAce     = state.focusTask.isAce
        savedStateForUndo           = state         // snapshot before Firestore overwrites lastActiveState
        _frozenForAnimation.value   = true

        viewModelScope.launch {
            try {
                _uiEffect.emit(FocusUiEffect.ShowUndoComplete("Task completed"))

                val user = _userSource?.value ?: run {
                    _frozenForAnimation.value = false
                    _uiEffect.emit(FocusUiEffect.ShowError("Failed to load user profile for XP update"))
                    return@launch
                }

                // Snapshot achievements BEFORE this task is counted so we can diff after
                // getAllCompletedTasksOnce covers all workspaces, achievements are cross-workspace
                val tasksBefore        = taskRepository.getAllCompletedTasksOnce(userId)
                val achievementsBefore = AchievementEngine.evaluate(tasksBefore, user.level)

                // Persist completion + XP, and clear the pinned task so observeTasks
                // selects and pins the next one when the Firestore snapshot arrives.
                taskRepository.markTaskCompleted(userId, state.focusTask.id)
                workspaceRepository.setFocusTask(userId, state.workspace.id, null)
                userRepository.addXp(userId, xpGained, user.xp, user.level)

                _frozenForAnimation.value = false

                userRepository.logDailyActivity(userId, xpGained, tasksCompleted = 1)

                // Evaluate AFTER by appending the just-completed task in memory
                // avoids a second Firestore fetch.
                val levelAfter        = XpEvents.levelForXp(user.xp + xpGained)
                if (levelAfter > user.level) {
                    _uiEffect.emit(FocusUiEffect.ShowLevelUp(previousLevel = user.level, newLevel = levelAfter))
                }
                val tasksAfter        = tasksBefore + state.focusTask
                val achievementsAfter = AchievementEngine.evaluate(tasksAfter, levelAfter)

                // Emit an unlock effect for each tier newly earned this completion
                val newlyUnlocked = achievementsAfter.filter { newProgress ->
                    val oldProgress = achievementsBefore.find { it.category == newProgress.category }
                    newProgress.earnedTier != null && newProgress.earnedTier != oldProgress?.earnedTier
                }
                newlyUnlocked.forEach { newProgress ->
                    _uiEffect.emit(
                        FocusUiEffect.ShowAchievementUnlocked(
                            name = newProgress.displayName,
                            tier = newProgress.earnedTier!!
                        )
                    )
                }

                // Update denormalized stats on User doc (non-critical, failure does not affect task completion)
                try { userRepository.updateUserStats(userId, xpGained, state.focusTask.isAce, newlyUnlocked) } catch (_: Exception) {}

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

                val allTasks         = taskRepository.getActiveTasksOnce(userId, state.workspace.id)
                val dueDateWeight = _currentUser.value?.dueDateWeight ?: 0.5f
                val nextTask = when (option) {
                    XpEvents.SnoozeOption.BY_DUE_DATE -> TaskSelector.getTopTaskByDueDate(
                        allTasks, dueDateWeight, excludeId = state.focusTask.id
                    )
                    else -> TaskSelector.getTopTask(
                        allTasks, dueDateWeight, excludeId = state.focusTask.id
                    )
                }
                workspaceRepository.setFocusTask(userId, state.workspace.id, nextTask?.id)

            } catch (e: Exception) {
                _uiEffect.emit(FocusUiEffect.ShowError("Failed to snooze task: ${e.message}"))
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
            focusTask      = taskToRestore,
            queue          = cachedState.queue,
            workspace      = cachedState.workspace,
            restoreVersion = cachedState.restoreVersion + 1
        )
        _frozenForAnimation.value = false

        undoInProgress = true
        viewModelScope.launch {
            try {
                // Re-pin Task A before restoring it: if restoreTask fires a Firestore
                // snapshot while currentFocusTaskId is still Task B, observeTasks would
                // overwrite _uiState back to Task B. undoInProgress blocks that, but
                // setting the pin first also ensures the first post-undo snapshot is clean.
                workspaceRepository.setFocusTask(userId, cachedState.workspace.id, taskToRestore.id)
                taskRepository.restoreTask(userId, taskToRestore.id)
                userRepository.removeXp(userId, lastXpAwarded)
                userRepository.removeDailyActivity(userId, lastXpAwarded)
                userRepository.undoUserStats(userId, lastCompletedTaskWasAce)
                lastCompletedTask       = null
                lastCompletedTaskWasAce = false
                savedStateForUndo       = null
            } catch (e: Exception) {
                _uiEffect.emit(FocusUiEffect.ShowError("Undo failed"))
            } finally {
                undoInProgress = false
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

    // ========== Edit Sheet Controls ==========

    private fun openEditSheet() {
        _editingTask.value = (_uiState.value as? FocusUiState.Active)?.focusTask
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.overwriteExistingTask(userId, task)
            } catch (e: Exception) {
                _uiEffect.emit(FocusUiEffect.ShowError("Failed to update task: ${e.message}"))
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
