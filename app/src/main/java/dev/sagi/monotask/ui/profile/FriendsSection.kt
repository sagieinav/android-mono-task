package dev.sagi.monotask.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.domain.service.AchievementEngine
import dev.sagi.monotask.domain.service.ActivityStats
import dev.sagi.monotask.ui.component.core.AvatarBox
import dev.sagi.monotask.ui.component.display.EmptyState
import dev.sagi.monotask.ui.component.core.GlassConfirmDialog
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.core.SwipeRevealAction
import dev.sagi.monotask.ui.component.core.SwipeRevealRow
import dev.sagi.monotask.ui.theme.penaltyRed
import dev.sagi.monotask.ui.component.display.LevelChip
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.component.display.SectionTitle
import dev.sagi.monotask.ui.component.display.StreakChip
import dev.sagi.monotask.ui.component.display.StreakChipSize
import dev.sagi.monotask.ui.component.display.LineChart
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors
import dev.sagi.monotask.ui.theme.clickableNoRipple
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING
import dev.sagi.monotask.util.Constants.Theme.TRAILING_BUTTON_SIZE

// ====================
// Friends section (header + list)
// ====================

@Composable
fun FriendsSection(
    friendUsers      : List<User>?,
    friendActivities : Map<String, List<DailyActivity>>,
    onShareInvite    : () -> Unit,
    onDeleteFriend   : (String) -> Unit = {},
    lazyListState    : LazyListState? = null
) {
    var expandedFriendId    by remember { mutableStateOf<String?>(null) }
    var deleteTargetFriend  by remember { mutableStateOf<User?>(null) }
    val friendRowShape = MaterialTheme.shapes.large

    Column {
        SectionTitle("Friends") {
            // Invite Button
            Row(
                modifier              = Modifier
                    .clip(CircleShape)
                    .clickableNoRipple(onClick = onShareInvite)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp)
                    .alignByBaseline(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                Icon(
                    painter            = painterResource(R.drawable.ic_add_user),
                    contentDescription = null,
                    modifier           = Modifier.size(13.dp),
                    tint               = color
                )
                Text(
                    text  = "Invite",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Thin,
                    color = color
                )
            }
        }

        when {
            friendUsers == null -> LoadingSpinner()
            friendUsers.isEmpty() -> EmptyState(
                title    = "No friends yet",
                subtitle = "Share your invite link to connect with friends",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            else -> Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                friendUsers.forEach { user ->
                    FriendRow(
                        user          = user,
                        shape         = friendRowShape,
                        activities    = friendActivities[user.id] ?: emptyList(),
                        expanded      = expandedFriendId == user.id,
                        onToggle      = { expandedFriendId = if (expandedFriendId == user.id) null else user.id },
                        onDelete      = { deleteTargetFriend = user },
                        lazyListState = lazyListState
                    )
                }
            }
        }
    }

    deleteTargetFriend?.let { friend ->
        GlassConfirmDialog(
            onDismissRequest = { deleteTargetFriend = null },
            title            = "Remove '${friend.displayName}'?",
            message          = "They will be removed from your friends list.",
            confirmLabel     = "Remove",
            dismissLabel     = "Cancel",
            confirmColor     = penaltyRed,
            onConfirm        = {
                onDeleteFriend(friend.id)
                deleteTargetFriend = null
            }
        )
    }
}

// ====================
// Expandable friend row
// ====================

