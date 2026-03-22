package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.theme.LocalProfileTabState
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.Constants

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileVM: ProfileViewModel
) {
    val uiState          by profileVM.uiState.collectAsStateWithLifecycle()
    val isRefreshing     by profileVM.isRefreshing.collectAsStateWithLifecycle()
    val friendUsers      by profileVM.friendUsers.collectAsStateWithLifecycle()
    val friendActivities by profileVM.friendActivities.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val onProfileEvent: (ProfileEvent) -> Unit = remember { { profileVM.onEvent(it) } }

    ProfileScreenContent(
        uiState          = uiState,
        isRefreshing     = isRefreshing,
        friendUsers      = friendUsers,
        friendActivities = friendActivities,
        onProfileEvent   = onProfileEvent,
        onShareInvite    = { profileVM.shareInviteLink(context) }
    )
}

// ====================
// Content
// ====================

@Composable
fun ProfileScreenContent(
    uiState          : ProfileUiState,
    isRefreshing     : Boolean = false,
    friendUsers      : List<User>? = null,
    friendActivities : Map<String, List<DailyActivity>> = emptyMap(),
    onProfileEvent   : (ProfileEvent) -> Unit = {},
    onShareInvite    : () -> Unit = {}
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
                state            = uiState,
                isRefreshing     = isRefreshing,
                friendUsers      = friendUsers,
                friendActivities = friendActivities,
                onProfileEvent   = onProfileEvent,
                onShareInvite    = onShareInvite
            )
        }
    }
}

// ====================
// Ready state
// ====================

@Composable
private fun ProfileReadyContent(
    state            : ProfileUiState.Ready,
    isRefreshing     : Boolean,
    friendUsers      : List<User>?,
    friendActivities : Map<String, List<DailyActivity>>,
    onProfileEvent   : (ProfileEvent) -> Unit,
    onShareInvite    : () -> Unit
) {
    val scaffoldPadding = LocalScaffoldPadding.current
    val topBarHeight    = scaffoldPadding.calculateTopPadding()
    val bottomPadding   = scaffoldPadding.calculateBottomPadding()
    val contentTopPadding = topBarHeight

    var selectedTab     by LocalProfileTabState.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Constants.Theme.SCREEN_PADDING / 2)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Constants.Theme.SCREEN_PADDING)
//                .background(MaterialTheme.colorScheme.background)
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
                    friendUsers      = friendUsers,
                    friendActivities = friendActivities,
                    onShareInvite    = onShareInvite,
                    topPadding       = contentTopPadding,
                    bottomPadding    = bottomPadding
                )
            }
        }
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
                        achievements = emptyList(),
                        activityData = emptyList()
                    )
                )
            }
        }
    }
}
