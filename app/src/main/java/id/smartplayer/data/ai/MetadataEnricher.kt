package id.smartplayer.data.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import id.smartplayer.data.db.AppDatabase
import id.smartplayer.data.repo.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MetadataEnricher(
    private val context: Context,
    private val database: AppDatabase,
    private val aiService: AiMetadataService,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    fun enrichSeriesAsync(seriesIds: List<Long>) {
        if (!settingsRepository.autoEnrich.value) return

        scope.launch {
            if (!canUseNetwork()) return@launch

            val seriesDao = database.seriesDao()
            for (id in seriesIds) {
                val series = seriesDao.findById(id) ?: continue
                if (series.synopsis != null && series.posterUrl != null) continue

                val result = aiService.enrich(series.displayTitle, series.seasonNumber)
                result.onSuccess { meta ->
                    val genresStr = if (meta.genres.isNotEmpty()) meta.genres.joinToString(", ") else null
                    seriesDao.updateMetadata(
                        seriesId = id,
                        poster = meta.posterUrl ?: series.posterUrl,
                        backdrop = meta.backdropUrl ?: series.backdropUrl,
                        synopsis = meta.synopsis.ifBlank { series.synopsis },
                        genres = genresStr ?: series.genres,
                        totalEps = meta.totalSeasonEpisodes ?: series.totalSeasonEpisodes
                    )
                }
            }
        }
    }

    private suspend fun canUseNetwork(): Boolean = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return@withContext false
        val activeNetwork = cm.activeNetwork ?: return@withContext false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return@withContext false

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) return@withContext false

        if (settingsRepository.syncOnlyWifi.value) {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            true
        }
    }
}
