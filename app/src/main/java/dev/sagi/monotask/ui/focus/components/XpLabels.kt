package dev.sagi.monotask.ui.focus.components

import androidx.compose.animation.AnimatedVisibility
import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.components.MonoLabel
import dev.sagi.monotask.designsystem.theme.LocalCustomColors
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.ui.focus.FocusUiEffect
import kotlinx.coroutines.delay

@Composable
fun XpLabel(xp: Int, modifier: Modifier = Modifier) {
    val color = MaterialTheme.customColors.xp
    MonoLabel(
        label = "$xp XP",
        accentColor = color,
        modifier = modifier,
        leadingContent = {
            Icon(
                painter = painterResource(IconPack.Xp),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
    )
}


@Composable
fun LevelUpBadge(
    event: FocusUiEffect.ShowLevelUp?,
    onAnimationEnd: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val shape = RoundedCornerShape(50)

    // Keep last non-null value so text doesn't blank during the exit animation.
    // Auto-dismiss after 2.5s.
    val displayEvent = remember { mutableStateOf(event) }
    LaunchedEffect(event) {
        if (event != null) {
            displayEvent.value = event
            delay(2500)
            onAnimationEnd()
        }
    }

    val dampingRatio = Spring.DampingRatioMediumBouncy
    val stiffness    = Spring.StiffnessMedium

    AnimatedVisibility(
        visible = event != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
        ) + scaleIn(
            initialScale = 0.7f,
            animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
        ) + fadeIn(tween(150)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250, easing = FastOutLinearInEasing)
        ) + fadeOut(tween(200))
    ) {
        displayEvent.value?.let { ev ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(shape)
                    .background(customColors.xp.copy(alpha = 0.12f))
                    .glassBorder(shape, color = customColors.xp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    painter = painterResource(IconPack.Upgrade),
                    contentDescription = null,
                    tint = customColors.xp,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Lv. ${ev.previousLevel} → ${ev.newLevel}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = customColors.xp
                )
            }
        }
    }
}
