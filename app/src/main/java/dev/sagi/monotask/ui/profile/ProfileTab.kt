package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.sagi.monotask.data.model.Badge
import dev.sagi.monotask.data.model.BadgeIds
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.util.Constants

// ====================
// Tab 1 — Profile: XP bar + badges grid
// ====================

@Composable
fun ProfileTab(
    state: ProfileUiState.Ready,
    bottomPadding: Dp
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(
            top = Constants.Theme.SCREEN_PADDING,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ProfileHeader(
                user     = state.user,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
        item {
            XpBar(
                level          = state.level,
                currentXp      = state.xpIntoLevel,
                xpForNextLevel = state.xpForNextLevel,
                modifier       = Modifier.fillMaxWidth()
            )
        }

        item {
            BadgesSection(earnedBadgeIds = state.badges.filter { it.earned }.map { it.id })
        }
    }
}

// ====================
// Header
// ====================

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
                .size(120.dp)
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
                        style      = MaterialTheme.typography.displayMedium,
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

// ====================
// Level chip
// ====================

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

// ====================
// All defined badges. Always shown, locked/greyed if not earned
// ====================

private val allBadges = listOf(
    Badge(id = BadgeIds.TOP_PERFORMER,    name = "Top Performer",    description = "Complete 10 high-importance tasks in a row"),
    Badge(id = BadgeIds.CONSISTENCY_KING, name = "Consistency King", description = "Stay active for 7 consecutive days"),
    Badge(id = BadgeIds.KNOWLEDGE_SEEKER, name = "Knowledge Seeker", description = "Complete 5 tasks tagged 'study'"),
    Badge(id = BadgeIds.FAST_FINISHER,    name = "Fast Finisher",    description = "Complete a task within 1 hour of adding it"),
)

@Composable
private fun BadgesSection(earnedBadgeIds: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text       = "Badges",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        allBadges.chunked(2).forEach { rowBadges ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowBadges.forEach { badge ->
                    BadgeCard(
                        badge    = badge,
                        earned   = badge.id in earnedBadgeIds,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Filler if odd count
                if (rowBadges.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BadgeCard(
    badge: Badge,
    earned: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (earned)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (earned)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text     = if (earned) badgeEmoji(badge.id) else "🔒",
            fontSize = 32.sp
        )
        Text(
            text       = badge.name,
            style      = MaterialTheme.typography.labelLarge,
            color      = contentColor,
            fontWeight = if (earned) FontWeight.SemiBold else FontWeight.Normal,
            textAlign  = TextAlign.Center
        )
        Text(
            text      = badge.description,
            style     = MaterialTheme.typography.bodySmall,
            color     = contentColor,
            textAlign = TextAlign.Center
        )
    }
}

private fun badgeEmoji(badgeId: String): String = when (badgeId) {
    BadgeIds.TOP_PERFORMER    -> "⚡"
    BadgeIds.CONSISTENCY_KING -> "👑"
    BadgeIds.KNOWLEDGE_SEEKER -> "📚"
    BadgeIds.FAST_FINISHER    -> "🚀"
    else                      -> "🏅"
}


@Preview(showBackground = true)
@Composable
private fun ProfileTabPreview() {
    MonoTaskTheme {
        ProfileTab(
            state = ProfileUiState.Ready(
                user           = User(id = "1", displayName = "Sagi Einav", level = 25, xp = 12450),
                level          = 25,
                levelProgress  = 0.73f,
                xpIntoLevel    = 2115,
                xpForNextLevel = 2326,
                badges         = listOf(
                    Badge(id = BadgeIds.TOP_PERFORMER,    name = "Top Performer",    description = "Complete 10 high-importance tasks in a row", earned = true),
                    Badge(id = BadgeIds.CONSISTENCY_KING, name = "Consistency King", description = "Stay active for 7 consecutive days",          earned = false),
                    Badge(id = BadgeIds.KNOWLEDGE_SEEKER, name = "Knowledge Seeker", description = "Add 20 tasks with descriptions",              earned = true),
                    Badge(id = BadgeIds.FAST_FINISHER,    name = "Fast Finisher",    description = "Complete a task within 1 hour of adding it",  earned = false)
                ),
                activityData   = emptyList()
            ),
            bottomPadding = 0.dp
        )
    }
}