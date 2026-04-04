package dev.sagi.monotask.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.designsystem.util.Constants.Theme.SCREEN_PADDING
import dev.sagi.monotask.ui.focus.FocusUiEffect
import dev.sagi.monotask.ui.focus.component.LevelUpBadge

@Composable
fun UserHeader(
    user : User?,
    currentStreak : Int,
    modifier : Modifier = Modifier,
    levelUpEvent : FocusUiEffect.ShowLevelUp? = null,
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
                text = user?.displayName ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = 1.dp) // optical correction
            )
            StreakChip(currentStreak)
        }
        Spacer(Modifier.weight(1f))
        LevelUpBadge(event = levelUpEvent, onAnimationEnd = onLevelUpDone)
    }
}