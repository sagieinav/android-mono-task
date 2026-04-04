package dev.sagi.monotask.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme

private const val SUBTLE_CHAR_DELAY_MS          = 30L
private const val SUBTLE_SUBTITLE_CHAR_DELAY_MS = 20L
private const val SUBTLE_DELAY_BEFORE_MS        = 600L

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emoji: String? = null,
    @DrawableRes imgRes: Int? = null,
    size: IllustrationSize = IllustrationSize.Small,
    action: (@Composable () -> Unit)? = null
) {
    when (size) {
        IllustrationSize.Large -> StateMessage(
            title = title,
            subtitle = subtitle,
            imgRes = imgRes,
            size = IllustrationSize.Large,
            modifier = modifier,
            action = action
        )
        IllustrationSize.Small -> SubtleEmptyStateContent(title, subtitle, emoji, modifier, action)
    }
}

@Composable
private fun SubtleEmptyStateContent(
    title: String,
    subtitle: String?,
    emoji: String?,
    modifier: Modifier,
    action: (@Composable () -> Unit)?
) {
    val titleWithEmoji = if (emoji != null) "$title $emoji" else title

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        TypewriterText(
            text = titleWithEmoji,
            delayBefore = SUBTLE_DELAY_BEFORE_MS,
            charDelay = SUBTLE_CHAR_DELAY_MS,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            TypewriterText(
                text = it,
                delayBefore = SUBTLE_DELAY_BEFORE_MS + (titleWithEmoji.length * SUBTLE_CHAR_DELAY_MS),
                charDelay = SUBTLE_SUBTITLE_CHAR_DELAY_MS,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(12.dp))
            it()
        }
    }
}

@Preview(showBackground = true, name = "EmptyState — with emoji")
@Composable
private fun FocusEmptyStatePreview() {
    MonoTaskTheme {
        EmptyState(
            emoji = "🦾",
            title = "You're all caught up!",
            subtitle = "No tasks here. Enjoy the moment.",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "EmptyState — no emoji")
@Composable
private fun FriendsEmptyStatePreview() {
    MonoTaskTheme {
        EmptyState(
            title = "No friends yet",
            subtitle = "Share your invite link to connect with friends",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "EmptyState — Large")
@Composable
private fun LargeEmptyStatePreview() {
    MonoTaskTheme {
        EmptyState(
            emoji = "📭",
            title = "Nothing here yet",
            subtitle = "Check back later",
            size = IllustrationSize.Large,
            modifier = Modifier.padding(16.dp)
        )
    }
}
