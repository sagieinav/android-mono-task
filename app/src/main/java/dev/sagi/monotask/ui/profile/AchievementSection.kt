package dev.sagi.monotask.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.AchievementCategory
import dev.sagi.monotask.data.model.AchievementColorBronze
import dev.sagi.monotask.data.model.AchievementColorGold
import dev.sagi.monotask.data.model.AchievementColorSilver
import dev.sagi.monotask.data.model.AchievementMilestone
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.ui.component.core.GlassDialog
import dev.sagi.monotask.ui.component.core.GlassTooltip
import dev.sagi.monotask.ui.component.core.SectionTitle
import dev.sagi.monotask.ui.theme.HexagonShape
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.ui.theme.glassBorderPremium
import dev.sagi.monotask.ui.theme.monoShadow

enum class AchievementBadgeStyle { FULL, CONCISE }

// ====================
// Achievements section, single row (friend view)
// ====================

@Composable
fun AchievementSectionRow(
    achievements : List<Achievement>,
    badgeStyle   : AchievementBadgeStyle = AchievementBadgeStyle.FULL,
    modifier     : Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        achievements.forEach { achievement ->
            HexagonAchievementBadge(
                achievement     = achievement,
                badgeStyle      = badgeStyle,
//                hexSizeFraction = 1f,
                modifier        = Modifier.weight(1f)
                    // padding decreases the badges' size
                    .padding(horizontal = 2.dp)
            )
        }
    }
}

// ====================
// Single hexagon badge
// ====================

@Composable
fun HexagonAchievementBadge(
    achievement     : Achievement,
    badgeStyle      : AchievementBadgeStyle = AchievementBadgeStyle.FULL,
    modifier        : Modifier = Modifier
) {
    val tierColor = achievement.tierColor
    val iconColor = lerp(tierColor, Color.Black, fraction = 0.15f)
    val alpha = if (achievement.isLocked) 0.35f else 1f

    var showTooltip by remember { mutableStateOf(false) }

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .monoShadow(HexagonShape, 8.dp)
                .clip(HexagonShape)
                .clickable { showTooltip = !showTooltip }
                .glassBackground(baseColor = tierColor)

                .border(shape = HexagonShape, color = tierColor, width = 1.dp)
                .glassBorderPremium(HexagonShape, width = 4.dp)

                .alpha(alpha)

        ) {
            val iconSize = maxWidth * 0.5f
            Icon(
                painter            = painterResource(achievement.iconRes),
                contentDescription = "Badge Icon",
                tint               = iconColor,
                modifier           = Modifier.size(iconSize)
            )

            // Tooltip anchored to the hexagon's bounds
            GlassTooltip(
                expanded  = showTooltip,
                onDismiss = { showTooltip = false }
            ) {
                AchievementTooltipContent(
                    achievement  = achievement,
                    style        = badgeStyle,
                    tierColor    = tierColor
                )
            }
        }

        if (badgeStyle == AchievementBadgeStyle.FULL) {
            Text(
                text       = achievement.displayName,
                style      = MaterialTheme.typography.labelSmall.copy(
                    lineBreak = LineBreak(
                        strategy = LineBreak.Strategy.Simple,
                        strictness = LineBreak.Strictness.Strict,
                        wordBreak = LineBreak.WordBreak.Default
                    )
                ),
//                fontWeight = FontWeight.Medium,
                textAlign  = TextAlign.Center,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha),
                modifier   = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}


// ====================
// Tooltip content
// ====================

@Composable
private fun AchievementTooltipContent(
    achievement : Achievement,
    style       : AchievementBadgeStyle,
    tierColor   : Color
) {
    when (style) {
        AchievementBadgeStyle.FULL -> {
            // Line 1: CURRENT earned tier description
            achievement.earnedMilestone?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text       = "CURRENT",
                        fontWeight = FontWeight.Bold,
                        color      = tierColor
                    )
                    Text(
                        text  = it.description,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            // Line 2: NEXT tier description
            achievement.nextTierColor?.let { nextColor ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text       = "NEXT",
                        fontWeight = FontWeight.Bold,
                        color      = nextColor
                    )
                    achievement.nextTier?.description?.let { text ->
                        Text(
                            text  = text,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
        AchievementBadgeStyle.CONCISE -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Line 1: badge display name
                Text(
                    text  = achievement.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                // Line 2: current tier description (muted), if earned
                achievement.earnedMilestone?.let {
                    Text(
                        text  = it.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}


// ========== Preview ==========
private fun previewAchievementMilestone(tier: AchievementTier, name: String, description: String) =
    AchievementMilestone(tier = tier, name = name, description = description)

@Preview(showBackground = true, name = "AchievementsSectionRow")
@Composable
private fun AchievementsSectionRowPreview() {
    MonoTaskTheme {
        AchievementSectionRow(
            achievements = listOf(
                Achievement(
                    category   = AchievementCategory.STREAKS,
                    iconRes    = R.drawable.ic_fire,
                    earnedTier = AchievementTier.SILVER,
                    bronze     = previewAchievementMilestone(AchievementTier.BRONZE, "First Flame",      "Active 3 days in a row"),
                    silver     = previewAchievementMilestone(AchievementTier.SILVER, "Consistency King", "Active 7 days in a row"),
                    gold       = previewAchievementMilestone(AchievementTier.GOLD,   "Unstoppable",      "Active 30 days in a row")
                ),
                Achievement(
                    category   = AchievementCategory.TASK_VOLUME,
                    iconRes    = R.drawable.ic_task_alt,
                    earnedTier = AchievementTier.GOLD,
                    bronze     = previewAchievementMilestone(AchievementTier.BRONZE, "Warming Up",   "Complete 5 tasks"),
                    silver     = previewAchievementMilestone(AchievementTier.SILVER, "Century",      "Complete 100 tasks"),
                    gold       = previewAchievementMilestone(AchievementTier.GOLD,   "Task Machine", "Complete 500 tasks")
                ),
                Achievement(
                    category   = AchievementCategory.DISCIPLINE,
                    iconRes    = R.drawable.ic_bolt,
                    earnedTier = AchievementTier.BRONZE,
                    bronze     = previewAchievementMilestone(AchievementTier.BRONZE, "No Excuses",    "50%+ ace ratio (20+ tasks)"),
                    silver     = previewAchievementMilestone(AchievementTier.SILVER, "Iron Will",     "70%+ ace ratio (20+ tasks)"),
                    gold       = previewAchievementMilestone(AchievementTier.GOLD,   "Denial Denier", "90%+ ace ratio (20+ tasks)")
                ),
                Achievement(
                    category   = AchievementCategory.XP_LEVELING,
                    iconRes    = R.drawable.ic_star_shine,
                    earnedTier = null,
                    bronze     = previewAchievementMilestone(AchievementTier.BRONZE, "Rising Star", "Reach level 5"),
                    silver     = previewAchievementMilestone(AchievementTier.SILVER, "Veteran",     "Reach level 15"),
                    gold       = previewAchievementMilestone(AchievementTier.GOLD,   "Legend",      "Reach level 30")
                )
            )
        )
    }
}