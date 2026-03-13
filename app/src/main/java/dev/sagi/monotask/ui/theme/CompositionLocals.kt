package dev.sagi.monotask.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState

val LocalHazeState = staticCompositionLocalOf { HazeState() }
val LocalCustomColors = staticCompositionLocalOf { lightCustomColors }
val LocalScaffoldPadding = staticCompositionLocalOf<PaddingValues> { PaddingValues(0.dp) }

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided.")
}
