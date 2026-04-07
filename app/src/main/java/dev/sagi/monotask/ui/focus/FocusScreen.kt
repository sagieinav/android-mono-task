package dev.sagi.monotask.ui.focus

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.common.EditTaskSheet
import java.util.Date
import dev.sagi.monotask.designsystem.components.EmptyState
import dev.sagi.monotask.designsystem.components.IllustrationSize
import dev.sagi.monotask.ui.common.SnoozeBottomSheet
import dev.sagi.monotask.designsystem.theme.LocalScaffoldPadding
import dev.sagi.monotask.designsystem.theme.LocalSnackbarHostState
import dev.sagi.monotask.designsystem.util.Constants
import dev.sagi.monotask.ui.focus.components.FocusAnimationState
import dev.sagi.monotask.ui.focus.components.FocusCardSwipeable
import dev.sagi.monotask.ui.common.UserHeader
import dev.sagi.monotask.ui.focus.components.rememberFocusAnimationState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ========== Entry Point ==========

@Composable
fun FocusScreen(
    focusVM: FocusViewModel,
) {
    val uiState by focusVM.uiState.collectAsStateWithLifecycle()
    val frozenForAnimation by focusVM.frozenForAnimation.collectAsStateWithLifecycle()
    val currentUser by focusVM.currentUser.collectAsStateWithLifecycle()
    val currentStreak = currentUser?.stats?.currentStreak ?: 0
    val editingTask by focusVM.editingTask.collectAsStateWithLifecycle()
    val snoozeSheetVisible by focusVM.snoozeSheetVisible.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    val stableOnFocusEvent = remember { { event: FocusEvent -> focusVM.onEvent(event) } }
    val animState = rememberFocusAnimationState(onFocusEvent = stableOnFocusEvent)
    var levelUpEvent by remember { mutableStateOf<FocusUiEffect.ShowLevelUp?>(null) }

    // Hold off UI updates while a completion animation is playing,
    // so Firestore snapshots don't interrupt the card animation mid-way
    var displayedUiState by remember { mutableStateOf(uiState) }
    LaunchedEffect(uiState, frozenForAnimation) {
        if (!frozenForAnimation) displayedUiState = uiState
    }

    // Undo snackbar. Uses `collect` (NOT collectLatest) so that subsequent
    // effects (level-up, achievements) can never cancel it mid-display.
    // Long duration gives a comfortable window to press Undo.
    LaunchedEffect(Unit) {
        focusVM.effect.collect { effect ->
            val (message, undoEvent) = when (effect) {
                is FocusUiEffect.ShowUndoComplete -> effect.message to FocusEvent.UndoCompleteTask
                is FocusUiEffect.ShowUndoSnooze -> effect.message to FocusEvent.UndoSnoozeTask
                else -> return@collect
            }
            val result = snackbarHostState.showSnackbar(message, "Undo", duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) {
                animState.cancelPendingEntryDirection()
                focusVM.onEvent(undoEvent)
            }
        }
    }

    // All other one-shot effects (errors, level-up, achievements).
    // collectLatest is fine here since none of these are undo snackbars.
    LaunchedEffect(Unit) {
        focusVM.effect.collectLatest { effect ->
            when (effect) {
                is FocusUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                }
                is FocusUiEffect.ShowAchievementUnlocked -> {
                    val tierLabel = when (effect.tier) {
                        AchievementTier.BRONZE -> "🥉"
                        AchievementTier.SILVER -> "🥈"
                        AchievementTier.GOLD -> "🥇"
                    }
                    snackbarHostState.showSnackbar(
                        message = "$tierLabel Achievement unlocked: ${effect.name}",
                        duration = SnackbarDuration.Short
                    )
                }
                is FocusUiEffect.ShowLevelUp -> {
                    levelUpEvent = effect
                }
                else -> { /* handled by the dedicated collectors above */ }
            }
        }
    }

    FocusScreenContent(
        uiState = displayedUiState,
        currentStreak = currentStreak,
        currentUser = currentUser,
        animState = animState,
        onFocusEvent = stableOnFocusEvent,
        snoozeSheetVisible = snoozeSheetVisible,
        levelUpEvent = levelUpEvent,
        onLevelUpDone = { levelUpEvent = null }
    )

    editingTask?.let { task ->
        EditTaskSheet(
            task = task,
            onDismiss = { focusVM.onEvent(FocusEvent.DismissEditSheet) },
            onSave = { title, desc, importance, tags, dueDate ->
                focusVM.onEvent(FocusEvent.UpdateTask(task.copy(
                    title = title,
                    description = desc,
                    importance = importance,
                    tags = tags,
                    dueDate = dueDate?.let { Timestamp(Date(it)) }
                )))
                focusVM.onEvent(FocusEvent.DismissEditSheet)
            }
        )
    }
}

// ========== Content ==========

@Composable
fun FocusScreenContent(
    uiState : FocusUiState,
    currentStreak : Int,
    currentUser : User?,
    animState : FocusAnimationState,
    onFocusEvent : (FocusEvent) -> Unit,
    snoozeSheetVisible : Boolean = false,
    levelUpEvent : FocusUiEffect.ShowLevelUp? = null,
    onLevelUpDone : () -> Unit = {}
) {
    val innerPadding = LocalScaffoldPadding.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = remember(configuration, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }

    if (uiState is FocusUiState.Loading) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
                start = Constants.Theme.SCREEN_PADDING,
                end = Constants.Theme.SCREEN_PADDING
            )
    ) {
        UserHeader(
            user = currentUser,
            currentStreak = currentStreak,
            levelUpEvent = levelUpEvent,
            onLevelUpDone = onLevelUpDone
        )

        when (uiState) {
            is FocusUiState.Empty  -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                EmptyState(
                    imgRes = IconPack.ImgEmptyFocus,
                    title = "Where are the tasks?",
                    subtitle = "Oh... you cleared 'em all. Well played!",
                    size = IllustrationSize.Large,
                    modifier = Modifier.padding(bottom = 40.dp) // optical correction for vertical position
                )
            }
            is FocusUiState.Active ->
                key(uiState.focusTask.id, uiState.restoreVersion) {
                    FocusCardSwipeable(
                        task = uiState.focusTask,
                        restoreVersion = uiState.restoreVersion,
                        animState = animState,
                        onSwipeRight = { onFocusEvent(FocusEvent.CompleteTask) },
                        onSwipeLeft  = { onFocusEvent(FocusEvent.OpenSnooze) },
                        onLongPress  = { onFocusEvent(FocusEvent.OpenEditSheet) }
                    )
                }
            is FocusUiState.Loading -> {} // unreachable: handled by early return above
        }
    }

    if (snoozeSheetVisible) {
        SnoozeBottomSheet(
            onDismissRequest = { onFocusEvent(FocusEvent.DismissSnooze) },
            onSnooze = { option -> animState.onSnoozeConfirmed(option, scope, screenWidthPx) }
        )
    }
}

