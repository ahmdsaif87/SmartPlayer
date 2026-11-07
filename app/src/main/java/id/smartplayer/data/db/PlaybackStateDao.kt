package id.smartplayer.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.smartplayer.data.db.entities.PlaybackStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackStateDao {

    @Upsert
    suspend fun save(state: PlaybackStateEntity)

    @Query("SELECT * FROM playback_state WHERE fingerprint = :fp LIMIT 1")
    suspend fun get(fp: String): PlaybackStateEntity?

    @Query("SELECT * FROM playback_state WHERE fingerprint = :fp LIMIT 1")
    fun observe(fp: String): Flow<PlaybackStateEntity?>

    @Query("DELETE FROM playback_state WHERE updatedAtEpochMs < :cutoff AND fingerprint NOT IN (SELECT fingerprint FROM episodes)")
    suspend fun purgeStale(cutoff: Long)

    @Query("DELETE FROM playback_state WHERE fingerprint NOT IN (SELECT fingerprint FROM episodes)")
    suspend fun purgeOrphans()

    @Query("DELETE FROM playback_state WHERE fingerprint IN (SELECT fingerprint FROM episodes WHERE id = :episodeId)")
    suspend fun deleteByEpisodeId(episodeId: Long)

    @Query("DELETE FROM playback_state")
    suspend fun clearAll()
}
