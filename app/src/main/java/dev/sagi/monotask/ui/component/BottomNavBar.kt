@file:OptIn(ExperimentalHazeMaterialsApi::class)

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.GlassSurface
import dev.sagi.monotask.ui.component.GlassSurfaceElevated
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.MonoTaskTheme

enum class NavTab(val label: String, val iconRes: Int) {
    BOARD("Board", R.drawable.ic_view_kanban),
    FOCUS("Focus", R.drawable.ic_fire),
    PROFILE("You", R.drawable.ic_account_circle)
}

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        GlassSurface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab.entries.forEach { tab ->
                    NavDockItem(
                        tab = tab,
                        isSelected = tab == selectedTab,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavDockItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dotAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "dot_alpha"
    )

    val iconSize = 36
    val dotOffset = iconSize / 1.6

    // A Box perfectly centers its contents independently
    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clip(MaterialTheme.shapes.medium) // Using your MaterialTheme shapes rule!
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Because of the Box, the Icon is now mathematically dead-center.
        Icon(
            painter = painterResource(tab.iconRes),
            contentDescription = tab.label,
            modifier = Modifier.size(iconSize.dp),
            tint = if (isSelected)
                MaterialTheme.colorScheme.scrim
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )

        // Dot indicator
        Box(
            modifier = Modifier
                .offset(y = dotOffset.dp)
                .size(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = dotAlpha),
                    shape = CircleShape
                )
        )
    }
}

// ========== Preview ==========
@Preview(showBackground = true)
@Composable
fun BottomNavBarPreview() {
    val hazeState = LocalHazeState.current
    var selected by remember { mutableStateOf(NavTab.FOCUS) }

    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter
        ) {

            // ========== THE BACKGROUND (hazeSource) ==========
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Just some long ass text",
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ========== THE FOREGROUND (hazeEffect) ==========
            BottomNavBar(
                selectedTab = selected,
                onTabSelected = { selected = it },
            )
        }
    }
}