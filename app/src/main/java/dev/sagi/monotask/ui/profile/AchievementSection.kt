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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Achievement
import dev.sagi.monotask.data.model.AchievementColorBronze
import dev.sagi.monotask.data.model.AchievementColorGold
import dev.sagi.monotask.data.model.AchievementColorSilver
import dev.sagi.monotask.data.model.AchievementTier
import dev.sagi.monotask.ui.component.core.GlassDialog
import dev.sagi.monotask.ui.component.core.GlassTooltip
import dev.sagi.monotask.ui.component.core.SectionTitle
import dev.sagi.monotask.ui.theme.HexagonShape
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.ui.theme.glassBorderPremium
import dev.sagi.monotask.ui.theme.monoShadow

// ====================
// Achievements section — 2x2 grid
// ====================

@Composable
internal fun AchievementsSection(
    achievements : List<Achievement>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionTitle("Achievements")
//        Text(
//            text       = "Achievements",
//            style      = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.SemiBold
//        )
        achievements.chunked(2).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { achievement ->
                    HexagonAchievementBadge(
                        achievement = achievement,
                        modifier    = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ====================
// Single hexagon badge
// ====================

@Composable
fun HexagonAchievementBadge(
    achievement : Achievement,
    modifier    : Modifier = Modifier
) {
    val tierColor = achievement.tierColor
    val iconColor = lerp(tierColor, Color.Black, fraction = 0.15f)
    val alpha = if (achievement.isLocked) 0.35f else 1f

    // fraction controls size
    val sizeFraction = 0.7f

    var showTooltip by remember { mutableStateOf(false) }

    Column(
        modifier            = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier         = Modifier

                .fillMaxWidth(sizeFraction)
                .aspectRatio(1f)
                .monoShadow(HexagonShape, 8.dp)
                .clip(HexagonShape)
                .clickable { showTooltip = !showTooltip }
                .glassBackground(baseColor = tierColor)
                // Design opt 1:
//                .border(shape = HexagonShape, color = tierColor, width = 1.5.dp)
//                .glassBorder(HexagonShape, color = tierColor, width = 8.dp)

                // Design opt 2:
                .border(shape = HexagonShape, color = tierColor, width = 1.dp)
//                .glassBorder(HexagonShape, width = 4.dp)
                .glassBorderPremium(HexagonShape, width = 4.dp)

                // Design opt 3:
//                .border(shape = HexagonShape, color = Color.White.copy(alpha = 0.45f), width = 3.dp)
//                .border(shape = HexagonShape, color = tierColor, width = 4.5.dp)

                // Design opt 4:
//                .border(shape = HexagonShape, color = tierColor, width = 1.5.dp)
//                .border(shape = HexagonShape, color = Color.White.copy(alpha = 0.45f), width = 4.5.dp)
//                .border(shape = HexagonShape, color = tierColor, width = 6.dp)

                .alpha(alpha)

        ) {
            val iconSize = maxWidth * sizeFraction * 0.7f
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
        }

        Text(
            text       = achievement.displayName,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onSurface.copy(alpha),
            modifier   = Modifier.fillMaxWidth(0.8f)
        )
        Text(
            text       = achievement.category.displayName,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.outline.copy(0.7f),
            modifier   = Modifier.fillMaxWidth(0.8f)
        )
    }
}

// ====================
// Achievement detail dialog (for future use)
// ====================

@Composable
private fun AchievementDetailDialog(
    achievement : Achievement,
    onDismiss   : () -> Unit
) {
    GlassDialog(
        onDismissRequest = onDismiss,
        title            = achievement.category.displayName,
        content          = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Triple(AchievementTier.BRONZE, achievement.bronze, AchievementColorBronze),
                    Triple(AchievementTier.SILVER, achievement.silver, AchievementColorSilver),
                    Triple(AchievementTier.GOLD,   achievement.gold,   AchievementColorGold),
                ).forEach { (tier, tierDef, tierColor) ->
                    val earned   = achievement.isEarned(tier)
                    val rowAlpha = if (earned) 1f else 0.4f
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier              = Modifier.alpha(rowAlpha)
                    ) {
                        Icon(
                            painter            = painterResource(R.drawable.ic_achievement_hexagon),
                            contentDescription = null,
                            tint               = tierColor,
                            modifier           = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = tierDef.name,
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text  = tierDef.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (earned) {
                            Text(
                                text       = "✓",
                                color      = tierColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                achievement.nextTier?.let { next ->
                    Text(
                        text  = "Next: ${next.description}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }
        },
        buttons = {
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text("Close", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
