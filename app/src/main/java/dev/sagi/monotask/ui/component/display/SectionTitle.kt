package dev.sagi.monotask.ui.component.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.gloock
import java.util.Locale
import java.util.Locale.getDefault
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp

@Composable
fun SectionTitle(
    text           : String,
    modifier       : Modifier = Modifier,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.alignByBaseline()
        )

        // Optional button
        trailingContent?.invoke(this)
    }
}


// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    MonoTaskTheme {
        SectionTitle(text = "Achievements")
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun SectionTitleButtonPreview() {
//    MonoTaskTheme {
//        SectionTitle(
//            text = "Achievements",
//            trailingContent =
//        )
//    }
//}
