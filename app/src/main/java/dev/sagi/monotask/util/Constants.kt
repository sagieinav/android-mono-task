package dev.sagi.monotask.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

class Constants {
    object Theme {
        // Not putting this on MainScaffold, to allow smooth horizontal scrolling in KanbanScreen
        val SCREEN_PADDING = 16.dp
        val TOP_BAR_ITEM_HEIGHT = 46.dp
    }
}