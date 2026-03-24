package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.component.core.AvatarBox
import dev.sagi.monotask.ui.component.core.AvatarPicker
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.component.display.SectionTitle
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
    val friendUsers      by profileVM.friendUsers.collectAsStateWithLifecycle()
    val friendActivities by profileVM.friendActivities.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val onProfileEvent: (ProfileEvent) -> Unit = remember { { profileVM.onEvent(it) } }

    ProfileScreenContent(
        uiState          = uiState,
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
    friendUsers      : List<User>?,
    friendActivities : Map<String, List<DailyActivity>>,
    onProfileEvent   : (ProfileEvent) -> Unit,
    onShareInvite    : () -> Unit
) {
    val scaffoldPadding = LocalScaffoldPadding.current
    val topBarHeight    = scaffoldPadding.calculateTopPadding()
    val bottomPadding   = scaffoldPadding.calculateBottomPadding()
    val listState       = rememberLazyListState()

    if (state.showAvatarPicker) {
        AvatarPicker(
            user      = state.user,
            onSelect  = { preset -> onProfileEvent(ProfileEvent.SelectAvatar(preset)) },
            onDismiss = { onProfileEvent(ProfileEvent.DismissAvatarPicker) }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Constants.Theme.SCREEN_PADDING)
        ) {
            LazyColumn(
                state               = listState,
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(
                    top    = topBarHeight,
                    bottom = bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                // Avatar + Name + XP bar
                item {
                    ProfileHeader(
                        user          = state.user,
                        modifier      = Modifier.padding(vertical = 6.dp),
                        onAvatarClick = { onProfileEvent(ProfileEvent.OpenAvatarPicker) }
                    )
                    XpBar(
                        level          = state.level,
                        currentXp      = state.xpIntoLevel,
                        xpForNextLevel = state.xpForNextLevel,
                        modifier       = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                }

                // Achievements
                item {
                    SectionTitle("Achievements")
                    AchievementSectionRow(
                        achievements = state.achievements,
                        modifier     = Modifier.padding(vertical = 16.dp)
                    )
                }

                // Friends
                item {
                    FriendsSection(
                        friendUsers      = friendUsers,
                        friendActivities = friendActivities,
                        onShareInvite    = onShareInvite,
                        lazyListState    = listState
                    )
                }
            }
        }

//        HorizontalDivider(
//            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
//            modifier = Modifier
//                .align(Alignment.TopCenter)
//                .padding(top = topBarHeight)
//                .graphicsLayer { alpha = dividerAlpha }
//        )
    }
}

// ====================
// Header
// ====================

@Composable
fun ProfileHeader(
    user: User,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = { }
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AvatarBox(
            user     = user,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable { onAvatarClick() }
        )

        Text(
            text       = user.displayName.ifEmpty { "MonoTask User" },
            style      = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.SemiBold
        )
    }
}


@Preview(showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            androidx.compose.runtime.CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
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
