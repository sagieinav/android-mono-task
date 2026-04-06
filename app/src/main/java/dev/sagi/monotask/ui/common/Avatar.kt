package dev.sagi.monotask.ui.common

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.designsystem.components.MonoBottomSheet
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.designsystem.theme.glassBorderPremium
import dev.sagi.monotask.designsystem.util.Constants

/**
 * Raw avatar image. Handles auto (DiceBear URL) vs preset drawable.
 * Apply sizing and clipping from outside via [modifier].
 */
@Composable
fun AvatarImage(
    user: User,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val scaledModifier = modifier.graphicsLayer {
        scaleX = 0.95f
        scaleY = 0.95f
        translationY = size.height * 0.05f
    }
    val drawableRes = user.avatarDrawableRes
    if (user.isAutoAvatar || drawableRes == null) {
        AsyncImage(
            model = user.resolvedAvatarUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = scaledModifier
        )
    } else {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = scaledModifier
        )
    }
}

/** Avatar with standard glass treatment: clipped circle with glass background and premium border */
@Composable
fun AvatarBox(
    user: User,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainer)
            .glassBorderPremium(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AvatarImage(user = user, modifier = Modifier.fillMaxSize())
    }
}


/**
 * Bottom sheet avatar picker. Auto (DiceBear) listed first, followed by preset drawables.
 */
@Composable
fun AvatarPicker(
    user: User,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options: List<Int?> = listOf(null) + (1..IconPack.AvatarPresets.size).toList()

    MonoBottomSheet(
        title = "Choose Your Avatar",
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(Constants.Theme.SCREEN_PADDING),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.wrapContentHeight().heightIn(max = 360.dp)
        ) {
            items(options) { preset ->
                val displayUser = if (preset == null) user.copy(avatarPreset = 0)
                else user.copy(avatarPreset = preset)
                val isSelected = (preset == null && user.isAutoAvatar) ||
                        preset == user.avatarPreset
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainer)
                        .then(
                            if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier.glassBorder(shape = CircleShape, width = 3.dp)
                        )
                        .clickable { onSelect(preset ?: 0) }
                ) {
                    AvatarImage(user = displayUser, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}


// ========== Previews ==========

@Preview(showBackground = true)
@Composable
private fun AvatarPreview() {
    val user = User(avatarPreset = IconPack.AvatarMicah01)
    MonoTaskTheme {
        Row(
            modifier             = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment    = Alignment.CenterVertically
        ) {
            AvatarBox(user = user, modifier = Modifier.size(56.dp))
            AvatarBox(user = user, modifier = Modifier.size(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarPickerPreview() {
    MonoTaskTheme {
        val user = User(avatarPreset = IconPack.AvatarMicah01)
        AvatarPicker(user = user, onSelect = {}, onDismiss = {})
    }
}
