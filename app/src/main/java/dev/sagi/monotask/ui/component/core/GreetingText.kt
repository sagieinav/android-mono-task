package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme


@Composable
fun HeroGreeting(
    userName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GreetingText(
            title = "Hi, ${userName.substringBefore(" ")}",
        )
    }
}


@Composable
fun GreetingText(
    title: String,
    subtitle: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.displaySmall,
    titleWeight: FontWeight = FontWeight.Normal,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = titleStyle,
            fontWeight = titleWeight,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier
        )
        subtitle?. let {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ─────────────────────────────────────────
// Previews
// ─────────────────────────────────────────
@Preview(showBackground = true, name = "Hero size")
@Composable
private fun GreetingTextHeroPreview() {
    MonoTaskTheme {
        GreetingText(
            title = "Hi, Sagi",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "With subtitle")
@Composable
private fun GreetingTextSubtitlePreview() {
    MonoTaskTheme {
        GreetingText(
            title = "Hi, Sagi",
            subtitle = "3 tasks remaining",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "TopBar size")
@Composable
private fun GreetingTextTopBarPreview() {
    MonoTaskTheme {
        GreetingText(
            title = "Sagi Einav",
            titleStyle = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}
