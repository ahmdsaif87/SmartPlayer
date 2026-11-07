package id.smartplayer.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.smartplayer.data.db.entities.EpisodeEntity
import id.smartplayer.data.db.entities.EpisodeRow
import id.smartplayer.data.db.entities.EpisodeSnapshot
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
}
