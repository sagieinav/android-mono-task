package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutBack
import androidx.compose.animation.core.EaseInOutBounce
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutExpo
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseInQuad
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.EaseInQuint
import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.sagi.monotask.ui.component.core.EmptyState
import dev.sagi.monotask.ui.component.core.HeroGreeting
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun FocusScreen(
    navController: NavHostController,
    viewModel: FocusViewModel,
    sharedWorkspaceVM: WorkspaceViewModel
) {
    LaunchedEffect(Unit) { viewModel.startObservingTasks(sharedWorkspaceVM.selectedWorkspace) }

    val uiState         by viewModel.uiState.collectAsState()
    val showSnoozeSheet by viewModel.showSnoozeSheet.collectAsState()
    val xpBadgeVisible  by viewModel.xpBadgeVisible.collectAsState()
    val lastXpGained    by viewModel.lastXpGained.collectAsState()

    // ========== Snooze coordination (all in one place) ==========
    var isSnoozeExiting     by remember { mutableStateOf(false) }
    var snoozeExitTrigger   by remember { mutableStateOf<SwipeExitDirection?>(null) }
    var pendingSnoozeAction by remember { mutableStateOf<(() -> Unit)?>(null) }


    var displayedUiState by remember { mutableStateOf<FocusUiState>(uiState) }
    LaunchedEffect(uiState) {
        // Single clean gate. No race between two separate flags
        snapshotFlow { xpBadgeVisible || isSnoozeExiting }
            .first { !it }
        displayedUiState = uiState
    }

    FocusScreenContent(
        uiState           = displayedUiState,
        showSnoozeSheet   = showSnoozeSheet,
        xpBadgeVisible    = xpBadgeVisible,
        lastXpGained      = lastXpGained,
        snoozeExitTrigger = snoozeExitTrigger,
        onCompleteTask    = { viewModel.completeTask() },
        onOpenSnooze      = { viewModel.openSnoozeSheet() },
        onDismissSnooze   = { viewModel.dismissSnoozeSheet() },
        // Sheet confirmation: stage action + start exit
        onSnoozeConfirmed = { penalty ->
            pendingSnoozeAction = { viewModel.snoozeTask(penalty) }
            isSnoozeExiting   = true
            snoozeExitTrigger = SwipeExitDirection.LEFT
            viewModel.dismissSnoozeSheet()
        },
        // Called after card is off-screen: fire action, clear ALL snooze state
        onSnoozeCardExited = {
            pendingSnoozeAction?.invoke()       // calls snoozeTask → Firestore updates
            pendingSnoozeAction   = null
            snoozeExitTrigger     = null        // null BEFORE new card mounts
            isSnoozeExiting       = false       // releases the gate
        }
    )
}



@Composable
fun FocusScreenContent(
    uiState: FocusUiState,
    showSnoozeSheet: Boolean = false,
    xpBadgeVisible: Boolean = false,
    lastXpGained: Int = 0,
    snoozeExitTrigger: SwipeExitDirection? = null,
    onCompleteTask: () -> Unit = {},
    onOpenSnooze: () -> Unit = {},
    onDismissSnooze: () -> Unit = {},
    onSnoozeConfirmed: (Int) -> Unit = {},
    onSnoozeCardExited: () -> Unit = {}
) {
    val innerPadding = LocalScaffoldPadding.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(
                top    = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
        ) {
            HeroGreeting(userName = "Sagi")
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                when (uiState) {
                    is FocusUiState.Loading -> LoadingSpinner()
                    is FocusUiState.Empty   -> EmptyState()
                    is FocusUiState.Active  -> ActiveFocusCard(
                        uiState           = uiState,
                        snoozeExitTrigger = snoozeExitTrigger,
                        onCompleteTask    = onCompleteTask,
                        onOpenSnooze      = onOpenSnooze,
                        onSnoozeCardExited = onSnoozeCardExited
                    )
                }
                XpLabelCompletion(
                    xpDelta  = lastXpGained,
                    visible  = xpBadgeVisible,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (showSnoozeSheet) {
            SnoozeBottomSheet(
                onDismissRequest = onDismissSnooze,
                onSnooze = { penalty -> onSnoozeConfirmed(penalty) }
            )
        }
    }
}


// ========== Active state: currently active FocusCard ==========

private class FocusCardAnim {
    val alpha  = Animatable(0f)
    val scale  = Animatable(0.7f)
    val border = Animatable(0f)

    suspend fun reset() {
        alpha.snapTo(0f)
        scale.snapTo(0.7f)
        border.snapTo(0f)
    }
}
@Composable
private fun ActiveFocusCard(
    uiState: FocusUiState.Active,
    snoozeExitTrigger: SwipeExitDirection?,
    onSnoozeCardExited: () -> Unit,
    onCompleteTask: () -> Unit,
    onOpenSnooze: () -> Unit,
) {
    val anim = remember { FocusCardAnim() }

    LaunchedEffect(uiState.focusTask.id) {
        anim.reset()

        val spring = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessVeryLow
        )
        launch { anim.alpha.animateTo(1f, tween(400)) }
        launch { anim.scale.animateTo(1f, spring) }
        launch {
//            delay(100)
            anim.border.animateTo(1f,   tween(1600, easing = EaseInQuart)) // Border animation
            anim.border.animateTo(1.1f, tween(200)) // Tail easing
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                alpha  = anim.alpha.value
                scaleX = anim.scale.value
                scaleY = anim.scale.value
            },
        contentAlignment = Alignment.Center
    ) {
        key(uiState.focusTask.id) {
            FocusCardSwipeable(
                task               = uiState.focusTask,
                exitTrigger        = snoozeExitTrigger,
                borderFraction     = anim.border.value,
                onSwipeRight       = onCompleteTask,
                onSwipeLeft        = onOpenSnooze,
                onSnoozeCardExited = onSnoozeCardExited,
                modifier           = Modifier.fillMaxWidth(),
            )
        }
    }
}
