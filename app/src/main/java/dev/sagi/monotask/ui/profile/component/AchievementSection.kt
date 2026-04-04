package dev.sagi.monotask.ui.profile.component

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.AchievementCategory
import dev.sagi.monotask.data.model.AchievementMilestone
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.designsystem.component.MonoTooltip
import dev.sagi.monotask.designsystem.theme.HexagonShape
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.designsystem.theme.glassBorderPremium
import dev.sagi.monotask.designsystem.theme.monoShadow
import android.graphics.Color as AndroidColor
import dev.sagi.monotask.designsystem.theme.customColors

@Composable
private fun AchievementTier?.badgeColor(): Color {
    val colors = MaterialTheme.customColors
    return when (this) {
        AchievementTier.GOLD   -> colors.achievementGold
        AchievementTier.SILVER -> colors.achievementSilver
        AchievementTier.BRONZE -> colors.achievementBronze
        null                   -> colors.achievementLocked
    }
}

enum class AchievementBadgeStyle { FULL, CONCISE }

// ====================
// Achievements section, single row (friend view)
// ====================

@Composable
fun AchievementSectionRow(
    achievements : List<Achievement>,
    modifier     : Modifier = Modifier,
    badgeStyle   : AchievementBadgeStyle = AchievementBadgeStyle.FULL
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        achievements.forEach { achievement ->
            HexagonAchievementBadge(
                achievement     = achievement,
                badgeStyle      = badgeStyle,
                modifier        = Modifier.weight(1f)
                // padding decreases the badges' size
//                    .padding(horizontal = 2.dp)
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
    modifier        : Modifier = Modifier,
    badgeStyle      : AchievementBadgeStyle = AchievementBadgeStyle.FULL
) {
    val tierColor = achievement.earnedTier.badgeColor()
    // Icon Color: saturate a bit, mix with a bit of black, then reduce alpha
    val iconColor = lerp(
        tierColor.saturate(0.15f),
        Color.Black,
        fraction = 0.15f
    )
        .copy(alpha = 0.8f)
    val alpha = if (achievement.isLocked) 0.35f else 1f

    var showTooltip by remember { mutableStateOf(false) }

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .fillMaxWidth()
                .aspectRatio(20f / 21.3f)
                .monoShadow(HexagonShape, 8.dp)
                .clip(HexagonShape)
                .clickable { if (!achievement.isLocked) showTooltip = !showTooltip }
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
            MonoTooltip(
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
                modifier   = Modifier.fillMaxWidth()
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
            achievement.nextMilestone?.let { nextMilestone ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text       = "NEXT",
                        fontWeight = FontWeight.Bold,
                        color      = nextMilestone.tier.badgeColor()
                    )
                    Text(
                        text  = nextMilestone.description,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
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

// Helper for saturating the icons' colors
fun Color.saturate(boost: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(this.toArgb(), hsv)
    hsv[1] = (hsv[1] + boost).coerceIn(0f, 1f) // additive boost keeps low-saturation tiers consistent
    return Color(AndroidColor.HSVToColor(hsv))
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
                    iconRes    = IconPack.Fire,
                    earnedTier = AchievementTier.SILVER,
                    milestones = listOf(
                        previewAchievementMilestone(AchievementTier.BRONZE, "First Flame",      "Active 3 days in a row"),
                        previewAchievementMilestone(AchievementTier.SILVER, "Consistency King", "Active 7 days in a row"),
                        previewAchievementMilestone(AchievementTier.GOLD,   "Unstoppable",      "Active 30 days in a row")
                    )
                ),
                Achievement(
                    category   = AchievementCategory.TASK_VOLUME,
                    iconRes    = IconPack.TaskAlt,
                    earnedTier = AchievementTier.GOLD,
                    milestones = listOf(
                        previewAchievementMilestone(AchievementTier.BRONZE, "Warming Up",   "Complete 5 tasks"),
                        previewAchievementMilestone(AchievementTier.SILVER, "Century",      "Complete 100 tasks"),
                        previewAchievementMilestone(AchievementTier.GOLD,   "Task Machine", "Complete 500 tasks")
                    )
                ),
                Achievement(
                    category   = AchievementCategory.DISCIPLINE,
                    iconRes    = IconPack.Bolt,
                    earnedTier = AchievementTier.BRONZE,
                    milestones = listOf(
                        previewAchievementMilestone(AchievementTier.BRONZE, "No Excuses",    "50%+ ace ratio (20+ tasks)"),
                        previewAchievementMilestone(AchievementTier.SILVER, "Iron Will",     "70%+ ace ratio (20+ tasks)"),
                        previewAchievementMilestone(AchievementTier.GOLD,   "Denial Denier", "90%+ ace ratio (20+ tasks)")
                    )
                ),
                Achievement(
                    category   = AchievementCategory.XP_LEVELING,
                    iconRes    = IconPack.StarShine,
                    earnedTier = null,
                    milestones = listOf(
                        previewAchievementMilestone(AchievementTier.BRONZE, "Rising Star", "Reach level 5"),
                        previewAchievementMilestone(AchievementTier.SILVER, "Veteran",     "Reach level 15"),
                        previewAchievementMilestone(AchievementTier.GOLD,   "Legend",      "Reach level 30")
                    )
                )
            )
        )
    }
}