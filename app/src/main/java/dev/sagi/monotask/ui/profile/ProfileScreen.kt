package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBorder

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

    ProfileScreenContent(
        uiState       = uiState,
        searchResults = searchResults,
        isSearching   = isSearching,
        onSearchUsers = { profileVM.searchUsers(it) },
        onAddFriend   = { profileVM.addFriend(it) }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    searchResults: List<User> = emptyList(),
    isSearching: Boolean = false,
    onSearchUsers: (String) -> Unit = {},
    onAddFriend: (String) -> Unit = {}
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
                state         = uiState,
                searchResults = searchResults,
                isSearching   = isSearching,
                onSearchUsers = onSearchUsers,
                onAddFriend   = onAddFriend
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ready state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileReadyContent(
    state: ProfileUiState.Ready,
    searchResults: List<User>,
    isSearching: Boolean,
    onSearchUsers: (String) -> Unit,
    onAddFriend: (String) -> Unit
) {
    val scaffoldPadding = LocalScaffoldPadding.current
    var selectedTab     by remember { mutableIntStateOf(0) }
    val tabs            = listOf("Profile", "Statistics", "Social")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = scaffoldPadding.calculateTopPadding())
    ) {
        ProfileHeader(
            user     = state.user,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = { selectedTab = index },
                    text     = { Text(title) },
                    unselectedContentColor = MaterialTheme.colorScheme.outline,
                )
            }
        }

        val bottomNavPadding: Dp = scaffoldPadding.calculateBottomPadding()

        when (selectedTab) {
            0 -> ProfileTab(state = state, bottomPadding = bottomNavPadding)
            1 -> StatisticsTab(state = state, bottomPadding = bottomNavPadding)
            2 -> SocialTab(
                friends       = state.user.friends,
                searchResults = searchResults,
                isSearching   = isSearching,
                onSearchUsers = onSearchUsers,
                onAddFriend   = onAddFriend,
                bottomPadding = bottomNavPadding
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileHeader(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .glassBorder(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (user.profilePicUrl.isNotEmpty()) {
                AsyncImage(
                    model              = user.profilePicUrl,
                    contentDescription = "Profile picture",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = user.displayName.firstOrNull()?.uppercase() ?: "?",
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text       = user.displayName.ifEmpty { "MonoTask User" },
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Level chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LevelChip(level: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Text(
            text       = "Level $level",
            style      = MaterialTheme.typography.labelLarge,
            color      = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
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
