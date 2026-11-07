package id.smartplayer.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.smartplayer.data.db.entities.EpisodeEntity
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.db.entities.EpisodeSnapshot
import id.smartplayer.data.db.entities.HistoryItemRow
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {

    @Query("""
        SELECT e.*, p.lastPlayedPosition AS resumePositionMs, COALESCE(p.isFinished, 0) AS isFinished
        FROM episodes e 
        LEFT JOIN playback_state p ON p.fingerprint = e.fingerprint
        WHERE e.seriesId = :seriesId
        ORDER BY (e.episodeNumber IS NULL) ASC, e.episodeNumber ASC, e.episodeVersion ASC, e.displayName COLLATE NOCASE ASC
    """)
    fun observeEpisodes(seriesId: Long): Flow<List<EpisodeRow>>

    @Query("""
        SELECT e.*, p.lastPlayedPosition AS resumePositionMs, COALESCE(p.isFinished, 0) AS isFinished
        FROM episodes e 
        LEFT JOIN playback_state p ON p.fingerprint = e.fingerprint
        WHERE e.seriesId = :seriesId
        ORDER BY (e.episodeNumber IS NULL) ASC, e.episodeNumber ASC, e.episodeVersion ASC, e.displayName COLLATE NOCASE ASC
    """)
    suspend fun getEpisodes(seriesId: Long): List<EpisodeRow>

    @Query("SELECT mediaStoreId, dateModifiedSec, sizeBytes FROM episodes")
    suspend fun snapshots(): List<EpisodeSnapshot>

    @Upsert
    suspend fun upsertAll(items: List<EpisodeEntity>)

    @Query("DELETE FROM episodes WHERE mediaStoreId IN (:ids)")
    suspend fun deleteByMediaStoreIds(ids: List<Long>)

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE fingerprint = :fp LIMIT 1")
    suspend fun findByFingerprint(fp: String): EpisodeEntity?

    @Query("""
        SELECT e.*, p.lastPlayedPosition AS resumePositionMs, COALESCE(p.isFinished, 0) AS isFinished
        FROM episodes e
        JOIN playback_state p ON p.fingerprint = e.fingerprint
        WHERE p.isFinished = 0 AND p.lastPlayedPosition > 5000
        ORDER BY p.updatedAtEpochMs DESC
        LIMIT 20
    """)
    fun observeContinueWatching(): Flow<List<EpisodeRow>>

    @Query("""
        SELECT 
            e.id AS episodeId,
            e.seriesId AS seriesId,
            s.displayTitle AS seriesTitle,
            e.mediaStoreId AS mediaStoreId,
            e.displayName AS displayName,
            e.episodeNumber AS episodeNumber,
            e.seasonNumber AS seasonNumber,
            e.durationMs AS durationMs,
            p.lastPlayedPosition AS resumePositionMs,
            COALESCE(p.isFinished, 0) AS isFinished,
            p.updatedAtEpochMs AS lastPlayedEpochMs,
            s.posterUrl AS posterUrl
        FROM episodes e
        JOIN playback_state p ON p.fingerprint = e.fingerprint
        JOIN series s ON s.id = e.seriesId
        WHERE p.isFinished = 1 OR p.lastPlayedPosition >= 5000
        ORDER BY p.updatedAtEpochMs DESC
        LIMIT 50
    """)
    fun observePlaybackHistory(): Flow<List<HistoryItemRow>>
}
