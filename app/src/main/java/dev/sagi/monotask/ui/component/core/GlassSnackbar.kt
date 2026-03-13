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
import dev.sagi.monotask.ui.theme.ibmPlexMono
import dev.sagi.monotask.ui.theme.monoShadow
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import dev.sagi.monotask.ui.theme.plusJakartaSans
import dev.sagi.monotask.ui.theme.roboto

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
            .wrapContentSize()
//            .fillMaxWidth()
        ,
        shape = CircleShape,
        blurred = true
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp) // internal padding
//                .fillMaxWidth()
            ,
            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // The main message (Takes up remaining space)
            Text(
                text = snackbarData.visuals.message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = ibmPlexMono,
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
//                TextButton(
//                    onClick = { snackbarData.performAction() },
//                    contentPadding = PaddingValues(0.dp),
//                    modifier = Modifier.heightIn(max = 28.dp)
//                ) {
//                    Text(
//                        text = snackbarData.visuals.actionLabel!!,
//                        color = MaterialTheme.colorScheme.primary,
//                        style = MaterialTheme.typography.labelMedium.copy(
//                            fontWeight = FontWeight.Bold
//                        ),
//                        fontFamily = ibmPlexMono
//                    )
//                }
            }
        }
    }
}