@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package dev.sagi.monotask.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.designsystem.components.MonoLoadingIndicator
import dev.sagi.monotask.designsystem.components.SegmentedToggle
import dev.sagi.monotask.designsystem.theme.LocalHazeState
import dev.sagi.monotask.designsystem.theme.LocalScaffoldPadding
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.util.Constants.Theme.TOP_BAR_ITEM_HEIGHT
import dev.sagi.monotask.ui.statistics.components.allTimeItems
import dev.sagi.monotask.ui.statistics.components.monthlyItems
import dev.sagi.monotask.ui.statistics.components.weeklyItems

private val statisticsTabs = listOf("Weekly", "Monthly", "All-Time")

@Composable
fun StatisticsScreen(
    statisticsVM: StatisticsViewModel
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    val uiState by statisticsVM.uiState.collectAsStateWithLifecycle()
    val scaffoldPadding = LocalScaffoldPadding.current

    when (uiState) {
        is StatisticsUiState.Loading -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = scaffoldPadding.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) { MonoLoadingIndicator() }
        }

        is StatisticsUiState.Ready -> {
            val readyState = uiState as StatisticsUiState.Ready
            StatisticsReadyContent(
                state = readyState,
                isRefreshing = readyState.isRefreshing,
                selectedSection = selectedSection,
                onSectionSelected = { selectedSection = it },
                onRefresh = { statisticsVM.onEvent(StatisticsEvent.Refresh) },
            )
        }
    }
}

@Composable
private fun StatisticsReadyContent(
    state: StatisticsUiState.Ready,
    isRefreshing: Boolean,
    selectedSection: Int,
    onSectionSelected: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    val scaffoldPadding = LocalScaffoldPadding.current
    val topBarHeight = scaffoldPadding.calculateTopPadding()
    val bottomPadding = scaffoldPadding.calculateBottomPadding()
    val density = LocalDensity.current
    val refreshState = rememberPullToRefreshState()
    val refreshThreshold = 30.dp

    var toggleHeightPx by remember { mutableIntStateOf(0) }
    val toggleHeight = with(density) { toggleHeightPx.toDp() }
    var timestampHeightPx by remember { mutableIntStateOf(0) }
    val timestampHeight = with(density) { timestampHeightPx.toDp() }
    // gap above toggle (between topBar bottom and toggle top)
    val toggleTopGap = 4.dp
    // gap below toggle (between toggle bottom and first content item)
    val toggleBottomGap = 12.dp
    val refreshBoxTopPadding = topBarHeight + toggleTopGap + toggleHeight
    val contentTopPadding = refreshBoxTopPadding + toggleBottomGap + timestampHeight + 8.dp

    var animationKey by remember { mutableIntStateOf(0) }
    var lastRefreshedAt by rememberSaveable { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var prevRefreshingRef by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        val wasRefreshing = prevRefreshingRef
        prevRefreshingRef = isRefreshing
        if (wasRefreshing && !isRefreshing) {
            animationKey++
            lastRefreshedAt = System.currentTimeMillis()
        }
    }

    val timestampLabel = lastRefreshedAt?.let { ts ->
        remember(ts) { "Last updated " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts)) }
    }

    val localHazeState = rememberHazeState()
    val pagerState = rememberPagerState(
        initialPage = selectedSection,
        pageCount = { statisticsTabs.size }
    )

    // Swipe → update toggle
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { onSectionSelected(it) }
    }
    // Toggle tap → animate pager
    LaunchedEffect(selectedSection) {
        if (pagerState.currentPage != selectedSection) {
            pagerState.animateScrollToPage(selectedSection)
        }
    }

    Box(Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = refreshState,
            threshold = refreshThreshold,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = refreshState,
                    isRefreshing = isRefreshing,
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    maxDistance = refreshThreshold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = refreshBoxTopPadding)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(localHazeState)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            translationY = refreshState.distanceFraction * refreshThreshold.toPx()
                        },
                    contentPadding = PaddingValues(
                        top = contentTopPadding,
                        bottom = bottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (page) {
                        0 -> weeklyItems(state, animationKey)
                        1 -> monthlyItems(state, animationKey)
                        2 -> allTimeItems(state, animationKey)
                    }
                }
            }
        }

        timestampLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = refreshBoxTopPadding + toggleBottomGap)
                    .fillMaxWidth()
                    .onSizeChanged { timestampHeightPx = it.height }
            )
        }

        CompositionLocalProvider(LocalHazeState provides localHazeState) {
            SegmentedToggle(
                options = statisticsTabs,
                selectedIndex = selectedSection,
                onOptionSelected = onSectionSelected,
                blurred = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = topBarHeight + toggleTopGap,
                        start = 16.dp,
                        end = 16.dp,
                    )
                    .height(TOP_BAR_ITEM_HEIGHT)
                    .onSizeChanged { toggleHeightPx = it.height }
            )
        }
    }
}

// ========== Preview ==========

private val previewScreenState = StatisticsUiState.Ready(
    weeklyXp = 960,
    weeklyTasks = 24,
    monthlyXp = 3840,
    totalXp = 12450,
    totalTasks = 142,
    aceCount = 89,
    longestStreak = 14,
)

@Preview(showSystemUi = true, name = "StatisticsScreen: Weekly")
@Composable
private fun StatisticsScreenWeeklyPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            StatisticsReadyContent(
                state = previewScreenState,
                isRefreshing = false,
                selectedSection = 0,
                onSectionSelected = {},
                onRefresh = {},
            )
        }
    }
}

@Preview(showSystemUi = true, name = "StatisticsScreen: Monthly")
@Composable
private fun StatisticsScreenMonthlyPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            StatisticsReadyContent(
                state = previewScreenState,
                isRefreshing = false,
                selectedSection = 1,
                onSectionSelected = {},
                onRefresh = {},
            )
        }
    }
}

@Preview(showSystemUi = true, name = "StatisticsScreen: All Time")
@Composable
private fun StatisticsScreenAllTimePreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            StatisticsReadyContent(
                state = previewScreenState,
                isRefreshing = false,
                selectedSection = 2,
                onSectionSelected = {},
                onRefresh = {},
            )
        }
    }
}
