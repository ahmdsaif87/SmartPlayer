package id.smartplayer.data.scan

import android.util.Log
import androidx.room.withTransaction
import id.smartplayer.data.common.FingerprintHelper
import id.smartplayer.data.db.AppDatabase
import id.smartplayer.data.db.entities.EpisodeEntity
import id.smartplayer.data.db.entities.SeriesEntity
import id.smartplayer.data.parse.DefaultFilenameParser
import id.smartplayer.data.parse.FilenameParser
import id.smartplayer.data.parse.SeriesKeyFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class ScanCoordinator(
    private val scanner: MediaStoreScanner,
    private val database: AppDatabase,
    private val parser: FilenameParser = DefaultFilenameParser(),
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val mutex = Mutex()
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private var scanJob: Job? = null

    // Callback invoked when new series are added so AI metadata enricher can process them
    var onNewSeriesDiscovered: ((List<Long>) -> Unit)? = null
    var onScanCompleted: (() -> Unit)? = null

    fun requestScan(): Job {
        scanJob?.cancel()
        val job = externalScope.launch {
            mutex.withLock {
                runScan()
            }
        }
        scanJob = job
        return job
    }

    private suspend fun runScan() = withContext(Dispatchers.IO) {
        try {
            val rawFiles = scanner.scan()
            val total = rawFiles.size
            _scanState.value = ScanState.Scanning(0, total)

            val seriesDao = database.seriesDao()
            val episodeDao = database.episodeDao()

            val existingSnapshots = episodeDao.snapshots().associateBy { it.mediaStoreId }
            val seenIds = mutableSetOf<Long>()
            val newlyCreatedSeriesIds = mutableListOf<Long>()

            val episodeUpsertBatch = mutableListOf<EpisodeEntity>()
            var addedCount = 0
            var updatedCount = 0

            val seriesKeyCache = mutableMapOf<String, SeriesEntity>()

            rawFiles.forEachIndexed { index, file ->
                seenIds.add(file.mediaStoreId)
                val existing = existingSnapshots[file.mediaStoreId]

                val needsParse = existing == null ||
                        existing.dateModifiedSec != file.dateModifiedSec ||
                        existing.sizeBytes != file.sizeBytes

                if (needsParse) {
                    val parentFolder = if (file.absolutePath.isNotEmpty()) {
                        File(file.absolutePath).parentFile?.name
                    } else null

                    val parsed = parser.parse(file.displayName, parentFolder)
                    val sKey = SeriesKeyFactory.key(parsed.coreTitle, parsed.seasonNumber)

                    // Get or create series
                    var series = seriesKeyCache[sKey] ?: seriesDao.findByKey(sKey)
                    if (series == null) {
                        val newSeries = SeriesEntity(
                            seriesKey = sKey,
                            displayTitle = parsed.coreTitle,
                            seasonNumber = parsed.seasonNumber,
                            coverMediaStoreId = file.mediaStoreId,
                            lastUpdatedSec = file.dateAddedSec,
                            isMovie = parsed.episodeNumber == null && (parsed.year != null || file.displayName.contains("movie", ignoreCase = true))
                        )
                        val insertedId = seriesDao.insert(newSeries)
                        series = newSeries.copy(id = insertedId)
                        seriesKeyCache[sKey] = series
                        newlyCreatedSeriesIds.add(insertedId)
                    }

                    val fingerprint = FingerprintHelper.generate(file.displayName, file.sizeBytes)

                    val episode = EpisodeEntity(
                        seriesId = series.id,
                        mediaStoreId = file.mediaStoreId,
                        contentUri = file.contentUri,
                        absolutePath = file.absolutePath,
                        relativePath = file.relativePath,
                        displayName = file.displayName,
                        sizeBytes = file.sizeBytes,
                        durationMs = file.durationMs,
                        dateAddedSec = file.dateAddedSec,
                        dateModifiedSec = file.dateModifiedSec,
                        episodeNumber = parsed.episodeNumber,
                        episodeVersion = parsed.episodeVersion,
                        seasonNumber = parsed.seasonNumber,
                        releaseGroup = parsed.releaseGroup,
                        resolution = parsed.resolution,
                        parseConfidence = parsed.confidence.name,
                        fingerprint = fingerprint
                    )

                    episodeUpsertBatch.add(episode)

                    if (existing == null) {
                        addedCount++
                    } else {
                        updatedCount++
                    }

                    if (episodeUpsertBatch.size >= 200) {
                        database.withTransaction {
                            episodeDao.upsertAll(episodeUpsertBatch)
                        }
                        episodeUpsertBatch.clear()
                    }
                }

                if (index % 50 == 0 || index == total - 1) {
                    _scanState.value = ScanState.Scanning(index + 1, total)
                }
            }

            // Flush remaining batch
            if (episodeUpsertBatch.isNotEmpty()) {
                database.withTransaction {
                    episodeDao.upsertAll(episodeUpsertBatch)
                }
                episodeUpsertBatch.clear()
            }

            // Deletions: any existing id not in seenIds
            val removedIds = existingSnapshots.keys.filter { it !in seenIds }
            if (removedIds.isNotEmpty()) {
                removedIds.chunked(500).forEach { chunk ->
                    database.withTransaction {
                        episodeDao.deleteByMediaStoreIds(chunk)
                    }
                }
            }

            // Clean orphan series
            database.withTransaction {
                seriesDao.deleteOrphans()
            }

            Log.d("SmartPlayer", "Scan finished: added=$addedCount, updated=$updatedCount, removed=${removedIds.size}")
            _scanState.value = ScanState.Done(added = addedCount, updated = updatedCount, removed = removedIds.size)

            if (newlyCreatedSeriesIds.isNotEmpty()) {
                onNewSeriesDiscovered?.invoke(newlyCreatedSeriesIds)
            }
            onScanCompleted?.invoke()
        } catch (t: Throwable) {
            Log.e("SmartPlayer", "Scan failed with error", t)
            _scanState.value = ScanState.Error(t)
        }
    }
}
