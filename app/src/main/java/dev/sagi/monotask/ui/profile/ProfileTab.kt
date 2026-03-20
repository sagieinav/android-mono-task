package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.AchievementCategory
import dev.sagi.monotask.data.model.AchievementMilestone
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.util.DiceBearHelper
import dev.sagi.monotask.ui.component.core.AvatarBox
import dev.sagi.monotask.ui.component.core.AvatarImage
import dev.sagi.monotask.ui.component.core.AvatarPicker
import dev.sagi.monotask.ui.component.core.BottomSheet
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.util.Constants

// ====================
// Tab 1 — Profile: XP bar + achievements grid
// ====================

@Composable
fun ProfileTab(
    state: ProfileUiState.Ready,
    onEvent: (ProfileEvent) -> Unit,
    topPadding: Dp,
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
            top    = topPadding,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(36.dp)
    ) {
        // Avatar + Name + Level & XP
        item {
            ProfileHeader(
                user          = state.user,
                modifier      = Modifier.padding(vertical = 6.dp),
                onAvatarClick = { onEvent(ProfileEvent.OpenAvatarPicker) }
            )
            XpBar(
                level          = state.level,
                currentXp      = state.xpIntoLevel,
                xpForNextLevel = state.xpForNextLevel,
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
//                    .padding(bottom = 24.dp) // more space between this and "Achievements"
            )
        }
//        item {
//
//        }
        item {
            AchievementsSection(achievements = state.achievements)
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
        AvatarBox(
            user     = user,
            modifier = Modifier
                .size(128.dp)
                .clickable { onAvatarClick() }
        )

        Text(
            text       = user.displayName.ifEmpty { "MonoTask User" },
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}


// ====================
// Preview
// ====================

@Preview(showBackground = true)
@Composable
private fun ProfileTabPreview() {
    fun tierDef(tier: AchievementTier, name: String, desc: String) = AchievementMilestone(tier, name, desc)

    MonoTaskTheme {
        ProfileTab(
            state = ProfileUiState.Ready(
                user           = User(id = "1", displayName = "Sagi Einav", level = 14, xp = 12450),
                level          = 14,
                levelProgress  = 0.73f,
                xpIntoLevel    = 572,
                xpForNextLevel = 1443,
                achievements   = listOf(
                    Achievement(
                        category   = AchievementCategory.STREAKS,
//                        emoji      = "🔥",
                        iconRes = R.drawable.ic_fire,
                        earnedTier = AchievementTier.SILVER,
                        bronze     = tierDef(AchievementTier.BRONZE, "First Flame",      "Active 3 days in a row"),
                        silver     = tierDef(AchievementTier.SILVER, "Consistency King", "Active 7 days in a row"),
                        gold       = tierDef(AchievementTier.GOLD,   "Unstoppable",      "Active 30 days in a row")
                    ),
                    Achievement(
                        category   = AchievementCategory.TASK_VOLUME,
//                        emoji      = "✅",
                        iconRes = R.drawable.ic_task_alt,
                        earnedTier = AchievementTier.GOLD,
                        bronze     = tierDef(AchievementTier.BRONZE, "Warming Up",   "Complete 5 tasks"),
                        silver     = tierDef(AchievementTier.SILVER, "Century",      "Complete 100 tasks"),
                        gold       = tierDef(AchievementTier.GOLD,   "Task Machine", "Complete 500 tasks")
                    ),
                    Achievement(
                        category   = AchievementCategory.DISCIPLINE,
//                        emoji      = "🧠",
                        iconRes = R.drawable.ic_bolt,
                        earnedTier = AchievementTier.BRONZE,
                        bronze     = tierDef(AchievementTier.BRONZE, "No Excuses",    "50%+ ace ratio (20+ tasks)"),
                        silver     = tierDef(AchievementTier.SILVER, "Iron Will",     "70%+ ace ratio (20+ tasks)"),
                        gold       = tierDef(AchievementTier.GOLD,   "Denial Denier", "90%+ ace ratio (20+ tasks)")
                    ),
                    Achievement(
                        category   = AchievementCategory.XP_LEVELING,
//                        emoji      = "🏅",
                        iconRes = R.drawable.ic_star_shine,
                        earnedTier = null,
                        bronze     = tierDef(AchievementTier.BRONZE, "Rising Star", "Reach level 5"),
                        silver     = tierDef(AchievementTier.SILVER, "Veteran",     "Reach level 15"),
                        gold       = tierDef(AchievementTier.GOLD,   "Legend",      "Reach level 30")
                    ),
                ),
                activityData = emptyList()
            ),
            topPadding    = 0.dp,
            bottomPadding = 0.dp,
            onEvent       = {}
        )
    }
}
