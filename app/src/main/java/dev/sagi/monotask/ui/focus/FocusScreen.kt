package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.ui.component.core.HeroGreeting
import dev.sagi.monotask.ui.shared.UserSessionViewModel
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.LocalSnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.StreakFire
import dev.sagi.monotask.ui.theme.fireIconGradient

// ========== Entry Point ==========

@Composable
fun FocusScreen(
    navController: NavHostController,
    focusVM: FocusViewModel,
//    userSessionVM: UserSessionViewModel
) {
    val uiState             by focusVM.uiState.collectAsStateWithLifecycle()
    val frozenForAnimation  by focusVM.frozenForAnimation.collectAsStateWithLifecycle()
    val currentStreak       by focusVM.currentStreak.collectAsStateWithLifecycle()
//    val userDisplayName     by userSessionVM.displayName.collectAsStateWithLifecycle()
    val snackbarHostState   = LocalSnackbarHostState.current

    val stableOnFocusEvent  = remember { { event: FocusEvent -> focusVM.onEvent(event) } }
    val animState           = rememberFocusAnimationState(onFocusEvent = stableOnFocusEvent)

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
                        message         = effect.message,
                        withDismissAction = true,
                        duration        = SnackbarDuration.Short
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
            }
        }
    }

    FocusScreenContent(
        uiState         = displayedUiState,
        currentStreak   = currentStreak,
        animState       = animState,
        onFocusEvent    = stableOnFocusEvent
    )
}

// ========== Content ==========

@Composable
fun FocusScreenContent(
    uiState: FocusUiState,
    currentStreak: Int,
    animState: FocusAnimationState,
    onFocusEvent: (FocusEvent) -> Unit
) {
    val innerPadding = LocalScaffoldPadding.current
    val horizontalPadding = 20.dp
    val scope = rememberCoroutineScope()

    if (uiState is FocusUiState.Loading) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
                start = horizontalPadding,
                end = horizontalPadding
            )
    ) {
        // Active Streak Chip
        StreakChip(currentStreak)

        // Card fills all space
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

@Composable
fun StreakChip(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    val streakLabel = if (currentStreak == 1) "day streak" else "days streak"

    StatChip(
        value        = currentStreak.toString(),
        label        = streakLabel,
        icon         = painterResource(R.drawable.ic_fire),
//        accentColor  = StreakFire,
        iconModifier = Modifier.fireIconGradient(),
        modifier     = modifier
    )
}

@Composable
private fun StatChip(
    value:        String,
    label:        String,
    icon:         Painter,
    accentColor:  Color    = MaterialTheme.colorScheme.onSurface,
    iconModifier: Modifier = Modifier,
    modifier:     Modifier = Modifier
) {
    // Captures the rendered value-text height so the icon can match it exactly
    var iconSizePx by remember { mutableIntStateOf(0) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.Bottom,
        modifier = modifier
    ) {
        // Icon: size matches value-text height
        // iconModifier injects optional effects (like gradient)
        Icon(
            painter            = icon,
            contentDescription = null,
//            tint               = accentColor,
//            tint               = Color(0xFFD25E03),
            modifier           = Modifier
                .size(
                    if (iconSizePx > 0) with(density) { (iconSizePx * 0.75f).toDp() } else 28.dp
                )
                .align(Alignment.CenterVertically)
                .then(iconModifier)
                .padding(end = 2.dp)
        )

        // Value / Number
        Text(
            text       = value,
            style      = MaterialTheme.typography.displaySmall,
//            color      = accentColor,
            modifier     = Modifier.alignByBaseline(),
            onTextLayout = { iconSizePx = it.size.height }
        )

        // Label / Unit
        Text(
            text  = label,
            style = MaterialTheme.typography.labelLarge,
            color    = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.alignByBaseline()
        )
    }
}

// ========== Active Card ==========
@Composable
private fun ActiveFocusCard(
    uiState: FocusUiState.Active,
    animState: FocusAnimationState,
    onFocusEvent: (FocusEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Synchronously mark dirty before the first frame draws
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
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = animState.displayAlpha
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

    private var needsReset by mutableStateOf(true)
    private var lastCardKey: Pair<String, Int>? = null

    val displayAlpha:  Float get() = if (needsReset) 0f    else alpha.value
    val displayScale:  Float get() = if (needsReset) 0.22f else scale.value
    val displayBorder: Float get() = if (needsReset) 0f    else border.value

    // Called during composition. Sets needsReset only when the card identity changes
    fun checkIfNeedsReset(taskId: String, restoreVersion: Int) {
        val key = taskId to restoreVersion
        if (key != lastCardKey) {
            lastCardKey = key
            needsReset = true
        }
    }

    // Called from LaunchedEffect. Snaps values then clears the reset flag
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
            pendingSnoozeAction = { onFocusEvent(FocusEvent.ExecuteSnooze(option)) }
            isSnoozeExiting  = true
            snoozeExitTrigger = SwipeExitDirection.LEFT
        }
    }

    fun onSnoozeCardExited() {
        pendingSnoozeAction?.invoke()
        pendingSnoozeAction  = null
        snoozeExitTrigger    = null
        isSnoozeExiting      = false
    }
}

@Composable
fun rememberFocusAnimationState(onFocusEvent: (FocusEvent) -> Unit): FocusAnimationState {
    return remember(onFocusEvent) { FocusAnimationState(onFocusEvent) }
}
