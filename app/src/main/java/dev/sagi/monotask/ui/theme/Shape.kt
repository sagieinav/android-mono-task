package dev.sagi.monotask.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Map/override the 5 default shapes of M3
val MonoTaskShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // Chips, small badges
    small      = RoundedCornerShape(12.dp),  // Input fields, snackbars
    medium     = RoundedCornerShape(16.dp),  // Kanban task cards
    large      = RoundedCornerShape(24.dp),  // FocusCard, bottom sheets
    extraLarge = RoundedCornerShape(32.dp)   // Dialogs, full overlays
)
