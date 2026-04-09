package dev.sagi.monotask.ui.brief

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.common.UserHeader
import dev.sagi.monotask.designsystem.theme.LocalScaffoldPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.util.Constants.Theme.SCREEN_PADDING

@Composable
fun BriefScreen(briefVM: BriefViewModel) {
    val uiState by briefVM.uiState.collectAsStateWithLifecycle()
    BriefContent(uiState = uiState)
}

@Composable
private fun BriefContent(uiState: BriefUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is BriefUiState.Loading -> Unit
            is BriefUiState.Ready -> ReadyContent(state = uiState)
        }
    }
}

@Composable
private fun ReadyContent(state: BriefUiState.Ready) {
    val scaffoldPadding = LocalScaffoldPadding.current
    val density = LocalDensity.current

    var briefStatusAnimated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { briefStatusAnimated = true }

    var overdueExpanded  by remember { mutableStateOf(false) }
    var dueTodayExpanded by remember { mutableStateOf(false) }

    // Fill the remaining viewport below the sections so the illustration is centered there.
    // Illustration item is at index 3. When expanded, remainingPx ≤ 0 → illustration hidden.
    val listState = rememberLazyListState()
    val illustrationHeightDp by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val illustrationItem = info.visibleItemsInfo.find { it.index == 3 }
                ?: return@derivedStateOf 0.dp
            val remainingPx = maxOf(
                info.viewportEndOffset - info.afterContentPadding - illustrationItem.offset,
                0
            )
            with(density) { remainingPx.toDp() }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SCREEN_PADDING),
        contentPadding = PaddingValues(
            top = scaffoldPadding.calculateTopPadding(),
            bottom = scaffoldPadding.calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {  // index 0
            UserHeader(
                user = state.user,
                currentStreak = state.user?.stats?.currentStreak ?: 0
            )
            Text(
                text = "all workspaces · ${state.pendingCount} active tasks",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }

        item {  // index 1
            ExpandableBriefSection(
                label = "Overdue",
                tasks = state.overdueTasks,
                workspaceNames = state.workspaceNames,
                color = MaterialTheme.colorScheme.error,
                expanded = overdueExpanded,
                onExpandChange = { overdueExpanded = it }
            )
        }

        item {  // index 2
            ExpandableBriefSection(
                label = "Due Today",
                tasks = state.dueTodayTasks,
                workspaceNames = state.workspaceNames,
                color = MaterialTheme.colorScheme.primary,
                expanded = dueTodayExpanded,
                onExpandChange = { dueTodayExpanded = it }
            )
        }

        item {  // index 3: illustration, fills remaining height
            if (illustrationHeightDp > 0.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(illustrationHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    BriefStatus(
                        status = state.briefStatus,
                        animate = !briefStatusAnimated
                    )
                }
            }
        }
    }
}



// ========== Preview ==========

private val previewReadyState = BriefUiState.Ready(
    overdueTasks = listOf(
        Task(id = "1", title = "Submit assignment", importance = Importance.HIGH, workspaceId = "study"),
        Task(id = "2", title = "Pay electricity bill", importance = Importance.MEDIUM, workspaceId = "personal"),
    ),
    dueTodayTasks = listOf(
        Task(id = "3", title = "Team standup notes", importance = Importance.LOW, workspaceId = "work"),
    ),
    pendingCount = 12,
    briefStatus = BriefStatus.OVERDUE,
    workspaceNames = mapOf("study" to "Study", "personal" to "Personal", "work" to "Work"),
    user = User(id = "u1", displayName = "Sagi")
)

@Preview(showSystemUi = true, name = "BriefScreen: Ready")
@Composable
private fun BriefScreenPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            BriefContent(uiState = previewReadyState)
        }
    }
}
