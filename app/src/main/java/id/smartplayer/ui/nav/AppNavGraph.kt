package id.smartplayer.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.smartplayer.SmartPlayerApp
import id.smartplayer.ui.dashboard.DashboardScreen
import id.smartplayer.ui.dashboard.DashboardViewModel
import id.smartplayer.ui.history.HistoryScreen
import id.smartplayer.ui.history.HistoryViewModel
import id.smartplayer.ui.player.PlayerActivity
import id.smartplayer.ui.series.SeriesDetailScreen
import id.smartplayer.ui.series.SeriesDetailViewModel
import id.smartplayer.ui.settings.SettingsScreen
import id.smartplayer.ui.settings.SettingsViewModel

object Destinations {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val SERIES_DETAIL = "series_detail/{seriesId}"
    const val SETTINGS = "settings"

    fun seriesDetail(seriesId: Long) = "series_detail/$seriesId"
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as SmartPlayerApp
    val container = app.container

    NavHost(
        navController = navController,
        startDestination = Destinations.DASHBOARD,
        modifier = modifier
    ) {
        composable(Destinations.DASHBOARD) {
            val dashboardViewModel: DashboardViewModel = viewModel {
                DashboardViewModel(container.libraryRepository, container.metadataEnricher)
            }

            DashboardScreen(
                viewModel = dashboardViewModel,
                onSeriesClick = { seriesId ->
                    navController.navigate(Destinations.seriesDetail(seriesId))
                },
                onHistoryClick = {
                    navController.navigate(Destinations.HISTORY)
                },
                onSettingsClick = {
                    navController.navigate(Destinations.SETTINGS)
                }
            )
        }

        composable(Destinations.HISTORY) {
            val historyViewModel: HistoryViewModel = viewModel {
                HistoryViewModel(container.libraryRepository, container.playbackRepository)
            }

            HistoryScreen(
                viewModel = historyViewModel,
                onBackClick = { navController.popBackStack() },
                onEpisodePlayClick = { episodeId, seriesId ->
                    val intent = PlayerActivity.createIntent(context, seriesId, episodeId)
                    context.startActivity(intent)
                }
            )
        }

        composable(
            route = Destinations.SERIES_DETAIL,
            arguments = listOf(navArgument("seriesId") { type = NavType.LongType })
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getLong("seriesId") ?: -1L
            val detailViewModel: SeriesDetailViewModel = viewModel {
                SeriesDetailViewModel(seriesId, container.libraryRepository)
            }

            SeriesDetailScreen(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() },
                onPlayClick = { episodeId, sId ->
                    val intent = PlayerActivity.createIntent(context, sId, episodeId)
                    context.startActivity(intent)
                }
            )
        }

        composable(Destinations.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel {
                SettingsViewModel(container.settingsRepository, container.libraryRepository, container.metadataEnricher)
            }

            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
