package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.sagi.monotask.ui.component.core.EmptyState
import dev.sagi.monotask.ui.component.core.HeroGreeting
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.shared.UserSessionViewModel
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun FocusScreen(
    navController: NavHostController,
    focusVM: FocusViewModel,
    workspaceVM: WorkspaceViewModel,
    userSessionVM: UserSessionViewModel
) {
    LaunchedEffect(Unit) { focusVM.startObservingTasks(workspaceVM.selectedWorkspace) }

    val uiState         by focusVM.uiState.collectAsState()
    val showSnoozeSheet by focusVM.showSnoozeSheet.collectAsState()
    val xpBadgeVisible  by focusVM.xpBadgeVisible.collectAsState()

    var isSnoozeExiting     by remember { mutableStateOf(false) }
    var snoozeExitTrigger   by remember { mutableStateOf<SwipeExitDirection?>(null) }
    var pendingSnoozeAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var displayedUiState by remember { mutableStateOf<FocusUiState>(uiState) }
    LaunchedEffect(uiState) {
        snapshotFlow { xpBadgeVisible || isSnoozeExiting }
            .first { !it }
        displayedUiState = uiState
    }

    val userDisplayName by userSessionVM.displayName.collectAsStateWithLifecycle()

    FocusScreenContent(
        uiState           = displayedUiState,
        userDisplayName   = userDisplayName,
        showSnoozeSheet   = showSnoozeSheet,
        snoozeExitTrigger = snoozeExitTrigger,
        onCompleteTask    = { focusVM.completeTask() },
        onOpenSnooze      = { focusVM.openSnoozeSheet() },
        onDismissSnooze   = { focusVM.dismissSnoozeSheet() },
        onSnoozeConfirmed = { penalty ->
            pendingSnoozeAction = { focusVM.snoozeTask(penalty) }
            isSnoozeExiting     = true
            snoozeExitTrigger   = SwipeExitDirection.LEFT
            focusVM.dismissSnoozeSheet()
        },
        onSnoozeCardExited = {
            pendingSnoozeAction?.invoke()
            pendingSnoozeAction = null
            snoozeExitTrigger   = null
            isSnoozeExiting     = false
        }
    )
}

@Composable
fun FocusScreenContent(
    uiState: FocusUiState,
    userDisplayName: String,
    showSnoozeSheet: Boolean = false,
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
            HeroGreeting(userName = userDisplayName)
            Box(
                modifier           = Modifier.fillMaxWidth().weight(1f),
                contentAlignment   = Alignment.Center
            ) {
                when (uiState) {
                    is FocusUiState.Loading -> LoadingSpinner()
                    is FocusUiState.Empty   -> EmptyState()
                    is FocusUiState.Active  -> ActiveFocusCard(
                        uiState            = uiState,
                        snoozeExitTrigger  = snoozeExitTrigger,
                        onCompleteTask     = onCompleteTask,
                        onOpenSnooze       = onOpenSnooze,
                        onSnoozeCardExited = onSnoozeCardExited
                    )
                }
            }
        }

        if (showSnoozeSheet) {
            SnoozeBottomSheet(
                onDismissRequest = onDismissSnooze,
                onSnooze         = { penalty -> onSnoozeConfirmed(penalty) }
            )
        }
    }
}

// ========== Active state ==========

private class FocusCardAnim {
    val alpha  = Animatable(0f)
    val scale  = Animatable(0.22f)
    val border = Animatable(0f)

    suspend fun reset() {
        alpha.snapTo(0f)
        scale.snapTo(0.22f)
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

        val entrySpec = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        )
        launch { anim.alpha.animateTo(1f, tween(300)) }
        launch { anim.scale.animateTo(1f, entrySpec) }
        launch {
            anim.border.animateTo(1f,   tween(1600, easing = EaseInQuart))
            anim.border.animateTo(1.1f, tween(200))
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
                modifier           = Modifier.fillMaxWidth()
            )
        }
    }
}
