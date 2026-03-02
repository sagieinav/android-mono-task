package dev.sagi.monotask.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    delayBefore: Long = 0L,
    charDelay: Long = 30L,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
) {
    var displayed by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        displayed = ""
        delay(delayBefore)
        text.forEachIndexed { i, _ ->
            displayed = text.substring(0, i + 1)
            delay(charDelay)
        }
    }

    Text(
        text = displayed,
        style = style,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
fun EmptyState(
    title: String = "You're all caught up!",
    subtitle: String = "No tasks here. Enjoy the moment.",
    emoji: String = "🎉",
    modifier: Modifier = Modifier
) {
    val emojiScale = remember { Animatable(0.2f) }
    val delayBefore = 600L
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val dynamicEmojiSize = with(LocalDensity.current) { (screenWidth * 0.7f).toSp() }
    val dynamicFontSizeBig = with(LocalDensity.current) { (screenWidth * 0.18f).toSp() }
    val dynamicFontSizeSmall = with(LocalDensity.current) { (screenWidth * 0.13f).toSp() }

    LaunchedEffect(Unit) {
        emojiScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = emoji,
//            fontSize = 64.sp,
            fontSize = dynamicEmojiSize,
            modifier = Modifier
//                .scale(emojiScale.value)
                .fillMaxWidth(0.85f)
//                .align(Alignment.CenterHorizontally)
//                .aspectRatio(1f)
            ,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        TypewriterText(
            text = title,
            delayBefore = delayBefore,
            charDelay = 30L,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = dynamicFontSizeBig,
            ),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(6.dp))
        TypewriterText(
            text = subtitle,
            delayBefore = delayBefore + (title.length * 50L),  // starts after title finishes
            charDelay = 25L,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = dynamicFontSizeSmall
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    MonoTaskTheme {
        Column {
            EmptyState()
//            EmptyState(
//                emoji = "\uD83E\uDDBE",
//                title = "No open tasks",
//                subtitle = "Damn, you're a productivity machine."
//            )
//            EmptyState(
//                emoji = "📋",
//                title = "No tasks yet",
//                subtitle = "Add your first task to get started."
//            )
        }
    }
}
