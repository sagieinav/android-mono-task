package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.component.display.EmptyState
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.LocalSnackbarHostState
import dev.sagi.monotask.util.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ========== Entry Point ==========

@Composable
fun FocusScreen(
//    navController: NavHostController,
    focusVM: FocusViewModel,
//    userSessionVM: UserSessionViewModel
) {
    val uiState            by focusVM.uiState.collectAsStateWithLifecycle()
    val frozenForAnimation by focusVM.frozenForAnimation.collectAsStateWithLifecycle()
    val currentStreak      by focusVM.currentStreak.collectAsStateWithLifecycle()
    val currentUser        by focusVM.currentUser.collectAsStateWithLifecycle()
    val snackbarHostState  = LocalSnackbarHostState.current

    val stableOnFocusEvent = remember { { event: FocusEvent -> focusVM.onEvent(event) } }
    val animState          = rememberFocusAnimationState(onFocusEvent = stableOnFocusEvent)

    // Hold off UI updates while a completion animation is playing,
    // so Firestore snapshots don't interrupt the card animation mid-way
    var displayedUiState by remember { mutableStateOf(uiState) }
    LaunchedEffect(uiState, frozenForAnimation) {
        if (!frozenForAnimation) displayedUiState = uiState
    }

    // Collect one-shot UI effects (snackbars)
    LaunchedEffect(Unit) {
        focusVM.uiEffect.collectLatest { effect ->
            when (effect) {
                is FocusUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message           = effect.message,
                        withDismissAction = true,
                        duration          = SnackbarDuration.Short
                    )
                }
                is FocusUiEffect.ShowUndoComplete -> {
                    val result = snackbarHostState.showSnackbar(
                        message     = effect.message,
                        actionLabel = "Undo",
                        duration    = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        focusVM.onEvent(FocusEvent.UndoCompleteTask)
                    }
                }
                is FocusUiEffect.ShowUndoSnooze -> {
                    val result = snackbarHostState.showSnackbar(
                        message     = effect.message,
                        actionLabel = "Undo",
                        duration    = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        focusVM.onEvent(FocusEvent.UndoSnoozeTask)
                    }
                }
                is FocusUiEffect.ShowAchievementUnlocked -> {
                    val tierLabel = when (effect.tier) {
                        AchievementTier.BRONZE -> "🥉"
                        AchievementTier.SILVER -> "🥈"
                        AchievementTier.GOLD   -> "🥇"
                    }
                    snackbarHostState.showSnackbar(
                        message  = "$tierLabel Achievement unlocked: ${effect.name}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    FocusScreenContent(
        uiState       = displayedUiState,
        currentStreak = currentStreak,
        currentUser   = currentUser,
        animState     = animState,
        onFocusEvent  = stableOnFocusEvent
    )
}

// ========== Content ==========

@Composable
fun FocusScreenContent(
    uiState       : FocusUiState,
    currentStreak : Int,
    currentUser   : User?,
    animState     : FocusAnimationState,
    onFocusEvent  : (FocusEvent) -> Unit
) {
    val innerPadding = LocalScaffoldPadding.current
    val scope        = rememberCoroutineScope()

    if (uiState is FocusUiState.Loading) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top    = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
                start  = Constants.Theme.SCREEN_PADDING,
                end    = Constants.Theme.SCREEN_PADDING
            )
    ) {
        UserHeader(user = currentUser, currentStreak = currentStreak)

        when (uiState) {
            is FocusUiState.Empty  -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                EmptyState(
                    imgRes = R.drawable.img_empty_focus_no_bg2,
                    title = "You're all caught up!",
                    subtitle = "No tasks here. Enjoy the moment.",
                    isMainContent = true,
                    modifier = Modifier
                        .padding(bottom = 40.dp) // optical correction for vertical position
                )
            }
            is FocusUiState.Active ->
                ActiveFocusCard(
                    uiState      = uiState,
                    animState    = animState,
                    onFocusEvent = onFocusEvent
                )
            else -> {}
        }
    }

    if (uiState is FocusUiState.Active && uiState.showSnoozeSheet) {
        SnoozeBottomSheet(
            onDismissRequest = { onFocusEvent(FocusEvent.DismissSnooze) },
            onSnooze         = { option -> animState.onSnoozeConfirmed(option, scope) }
        )
    }
}

// ========== Active Card ==========

@Composable
private fun ActiveFocusCard(
    uiState      : FocusUiState.Active,
    animState    : FocusAnimationState,
    onFocusEvent : (FocusEvent) -> Unit,
    modifier     : Modifier = Modifier
) {
    SideEffect {
        animState.checkIfNeedsReset(uiState.focusTask.id, uiState.restoreVersion)
    }

    LaunchedEffect(uiState.focusTask.id, uiState.restoreVersion) {
        animState.resetCard()
        val entrySpec = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        )
        launch { animState.alpha.animateTo(1f, tween(300)) }
        launch { animState.scale.animateTo(1f, entrySpec) }
        launch {
            animState.border.animateTo(1f,   tween(1600, easing = EaseInQuart))
            animState.border.animateTo(1.1f, tween(200))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha  = animState.displayAlpha
                scaleX = animState.displayScale
                scaleY = animState.displayScale
            },
        contentAlignment = Alignment.Center
    ) {
        key(uiState.focusTask.id, uiState.restoreVersion) {
            FocusCardSwipeable(
                task               = uiState.focusTask,
                exitTrigger        = animState.snoozeExitTrigger,
                borderFraction     = animState.displayBorder,
                onSwipeRight       = { onFocusEvent(FocusEvent.CompleteTask) },
                onSwipeLeft        = { onFocusEvent(FocusEvent.OpenSnooze) },
                onSnoozeCardExited = { animState.onSnoozeCardExited() },
                modifier           = Modifier.fillMaxWidth()
            )
        }
    }
}
