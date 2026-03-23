@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package dev.sagi.monotask.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.component.core.GlassTabRow
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.component.core.SegmentedToggle
import dev.sagi.monotask.ui.profile.ProfileEvent
import dev.sagi.monotask.ui.profile.ProfileUiState
import dev.sagi.monotask.ui.profile.ProfileViewModel
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.Constants

private val statisticsTabs = listOf("Weekly", "Monthly", "All Time")

@Composable
fun StatisticsScreen(
    profileVM: ProfileViewModel,
    selectedSection: Int,          // 0 = Weekly, 1 = Monthly, 2 = All Time
    onSectionSelected: (Int) -> Unit = {}
) {
    val uiState      by profileVM.uiState.collectAsStateWithLifecycle()
    val isRefreshing by profileVM.isRefreshing.collectAsStateWithLifecycle()
    val scaffoldPadding = LocalScaffoldPadding.current

    when (uiState) {
        is ProfileUiState.Loading -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = scaffoldPadding.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) { LoadingSpinner() }
        }

        is ProfileUiState.Error -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = scaffoldPadding.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) { Text((uiState as ProfileUiState.Error).message, color = MaterialTheme.colorScheme.error) }
        }

        is ProfileUiState.Ready -> {
            StatisticsReadyContent(
                state             = uiState as ProfileUiState.Ready,
                isRefreshing      = isRefreshing,
                selectedSection   = selectedSection,
                onSectionSelected = onSectionSelected,
                onRefresh         = { profileVM.onEvent(ProfileEvent.RefreshPage) },
                topPadding        = scaffoldPadding.calculateTopPadding()
            )
        }
    }
}

@Composable
private fun StatisticsReadyContent(
    state: ProfileUiState.Ready,
    isRefreshing: Boolean,
    selectedSection: Int,
    onSectionSelected: (Int) -> Unit,
    onRefresh: () -> Unit,
    topPadding: Dp,
) {
    val refreshState     = rememberPullToRefreshState()
    val refreshThreshold = 40.dp

    Column(Modifier.fillMaxSize()) {
//        SegmentedToggle(
//            options          = statisticsTabs,
//            selectedIndex = selectedSection,
//            onOptionSelected = onSectionSelected,
//            modifier      = Modifier
////                .fillMaxWidth()
//                .align(Alignment.CenterHorizontally)
//                .padding(
//                    top = topPadding,
//                    start = 16.dp,
//                    end = 16.dp,
//                    bottom = 20.dp
//                )
////                .height(Constants.Theme.TOP_BAR_ITEM_HEIGHT)
//        )
        GlassTabRow(
            tabs          = statisticsTabs,
            selectedIndex = selectedSection,
            onTabSelected = onSectionSelected,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(
                    top = topPadding,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 20.dp
                )
                .height(Constants.Theme.TOP_BAR_ITEM_HEIGHT)
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = onRefresh,
            modifier     = Modifier.weight(1f),
            state        = refreshState,
            threshold    = refreshThreshold,
            indicator    = {
                PullToRefreshDefaults.LoadingIndicator(
                    state          = refreshState,
                    isRefreshing   = isRefreshing,
                    color          = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    maxDistance    = refreshThreshold,
                    modifier       = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        translationY = refreshState.distanceFraction * refreshThreshold.toPx()
                    },
                contentPadding      = PaddingValues(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedSection) {
                    0 -> weeklyItems(state)
                    1 -> monthlyItems(state)
                    2 -> allTimeItems(state)
                }
            }
        }
    }
}

// ========== Preview ==========

private val previewScreenState = ProfileUiState.Ready(
    user           = User(id = "1", displayName = "Sagi Einav", level = 25, xp = 12450),
    level          = 25,
    levelProgress  = 0.73f,
    xpIntoLevel    = 2115,
    xpForNextLevel = 2326,
    achievements   = emptyList(),
    activityData   = listOf(
        DailyActivity(dateEpochDay = 20000L, tasksCompleted = 3, xpEarned = 120),
        DailyActivity(dateEpochDay = 20001L, tasksCompleted = 5, xpEarned = 200),
        DailyActivity(dateEpochDay = 20002L, tasksCompleted = 2, xpEarned = 80),
        DailyActivity(dateEpochDay = 20003L, tasksCompleted = 4, xpEarned = 160),
        DailyActivity(dateEpochDay = 20004L, tasksCompleted = 6, xpEarned = 240),
        DailyActivity(dateEpochDay = 20005L, tasksCompleted = 1, xpEarned = 40),
        DailyActivity(dateEpochDay = 20006L, tasksCompleted = 3, xpEarned = 120),
    )
)

@Preview(showSystemUi = true, name = "StatisticsScreen: Weekly")
@Composable
private fun StatisticsScreenWeeklyPreview() {
    MonoTaskTheme {
        androidx.compose.runtime.CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            StatisticsReadyContent(
                state             = previewScreenState,
                isRefreshing      = false,
                selectedSection   = 0,
                onSectionSelected = {},
                onRefresh         = {},
                topPadding        = 32.dp,
            )
        }
    }
}

@Preview(showSystemUi = true, name = "StatisticsScreen: Monthly")
@Composable
private fun StatisticsScreenMonthlyPreview() {
    MonoTaskTheme {
        androidx.compose.runtime.CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            StatisticsReadyContent(
                state             = previewScreenState,
                isRefreshing      = false,
                selectedSection   = 1,
                onSectionSelected = {},
                onRefresh         = {},
                topPadding        = 32.dp,
            )
        }
    }
}

@Preview(showSystemUi = true, name = "StatisticsScreen: All Time")
@Composable
private fun StatisticsScreenAllTimePreview() {
    MonoTaskTheme {
        androidx.compose.runtime.CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            StatisticsReadyContent(
                state             = previewScreenState,
                isRefreshing      = false,
                selectedSection   = 2,
                onSectionSelected = {},
                onRefresh         = {},
                topPadding        = 32.dp,
            )
        }
    }
}