@Composable
private fun FriendRow(
    user: User,
    activities: List<DailyActivity>,
    expanded: Boolean,
    shape: Shape = MaterialTheme.shapes.large,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    lazyListState: LazyListState? = null
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "chevronRotation"
    )

    val liveStreak = remember(activities) {
        user.stats.currentStreak
//        ActivityStats.computeCurrentStreak(activities)
    }
    var expandedContentHeightPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(expanded) {
        if (expanded && lazyListState != null) {
            withFrameNanos { }
            if (expandedContentHeightPx > 0) {
                lazyListState.animateScrollBy(expandedContentHeightPx.toFloat())
            }
        }
    }

    SwipeRevealRow(
        shape = shape,
        modifier = Modifier.fillMaxWidth(),
        endAction = SwipeRevealAction(
            color = penaltyRed,
            icon = R.drawable.ic_delete,
            label = "Remove",
            onTriggered = onDelete
        )
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape),
            shape = shape,
            blurred = false
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .clickable { onToggle() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AvatarBox(
                        user = user,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                            LevelChip(user.level)
                        }

                        StreakChip(
                            currentStreak = liveStreak,
                            size = StreakChipSize.SMALL
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.ic_chevron),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(TRAILING_BUTTON_SIZE)
                            .rotate(chevronRotation)
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Box(modifier = Modifier.onSizeChanged { expandedContentHeightPx = it.height }) {
                        FriendExpandedContent(user = user, activities = activities)
                    }
                }
            }
        }
    }
}

// ====================
// Expanded friend content
// ====================

@Composable
private fun FriendExpandedContent(user: User, activities: List<DailyActivity>) {
    val stats        = user.stats
    val badges       = remember(stats, user.level) { AchievementEngine.evaluateFromStats(stats, user.level) }
    val weekActivity = remember(activities) { ActivityStats.weekActivity(activities) }
    val xpPoints     = remember(weekActivity) { ActivityStats.buildXpPoints(weekActivity) }
    val xpTrend      = remember(weekActivity) { ActivityStats.computeXpTrend(weekActivity) }
    val totalWeekXp  = remember(weekActivity) { weekActivity.sumOf { it.xpEarned } }

    val topPadding    = SCREEN_PADDING / 2
    val bottomPadding = SCREEN_PADDING
    val horizPadding  = SCREEN_PADDING
    val contentPadding = SCREEN_PADDING * 1.25f

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizPadding)
            .padding(bottom = bottomPadding, top = topPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        AchievementSectionRow(
            achievements = badges,
            badgeStyle   = AchievementBadgeStyle.CONCISE,
            modifier     = Modifier.padding(horizontal = 4.dp) // optical correction
        )

        LineChart(
            title         = "Weekly Activity",
            headlineValue = "$totalWeekXp",
            headlineUnit  = "xp",
            points        = xpPoints,
            trendPercent  = xpTrend,
            lineColor     = MaterialTheme.customColors.xp,
            animate       = false,
            chartHeight   = 80.dp,
            shape         = MaterialTheme.shapes.medium,
            modifier      = Modifier.fillMaxWidth()
        )
    }
}

// ========== Preview ==========

private val previewFriend1 = User(id = "1", displayName = "Roei Zalah", level = 12, xp = 4800)
private val previewFriend2 = User(id = "2", displayName = "Ofek Fanian", level = 7, xp = 1950)
private val previewActivities = listOf(
    DailyActivity(dateEpochDay = 20000L, tasksCompleted = 3, xpEarned = 120),
    DailyActivity(dateEpochDay = 20001L, tasksCompleted = 5, xpEarned = 200),
    DailyActivity(dateEpochDay = 20002L, tasksCompleted = 2, xpEarned = 80),
    DailyActivity(dateEpochDay = 20003L, tasksCompleted = 4, xpEarned = 160),
    DailyActivity(dateEpochDay = 20004L, tasksCompleted = 6, xpEarned = 240),
    DailyActivity(dateEpochDay = 20005L, tasksCompleted = 1, xpEarned = 40),
    DailyActivity(dateEpochDay = 20006L, tasksCompleted = 3, xpEarned = 120),
)

@Preview(showBackground = true, name = "FriendsSection – with friends")
@Composable
private fun FriendsSectionPreview() {
    MonoTaskTheme {
        FriendsSection(
            friendUsers      = listOf(previewFriend1, previewFriend2),
            friendActivities = mapOf(
                previewFriend1.id to previewActivities,
                previewFriend2.id to previewActivities.take(4)
            ),
            onShareInvite    = {}
        )
    }
}

@Preview(showBackground = true, name = "FriendsSection – empty")
@Composable
private fun FriendsSectionEmptyPreview() {
    MonoTaskTheme {
        FriendsSection(
            friendUsers      = emptyList(),
            friendActivities = emptyMap(),
            onShareInvite    = {}
        )
    }
}