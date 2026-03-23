package dev.sagi.monotask.ui.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.ui.component.core.AvatarBox
import dev.sagi.monotask.ui.component.display.StreakChip
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING

@Composable
fun UserHeader(
    user          : User?,
    currentStreak : Int,
    modifier      : Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = SCREEN_PADDING)
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
    }
}
