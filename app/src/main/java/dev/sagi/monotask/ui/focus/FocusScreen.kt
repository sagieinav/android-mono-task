package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.ui.component.core.AvatarBox
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.LocalSnackbarHostState
import dev.sagi.monotask.ui.theme.fireIconGradient
import dev.sagi.monotask.ui.theme.googleSans
import dev.sagi.monotask.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ========== Entry Point ==========

@Composable
fun FocusScreen(
    navController: NavHostController,
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
                EmptyState(emoji = "🦾")
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

// ========== User Header ==========

@Composable
fun UserHeader(
    user          : User?,
    currentStreak : Int,
    modifier      : Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        user?.let {
            AvatarBox(
                user = it,
                modifier = Modifier.size(58.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = user?.displayName ?: "",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier
                    .padding(start = 1.dp) // optical correction
            )
            StreakChip(currentStreak)
        }
    }
}

// ========== Streak Chip ==========

@Composable
fun StreakChip(
    currentStreak : Int,
    modifier      : Modifier = Modifier
) {
    val streakLabel = if (currentStreak == 1) "day streak" else "days streak"

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_fire),
            contentDescription = null,
            modifier           = Modifier
                .size(18.dp)
                .fireIconGradient()
                .padding(bottom = 1.5.dp) // optical correction
        )
        Spacer(Modifier.padding(horizontal = 1.dp))
        Text(
            text  = "$currentStreak ",
            fontWeight = FontWeight.Black,
            fontFamily = googleSans,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text  = streakLabel,
            fontWeight = FontWeight.Thin,
            fontFamily = googleSans,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}


// ========== Animation State ==========

@Stable
class FocusAnimationState(
    private val onFocusEvent: (FocusEvent) -> Unit
) {
    // ========== Snooze Exit State ==========

    var isSnoozeExiting by mutableStateOf(false)
        private set

    var snoozeExitTrigger by mutableStateOf<SwipeExitDirection?>(null)
        private set

    private var pendingSnoozeAction: (() -> Unit)? = null

    // ========== Entry Animation ==========

    val alpha  = Animatable(0f)
    val scale  = Animatable(0.22f)
    val border = Animatable(0f)

    private var needsReset  by mutableStateOf(true)
    private var lastCardKey : Pair<String, Int>? = null

    val displayAlpha  : Float get() = if (needsReset) 0f    else alpha.value
    val displayScale  : Float get() = if (needsReset) 0.22f else scale.value
    val displayBorder : Float get() = if (needsReset) 0f    else border.value

    fun checkIfNeedsReset(taskId: String, restoreVersion: Int) {
        val key = taskId to restoreVersion
        if (key != lastCardKey) {
            lastCardKey = key
            needsReset  = true
        }
    }

    suspend fun resetCard() {
        alpha.snapTo(0f)
        scale.snapTo(0.22f)
        border.snapTo(0f)
        needsReset = false
    }

    // ========== Snooze Actions ==========

    fun onSnoozeConfirmed(option: XpEvents.SnoozeOption, scope: CoroutineScope) {
        scope.launch {
            onFocusEvent(FocusEvent.DismissSnooze)
            delay(100)
            pendingSnoozeAction  = { onFocusEvent(FocusEvent.ExecuteSnooze(option)) }
            isSnoozeExiting      = true
            snoozeExitTrigger    = SwipeExitDirection.LEFT
        }
    }

    fun onSnoozeCardExited() {
        pendingSnoozeAction?.invoke()
        pendingSnoozeAction = null
        snoozeExitTrigger   = null
        isSnoozeExiting     = false
    }
}

@Composable
fun rememberFocusAnimationState(onFocusEvent: (FocusEvent) -> Unit): FocusAnimationState {
    return remember(onFocusEvent) { FocusAnimationState(onFocusEvent) }
}
