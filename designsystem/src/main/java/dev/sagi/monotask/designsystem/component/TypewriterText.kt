package dev.sagi.monotask.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    modifier: Modifier = Modifier,
    text: String = "Preview Text",
    delayBefore: Long = 0L,
    charDelay: Long = 30L,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    animate: Boolean = true,
) {
    var hasAnimated by remember { mutableStateOf(!animate) }
    var displayed by remember { mutableStateOf(if (!animate) text else "") }

    LaunchedEffect(text) {
        if (hasAnimated) {
            displayed = text
            return@LaunchedEffect
        }
        hasAnimated = true
        displayed = ""
        delay(delayBefore)
        text.forEachIndexed { i, _ ->
            displayed = text.substring(0, i + 1)
            delay(charDelay)
        }
    }

    Text(
        text       = displayed,
        style      = style,
        color      = color,
        fontWeight = fontWeight,
        textAlign  = textAlign,
        modifier   = modifier
    )
}