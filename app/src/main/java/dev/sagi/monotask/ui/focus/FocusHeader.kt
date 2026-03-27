package dev.sagi.monotask.ui.focus

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.component.core.AvatarBox
import dev.sagi.monotask.ui.component.display.StreakChip
import dev.sagi.monotask.ui.theme.LocalCustomColors
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING
import kotlinx.coroutines.delay

@Composable
fun UserHeader(
    user          : User?,
    currentStreak : Int,
    modifier      : Modifier = Modifier,
    levelUpEvent  : FocusUiEffect.ShowLevelUp? = null,
    onLevelUpDone : () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SCREEN_PADDING)
    ) {
        user?.let {
            AvatarBox(
                user = it,
                modifier = Modifier.size(58.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = user?.displayName ?: "",
                style      = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier
                    .padding(start = 1.dp) // optical correction
            )
            StreakChip(currentStreak)
        }
        Spacer(Modifier.weight(1f))
        LevelUpBadge(event = levelUpEvent, onAnimationEnd = onLevelUpDone)
    }
}

@Composable
private fun LevelUpBadge(
    event         : FocusUiEffect.ShowLevelUp?,
    onAnimationEnd: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val shape        = RoundedCornerShape(50)

    // Keep last non-null value so text doesn't blank during the exit animation
    val displayEvent = remember { mutableStateOf(event) }
    LaunchedEffect(event) { if (event != null) displayEvent.value = event }

    // Auto-dismiss after 2.5s
    LaunchedEffect(event) {
        if (event != null) {
            delay(2500)
            onAnimationEnd()
        }
    }

    AnimatedVisibility(
        visible = event != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec  = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            )
        ) + scaleIn(
            initialScale  = 0.7f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            )
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
                    painter            = painterResource(R.drawable.ic_upgrade),
                    contentDescription = null,
                    tint               = customColors.xp,
                    modifier           = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text       = "Lv. ${ev.previousLevel} → ${ev.newLevel}",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = customColors.xp
                )
            }
        }
    }
}
