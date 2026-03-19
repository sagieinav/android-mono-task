package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.component.core.GlassTabRow
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.LocalProfileTabState
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.Constants

// Estimated total height of the floating GlassTabRow including its vertical padding.
// (4dp inner + 10dp tab vertical padding + ~20dp titleSmall + 10dp + 4dp) + 8dp*2 outer = 64dp
private val TAB_ROW_AREA_HEIGHT = 64.dp

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileVM: ProfileViewModel
) {
    val uiState       by profileVM.uiState.collectAsStateWithLifecycle()
    val searchResults by profileVM.searchResults.collectAsStateWithLifecycle()
    val isSearching   by profileVM.isSearching.collectAsStateWithLifecycle()
    val isRefreshing  by profileVM.isRefreshing.collectAsStateWithLifecycle()

    val onProfileEvent: (ProfileEvent) -> Unit = remember { { profileVM.onEvent(it) } }

    ProfileScreenContent(
        uiState        = uiState,
        searchResults  = searchResults,
        isSearching    = isSearching,
        isRefreshing   = isRefreshing,
        onProfileEvent = onProfileEvent
    )
}

// ====================
// Content
// ====================

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    searchResults: List<User> = emptyList(),
    isSearching: Boolean = false,
    isRefreshing: Boolean = false,
    onProfileEvent: (ProfileEvent) -> Unit = {}
) {
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
            ) { Text(uiState.message, color = MaterialTheme.colorScheme.error) }
        }

        is ProfileUiState.Ready -> {
            ProfileReadyContent(
                state          = uiState,
                searchResults  = searchResults,
                isSearching    = isSearching,
                isRefreshing   = isRefreshing,
                onProfileEvent = onProfileEvent
            )
        }
    }
}

// ====================
// Ready state
// ====================

@Composable
private fun ProfileReadyContent(
    state: ProfileUiState.Ready,
    searchResults: List<User>,
    isSearching: Boolean,
    isRefreshing: Boolean,
    onProfileEvent: (ProfileEvent) -> Unit
) {
    val scaffoldPadding = LocalScaffoldPadding.current
    val localHaze       = rememberHazeState()
    val topBarHeight    = scaffoldPadding.calculateTopPadding()
    val bottomPadding   = scaffoldPadding.calculateBottomPadding()
    // Content must clear both the transparent top bar and the floating tab row.
//    val contentTopPadding = topBarHeight + TAB_ROW_AREA_HEIGHT
    val contentTopPadding = topBarHeight

//    var selectedTab     by remember { mutableIntStateOf(0) }
    var selectedTab     by LocalProfileTabState.current
    val tabs            = listOf("Profile", "Statistics", "Social")

    // Outer Box fills the *full* screen (no top offset) so content scrolls
    // behind the transparent top bar.
    Box(modifier = Modifier.fillMaxSize()) {
        // ========== Scrollable content ==========
        // Starts at y=0 so it can reach behind both the top bar and the tab row.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Constants.Theme.SCREEN_PADDING)
                .background(MaterialTheme.colorScheme.background)
                .hazeSource(localHaze)
        ) {
            when (selectedTab) {
                0 -> ProfileTab(
                    state         = state,
                    topPadding    = contentTopPadding,
                    bottomPadding = bottomPadding,
                    onEvent       = onProfileEvent
                )
                1 -> StatisticsTab(
                    state         = state,
                    isRefreshing  = isRefreshing,
                    onRefresh     = { onProfileEvent(ProfileEvent.RefreshPage) },
                    topPadding    = contentTopPadding,
                    bottomPadding = bottomPadding
                )
                2 -> SocialTab(
                    friends        = state.user.friends,
                    searchResults  = searchResults,
                    isSearching    = isSearching,
                    onProfileEvent = onProfileEvent,
                    topPadding     = contentTopPadding,
                    bottomPadding  = bottomPadding
                )
            }
        }

//        // ========== Floating tab row ==========
//        // Offset by topBarHeight so it sits directly below the top bar.
//        CompositionLocalProvider(LocalHazeState provides localHaze) {
//            GlassTabRow(
//                tabs          = tabs,
//                selectedIndex = selectedTab,
//                onTabSelected = { selectedTab = it },
//                modifier      = Modifier
//                    .fillMaxWidth()
//                    .padding(
//                        start  = Constants.Theme.SCREEN_PADDING,
//                        end    = Constants.Theme.SCREEN_PADDING,
//                        top    = topBarHeight + 8.dp,
//                        bottom = 8.dp
//                    )
//            )
//        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
                ProfileScreenContent(
                    uiState = ProfileUiState.Ready(
                        user = User(id = "1", displayName = "Sagi Einav", level = 25, xp = 12450),
                        level = 25,
                        levelProgress = 0.73f,
                        xpIntoLevel = 2115,
                        xpForNextLevel = 2326,
                        badges = emptyList(),
                        activityData = emptyList()
                    )
                )
            }
        }
    }
}
