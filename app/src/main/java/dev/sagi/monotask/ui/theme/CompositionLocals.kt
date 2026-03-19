package dev.sagi.monotask.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState

val LocalHazeState = staticCompositionLocalOf { HazeState() }
val LocalCustomColors = staticCompositionLocalOf { lightCustomColors }
val LocalScaffoldPadding = staticCompositionLocalOf<PaddingValues> { PaddingValues(0.dp) }

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided.")
}

val LocalProfileTabState: ProvidableCompositionLocal<MutableState<Int>> =
    compositionLocalOf { mutableIntStateOf(0) }

