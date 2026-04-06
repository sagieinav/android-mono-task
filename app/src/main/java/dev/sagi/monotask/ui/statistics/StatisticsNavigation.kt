package dev.sagi.monotask.ui.statistics

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.sagi.monotask.ui.navigation.StatisticsRoute

fun NavGraphBuilder.statisticsGraph() {
    composable<StatisticsRoute> {
        val statisticsVM: StatisticsViewModel = hiltViewModel()
        StatisticsScreen(statisticsVM = statisticsVM)
    }
}
