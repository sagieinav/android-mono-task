package dev.sagi.monotask.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun HeroGreeting(
    userName: String,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val firstName = userName.substringBefore(" ")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Hi, $firstName",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.hazeSource(hazeState)
        )
    }
}