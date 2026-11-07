package id.smartplayer.data.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import id.smartplayer.data.db.AppDatabase
import id.smartplayer.data.db.entities.SeriesEntity
import id.smartplayer.data.repo.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface EnrichState {
    data object Idle : EnrichState
    data class Progress(val currentTitle: String, val processed: Int, val total: Int) : EnrichState
    data class Completed(val enrichedCount: Int) : EnrichState
    data class Error(val message: String) : EnrichState
}

class MetadataEnricher(
    private val context: Context,
    private val database: AppDatabase,
    private val aiService: AiMetadataService,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _enrichState = MutableStateFlow<EnrichState>(EnrichState.Idle)
    val enrichState: StateFlow<EnrichState> = _enrichState.asStateFlow()

    fun dismissState() {
        _enrichState.value = EnrichState.Idle
    }

    fun enrichSeriesAsync(seriesIds: List<Long>) {
        if (!settingsRepository.autoEnrich.value) return
        scope.launch {
            enrichSeriesInternal(seriesIds)
        }
    }

    fun enrichAllMissingAsync(force: Boolean = false) {
        scope.launch {
            if (!canUseNetwork()) {
                val msg = if (settingsRepository.syncOnlyWifi.value) {
                    "Memerlukan koneksi Wi-Fi (sesuai pengaturan). Matikan opsi 'Hanya Sync Lewat Wi-Fi' jika menggunakan kuota seluler."
                } else {
                    "Tidak ada koneksi internet."
                }
                _enrichState.value = EnrichState.Error(msg)
                return@launch
            }

            val seriesDao = database.seriesDao()
            val allCandidates = if (force) seriesDao.getAllSeries() else seriesDao.getSeriesNeedingEnrichment()
            // Filter out camera recordings or internal timestamp names
            val candidates = allCandidates.filter { isLikelyMediaTitle(it.displayTitle) }

            if (candidates.isEmpty()) {
                _enrichState.value = EnrichState.Completed(0)
                return@launch
            }

            Log.d("SmartPlayer", "Starting enrichment for ${candidates.size} series candidates")
            var enriched = 0
            for ((index, series) in candidates.withIndex()) {
                _enrichState.value = EnrichState.Progress(
                    currentTitle = series.displayTitle,
                    processed = index + 1,
                    total = candidates.size
                )

                val result = aiService.enrich(series.displayTitle, series.seasonNumber)
                result.onSuccess { meta ->
                    val genresStr = if (meta.genres.isNotEmpty()) meta.genres.joinToString(", ") else null
                    seriesDao.updateMetadata(
                        seriesId = series.id,
                        poster = meta.posterUrl ?: series.posterUrl,
                        backdrop = meta.backdropUrl ?: series.backdropUrl,
                        synopsis = if (meta.synopsis.isNotBlank()) meta.synopsis else series.synopsis,
                        genres = genresStr ?: series.genres,
                        totalEps = meta.totalSeasonEpisodes ?: series.totalSeasonEpisodes
                    )
                    enriched++
                }.onFailure { err ->
                    Log.w("SmartPlayer", "Failed enriching '${series.displayTitle}': ${err.message}")
                }
            }

            _enrichState.value = EnrichState.Completed(enriched)
        }
    }

    private suspend fun enrichSeriesInternal(seriesIds: List<Long>) {
        if (!canUseNetwork()) return

        val seriesDao = database.seriesDao()
        for (id in seriesIds) {
            val series = seriesDao.findById(id) ?: continue
            if (!isLikelyMediaTitle(series.displayTitle)) continue
            if (!series.synopsis.isNullOrBlank() && !series.posterUrl.isNullOrBlank() && series.posterUrl.contains("static.tvmaze.com")) continue

            val result = aiService.enrich(series.displayTitle, series.seasonNumber)
            result.onSuccess { meta ->
                val genresStr = if (meta.genres.isNotEmpty()) meta.genres.joinToString(", ") else null
                seriesDao.updateMetadata(
                    seriesId = id,
                    poster = meta.posterUrl ?: series.posterUrl,
                    backdrop = meta.backdropUrl ?: series.backdropUrl,
                    synopsis = if (meta.synopsis.isNotBlank()) meta.synopsis else series.synopsis,
                    genres = genresStr ?: series.genres,
                    totalEps = meta.totalSeasonEpisodes ?: series.totalSeasonEpisodes
                )
            }
        }
    }

    private fun isLikelyMediaTitle(title: String): Boolean {
        // Exclude raw phone camera clips or WhatsApp temp videos
        if (title.matches(Regex("""^(?:20\d{6}_\d{6}|WA\s?\d+|\d{8}\s\d{6})$"""))) {
            return false
        }
        return true
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
