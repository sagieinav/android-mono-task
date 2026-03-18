package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.monoShadow
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import dev.sagi.monotask.ui.theme.nationalPark
import dev.sagi.monotask.ui.theme.plusJakartaSans

@Composable
fun GlassSnackbarDismissable(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    // Create the state that listens for the swipe gesture
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue != SwipeToDismissBoxValue.Settled) {
                snackbarData.dismiss()
                true
            } else {
                false
            }
        }
    )

    // Wrap the whole custom snackbar in a "Swipe to Dismiss" box
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { Spacer(modifier = Modifier.fillMaxSize()) },
        content = { GlassSnackbar(snackbarData, modifier) }
    )
}


@Composable
fun GlassSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .padding(10.dp) // external padding
            .monoShadow(CircleShape)
            .wrapContentSize(),
        shape = CircleShape,
        blurred = true
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp), // internal padding
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The main message (Takes up remaining space)
            Text(
                text = snackbarData.visuals.message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = nationalPark,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 22.dp)
            )

            // (Optional) Action Icon
            if (snackbarData.visuals.actionLabel != null) {
                // If an action is provided, SHOW ACTION
                val iconSize = 26.dp
                IconButton(
                    onClick = { snackbarData.performAction() },
                    colors = IconButtonDefaults.iconButtonColors().copy(
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .heightIn(max = iconSize)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_undo),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Undo",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}