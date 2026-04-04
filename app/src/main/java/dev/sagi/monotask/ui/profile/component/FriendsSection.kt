package dev.sagi.monotask.ui.profile.component

import dev.sagi.monotask.designsystem.theme.IconPack
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
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.common.AvatarBox
import dev.sagi.monotask.designsystem.component.EmptyState
import dev.sagi.monotask.designsystem.component.MonoConfirmDialog
import dev.sagi.monotask.designsystem.component.GlassSurface
import dev.sagi.monotask.designsystem.component.MonoLabel
import dev.sagi.monotask.designsystem.component.SwipeRevealAction
import dev.sagi.monotask.designsystem.component.SwipeRevealRow
import dev.sagi.monotask.designsystem.theme.penaltyRed
import dev.sagi.monotask.designsystem.component.MonoLoadingIndicator
import dev.sagi.monotask.designsystem.component.SectionTitle
import dev.sagi.monotask.ui.common.StreakChip
import dev.sagi.monotask.ui.common.StreakChipSize
import dev.sagi.monotask.ui.statistics.component.LineChart
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.theme.clickableNoRipple
import dev.sagi.monotask.designsystem.util.Constants.Theme.SCREEN_PADDING
import dev.sagi.monotask.designsystem.util.Constants.Theme.TRAILING_BUTTON_SIZE
import dev.sagi.monotask.ui.profile.FriendStats

// ====================
// Friends section (header + list)
// ====================

@Composable
fun FriendsSection(
    friendUsers : List<User>?,
    friendStats : Map<String, FriendStats>,
    onShareInvite : () -> Unit,
    onDeleteFriend : (String) -> Unit = {},
    lazyListState : LazyListState? = null
) {
    var expandedFriendId by remember { mutableStateOf<String?>(null) }
    var deleteTargetFriend by remember { mutableStateOf<User?>(null) }
    val friendRowShape = MaterialTheme.shapes.large

    Column {
        SectionTitle("Friends") {
            // Invite Button
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickableNoRipple(onClick = onShareInvite)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp)
                    .alignByBaseline(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                Icon(
                    painter = painterResource(IconPack.AddUser),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = color
                )
                Text(
                    text = "Invite",
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
        }

        when {
            friendUsers == null -> MonoLoadingIndicator()
            friendUsers.isEmpty() -> EmptyState(
                title = "No friends yet",
                subtitle = "Share your invite link to connect with friends",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            else -> Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                friendUsers.forEach { user ->
                    FriendRow(
                        user = user,
                        shape = friendRowShape,
                        stats = friendStats[user.id],
                        expanded = (expandedFriendId == user.id),
                        onToggle = { expandedFriendId = if (expandedFriendId == user.id) null else user.id },
                        onDelete = { deleteTargetFriend = user },
                        lazyListState = lazyListState
                    )
                }
            }
        }
    }

    deleteTargetFriend?.let { friend ->
        MonoConfirmDialog(
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
    stats: FriendStats?,
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

    val liveStreak = user.stats.currentStreak
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
            icon = IconPack.Delete,
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
                            // Friend's Name:
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(start = 2.dp)
                            )

                            // Level Chip:
                            MonoLabel(
                                label = "Lv. ${user.level}",
                                textStyle = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                                verticalPadding = 2.dp,
                                horizontalPadding = 8.dp
                            )
                        }

                        StreakChip(
                            currentStreak = liveStreak,
                            size = StreakChipSize.Small
                        )
                    }

                    Icon(
                        painter = painterResource(IconPack.Chevron),
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
                        if (stats != null) FriendExpandedContent(stats = stats)
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
private fun FriendExpandedContent(stats: FriendStats) {

    val topPadding = SCREEN_PADDING / 2
    val bottomPadding = SCREEN_PADDING
    val horizPadding = SCREEN_PADDING
    val contentPadding = SCREEN_PADDING * 1.25f

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizPadding)
            .padding(bottom = bottomPadding, top = topPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        AchievementSectionRow(
            achievements = stats.badges,
            badgeStyle = AchievementBadgeStyle.CONCISE,
            modifier = Modifier.padding(horizontal = 4.dp) // optical correction
        )

        LineChart(
            title = "Weekly Activity",
            headlineValue = "${stats.totalWeekXp}",
            headlineUnit = "xp",
            points = stats.xpPoints,
            trendPercent = stats.xpTrend,
            lineColor = MaterialTheme.customColors.xp,
            animate = false,
            chartHeight = 80.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ========== Preview ==========

private val previewFriend1 = User(id = "1", displayName = "Roei Zalah", level = 12, xp = 4800)
private val previewFriend2 = User(id = "2", displayName = "Ofek Fanian", level = 7, xp = 1950)
private val previewFriendStats = FriendStats(
    badges = emptyList(),
    xpPoints = emptyList(),
    xpTrend = 12,
    totalWeekXp = 720
)

@Preview(showBackground = true, name = "FriendsSection – with friends")
@Composable
private fun FriendsSectionPreview() {
    MonoTaskTheme {
        FriendsSection(
            friendUsers = listOf(previewFriend1, previewFriend2),
            friendStats = mapOf(
                previewFriend1.id to previewFriendStats,
                previewFriend2.id to previewFriendStats
            ),
            onShareInvite = {}
        )
    }
}

@Preview(showBackground = true, name = "FriendsSection – empty")
@Composable
private fun FriendsSectionEmptyPreview() {
    MonoTaskTheme {
        FriendsSection(
            friendUsers = emptyList(),
            friendStats = emptyMap(),
            onShareInvite = {}
        )
    }
}