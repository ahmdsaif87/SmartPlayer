package id.smartplayer.data.repo

import id.smartplayer.data.db.PlaybackStateDao
import id.smartplayer.data.db.entities.PlaybackStateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlaybackRepository(
    private val playbackStateDao: PlaybackStateDao
) {
    suspend fun saveProgress(
        fingerprint: String,
        positionMs: Long,
        durationMs: Long
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // §3.3.5 Finished Semantics
        val isFinished = (durationMs > 0 && positionMs >= 0.95 * durationMs) ||
                (durationMs > 90_000 && durationMs - positionMs <= 90_000)

        // Don't save false history if video was barely opened (< 5s) and not finished
        if (!isFinished && positionMs < 5000L) {
            return@withContext
        }

        val persistedPosition = when {
            isFinished -> 0L
            else -> positionMs
        }

        val state = PlaybackStateEntity(
            fingerprint = fingerprint,
            lastPlayedPosition = persistedPosition,
            durationMs = durationMs,
            isFinished = isFinished,
            updatedAtEpochMs = now
        )

        playbackStateDao.save(state)
    }

    suspend fun deleteHistoryByEpisodeId(episodeId: Long) = withContext(Dispatchers.IO) {
        playbackStateDao.deleteByEpisodeId(episodeId)
    }

    suspend fun getProgress(fingerprint: String): PlaybackStateEntity? =
        withContext(Dispatchers.IO) {
            playbackStateDao.get(fingerprint)
        }

    fun observeProgress(fingerprint: String): Flow<PlaybackStateEntity?> =
        playbackStateDao.observe(fingerprint)

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        playbackStateDao.clearAll()
    }
}
