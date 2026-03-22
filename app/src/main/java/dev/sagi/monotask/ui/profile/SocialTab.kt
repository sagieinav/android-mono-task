package dev.sagi.monotask.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.UserStats
import dev.sagi.monotask.domain.util.AchievementEngine
import dev.sagi.monotask.domain.util.ActivityStats
import dev.sagi.monotask.ui.component.core.ActionButton
import dev.sagi.monotask.ui.component.core.AvatarBox
import dev.sagi.monotask.ui.component.core.EmptyState
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.core.LevelChip
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.component.core.SectionTitle
import dev.sagi.monotask.ui.component.core.StreakChip
import dev.sagi.monotask.ui.component.core.StreakChipSize
import dev.sagi.monotask.ui.component.statistics.LineChart
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.XpViolet

// ====================
// Tab 3: Social. invite link + friends list
// ====================

@Composable
fun SocialTab(
    friendUsers      : List<User>?,
    friendActivities : Map<String, List<DailyActivity>>,
    onShareInvite    : () -> Unit,
    topPadding       : Dp,
    bottomPadding    : Dp
) {
    when {
        friendUsers == null -> Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding),
            contentAlignment = Alignment.Center
        ) {
            LoadingSpinner()
        }
//        friendUsers.isEmpty() -> FriendsEmptyState(
//            topPadding    = topPadding,
//            bottomPadding = bottomPadding,
////            onShareInvite = onShareInvite
//        )
        friendUsers.isEmpty() -> EmptyState(
            title    = "No friends yet",
            subtitle = "Share your invite link to connect with friends"
        )

        else -> FriendsListState(
            friendUsers      = friendUsers,
            friendActivities = friendActivities,
            topPadding       = topPadding,
            bottomPadding    = bottomPadding,
            onShareInvite    = onShareInvite
        )
    }
}

// ====================
// Empty state
// ====================

//@Composable
//private fun FriendsEmptyState(
//    topPadding: Dp,
//    bottomPadding: Dp,
////    onShareInvite: () -> Unit
//) {
//    Box(
//        modifier         = Modifier
//            .fillMaxSize()
//            .padding(top = topPadding, bottom = bottomPadding),
//        contentAlignment = Alignment.Center
//    ) {
//        EmptyState(
//            title    = "No friends yet",
//            subtitle = "Share your invite link to connect with friends"
//        )
//    }
//}

// ====================
// Non-empty state
// ====================

@Composable
private fun FriendsListState(
    friendUsers      : List<User>,
    friendActivities : Map<String, List<DailyActivity>>,
    topPadding       : Dp,
    bottomPadding    : Dp,
    onShareInvite    : () -> Unit
) {
    var expandedFriendId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(top = topPadding, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header + inline invite button
        item {
            SectionTitle("Friends") {
                Row(
                    modifier              = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onShareInvite)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_add_user),
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text       = "Invite",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Friend rows (expandable, one at a time)
        items(friendUsers, key = { it.id }) { user ->
            FriendRow(
                user       = user,
                activities = friendActivities[user.id] ?: emptyList(),
                expanded   = expandedFriendId == user.id,
                onToggle   = {
                    expandedFriendId = if (expandedFriendId == user.id) null else user.id
                }
            )
        }
    }
}

// ====================
// Expandable friend row
// ====================

@Composable
private fun FriendRow(
    user      : User,
    activities: List<DailyActivity>,
    expanded  : Boolean,
    onToggle  : () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label       = "chevron"
    )
    val shape = MaterialTheme.shapes.large

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
        shape   = shape,
        blurred = false
    ) {
        Column {
            // Collapsed header: clickable here only
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .clickable { onToggle() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AvatarBox(
                        user     = user,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text       = user.displayName,
                                style      = MaterialTheme.typography.titleMedium,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Normal,
                                modifier   = Modifier.padding(start = 2.dp) // optical correction
                            )
                            LevelChip(user.level)
                        }
                        val liveStreak = remember(activities) {
                            ActivityStats.computeCurrentStreak(activities)
                        }
                        StreakChip(
                            currentStreak = liveStreak,
                            size          = StreakChipSize.SMALL
                        )
                    }
                }
                Icon(
                    painter            = painterResource(R.drawable.ic_chevron),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation)
                )
            }

            // Expanded content (not clickable)
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                FriendExpandedContent(user = user, activities = activities)
            }
        }
    }
}

// ====================
// Expanded section content
// ====================

@Composable
private fun FriendExpandedContent(user: User, activities: List<DailyActivity>) {
    val stats = user.stats

    val badges = remember(stats, user.level) {
        AchievementEngine.evaluateFromStats(stats, user.level)
    }

    val weekActivity = remember(activities) { ActivityStats.weekActivity(activities) }
    val xpPoints     = remember(weekActivity) { ActivityStats.buildXpPoints(weekActivity) }
    val xpTrend      = remember(weekActivity) { ActivityStats.computeXpTrend(weekActivity) }
    val totalWeekXp  = remember(weekActivity) { weekActivity.sumOf { it.xpEarned } }

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val titleContentPadding = 8.dp

        // Achievements
        Column(
            verticalArrangement = Arrangement.spacedBy(titleContentPadding - 2.dp)
        ) {
            SectionTitle("Achievements")
            AchievementSectionRow(
                achievements = badges,
                badgeStyle   = AchievementBadgeStyle.CONCISE
            )
        }


        // Weekly XP line chart (condensed)
        Column(
            verticalArrangement = Arrangement.spacedBy(titleContentPadding)
        ) {
            SectionTitle("Weekly Activity")
            LineChart(
                headlineValue     = "$totalWeekXp",
                headlineUnit = "xp",
                points       = xpPoints,
                trendPercent = xpTrend,
                lineColor    = XpViolet,
                animate      = false,
                chartHeight  = 80.dp,
                shape = MaterialTheme.shapes.medium,
                modifier     = Modifier.fillMaxWidth()
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SocialTabEmptyPreview() {
    MonoTaskTheme {
        SocialTab(
            friendUsers      = emptyList(),
            friendActivities = emptyMap(),
            onShareInvite    = {},
            topPadding       = 0.dp,
            bottomPadding    = 0.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SocialTabWithFriendsPreview() {
    MonoTaskTheme {
        SocialTab(
            friendUsers      = listOf(
                User(id = "1", displayName = "Roei Zalah",  level = 12, xp = 3400,
                     stats = UserStats(totalTasksCompleted = 87, aceCount = 52, currentStreak = 5, longestStreak = 14, weeklyXp = 340)),
                User(id = "2", displayName = "Ofek Fanian", level = 7,  xp = 1200)
            ),
            friendActivities = emptyMap(),
            onShareInvite    = {},
            topPadding       = 0.dp,
            bottomPadding    = 0.dp
        )
    }
}
