package id.smartplayer.data.repo

import id.smartplayer.data.db.AppDatabase
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.db.entities.HistoryItemRow
import id.smartplayer.data.db.entities.SeriesEntity
import id.smartplayer.data.db.entities.SeriesSummary
import id.smartplayer.data.scan.ScanCoordinator
import id.smartplayer.data.scan.ScanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class LibraryRepository(
    private val database: AppDatabase,
    private val scanCoordinator: ScanCoordinator
) {
    val scanState: StateFlow<ScanState> = scanCoordinator.scanState

    fun observeDashboard(): Flow<List<SeriesSummary>> =
        database.seriesDao().observeDashboard()

    fun observeContinueWatching(): Flow<List<EpisodeRow>> =
        database.episodeDao().observeContinueWatching()

    fun observePlaybackHistory(): Flow<List<HistoryItemRow>> =
        database.episodeDao().observePlaybackHistory()

    fun observeEpisodes(seriesId: Long): Flow<List<EpisodeRow>> =
        database.episodeDao().observeEpisodes(seriesId)

    suspend fun getSeriesById(seriesId: Long): SeriesEntity? =
        database.seriesDao().findById(seriesId)

    suspend fun getEpisodes(seriesId: Long): List<EpisodeRow> =
        database.episodeDao().getEpisodes(seriesId)

    suspend fun deleteEpisodesByMediaStoreIds(mediaStoreIds: List<Long>) {
        if (mediaStoreIds.isEmpty()) return
        database.episodeDao().deleteByMediaStoreIds(mediaStoreIds)
        database.playbackStateDao().purgeOrphans()
        database.seriesDao().deleteOrphans()
    }

    suspend fun deleteSeries(seriesId: Long) {
        val episodes = database.episodeDao().getEpisodes(seriesId)
        val ids = episodes.map { it.mediaStoreId }
        database.episodeDao().deleteByMediaStoreIds(ids)
        database.playbackStateDao().purgeOrphans()
        database.seriesDao().deleteById(seriesId)
    }

    fun requestScan() = scanCoordinator.requestScan()

    fun dismissScanState() = scanCoordinator.dismissScanState()
}
