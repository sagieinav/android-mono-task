package dev.sagi.monotask.ui.brief

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import dev.sagi.monotask.util.AuthUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant
import javax.inject.Inject

@HiltViewModel
class BriefViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BriefUiState>(BriefUiState.Loading)
    val uiState: StateFlow<BriefUiState> = _uiState

    private var _userSource: StateFlow<User?>? = null
    private val _currentUser = MutableStateFlow<User?>(null)

    fun setUserSource(userFlow: StateFlow<User?>) {
        if (_userSource == null) {
            _userSource = userFlow
            viewModelScope.launch { userFlow.collect { _currentUser.value = it } }
        }
    }

    init {
        viewModelScope.launch {
            val uid = AuthUtils.awaitUid()
            combine(
                taskRepository.getAllActiveTasks(uid),
                workspaceRepository.getWorkspaces(uid),
                _currentUser
            ) { tasks, workspaces, user ->
                val tz             = TimeZone.currentSystemDefault()
                val today          = Clock.System.now().toLocalDateTime(tz).date
                val workspaceNames = workspaces.associate { it.id to it.name }

                val overdue = tasks
                    .filter { task ->
                        if (task.dueDate == null) return@filter false
                        val due = Instant.fromEpochMilliseconds(task.dueDate.toDate().time)
                            .toLocalDateTime(tz).date
                        today.daysUntil(due) < 0
                    }
                    .sortedBy { it.dueDate!!.toDate().time }
                val dueToday = tasks
                    .filter { task ->
                        if (task.dueDate == null) return@filter false
                        val due = Instant.fromEpochMilliseconds(task.dueDate.toDate().time)
                            .toLocalDateTime(tz).date
                        today.daysUntil(due) == 0
                    }
                    .sortedByDescending { it.importance.weight }

                BriefUiState.Ready(
                    overdueTasks   = overdue,
                    dueTodayTasks  = dueToday,
                    pendingCount   = tasks.size,
                    workspaceNames = workspaceNames,
                    user           = user
                )
            }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BriefUiState.Loading)
                .collect { _uiState.value = it }
        }
    }
}
