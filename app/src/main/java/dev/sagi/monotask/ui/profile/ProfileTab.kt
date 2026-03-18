package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import dev.sagi.monotask.domain.util.DiceBearHelper
import dev.sagi.monotask.ui.component.core.BottomSheet
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.util.Constants

// ====================
// Tab 1 — Profile: XP bar + badges grid
// ====================

@Composable
fun ProfileTab(
    state: ProfileUiState.Ready,
    onEvent: (ProfileEvent) -> Unit,
    bottomPadding: Dp
) {
    if (state.showAvatarPicker) {
        AvatarPicker(
            user      = state.user,
            onSelect  = { preset -> onEvent(ProfileEvent.SelectAvatar(preset)) },
            onDismiss = { onEvent(ProfileEvent.DismissAvatarPicker) }
        )
    }
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
                user          = state.user,
                modifier      = Modifier.padding(vertical = 6.dp),
                onAvatarClick = { onEvent(ProfileEvent.OpenAvatarPicker) }
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
    onAvatarClick: () -> Unit = { },   // opens avatar picker
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainer)
                .glassBorder(CircleShape)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            AvatarImage(user = user, modifier = Modifier.fillMaxSize())
        }

        Text(
            text       = user.displayName.ifEmpty { "MonoTask User" },
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ====================
// Shared avatar image — handles auto (DiceBear URL) vs preset (local drawable)
// ====================

@Composable
fun AvatarImage(user: User, modifier: Modifier = Modifier) {
    val scaledModifier = modifier.graphicsLayer {
        scaleX       = 0.95f
        scaleY       = 0.95f
        translationY = size.height * 0.05f
    }
    if (user.isAutoAvatar) {
        AsyncImage(
            model              = user.resolvedAvatarUrl(),
            contentDescription = "Avatar",
            contentScale       = ContentScale.Fit,
            modifier           = scaledModifier
        )
    } else {
        Image(
            painter            = painterResource(user.avatarPreset),
            contentDescription = "Avatar",
            contentScale       = ContentScale.Fit,
            modifier           = scaledModifier
        )
    }
}

// ====================
// Avatar Picker (bottom sheet)
// ====================

@Composable
fun AvatarPicker(
    user: User,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options: List<DiceBearHelper.AvatarPreset?> = listOf(null) + DiceBearHelper.PRESETS

    BottomSheet(
        title            = "Choose Your Avatar",
        onDismissRequest = onDismiss
    ) {
        LazyVerticalGrid(
            columns               = GridCells.Fixed(3),
            contentPadding        = PaddingValues(Constants.Theme.SCREEN_PADDING),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            modifier              = Modifier.wrapContentHeight().heightIn(max = 360.dp)
        ) {
            items(options) { preset ->
                val displayUser = if (preset == null) user.copy(avatarPreset = 0)
                                  else user.copy(avatarPreset = preset.drawable)
                val isSelected  = (preset == null && user.isAutoAvatar) ||
                                   preset?.drawable == user.avatarPreset
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainer)
                        .then(
                            if (isSelected) Modifier.border(
                                shape = CircleShape,
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            else Modifier.glassBorder(shape = CircleShape, width = 3.dp)
                        )
                        .clickable { onSelect(preset?.drawable ?: 0) }
                ) {
                    AvatarImage(user = displayUser, modifier = Modifier.fillMaxSize())
                }
            }
        }
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
            bottomPadding = 0.dp,
            onEvent = {}
        )
    }
}
