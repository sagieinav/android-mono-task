package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import dev.sagi.monotask.ui.theme.gloock

@Composable
fun SectionTitle(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.titleMedium,
        color      = MaterialTheme.colorScheme.onSurfaceVariant,
//        textAlign  = TextAlign.Center,
        modifier   = Modifier.fillMaxWidth()
    )
}

//@Composable
//fun SectionTitle(text: String) {
//    Text(
//        text       = text,
//        style      = MaterialTheme.typography.headlineSmall,
////        fontFamily = gloock,
//        fontWeight = FontWeight.Bold,
//        color      = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
//        textAlign  = TextAlign.Center,
//        modifier   = Modifier.fillMaxWidth()
//    )
//}
