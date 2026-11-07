package id.smartplayer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.smartplayer.data.db.entities.SeriesEntity
import id.smartplayer.data.db.entities.SeriesSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {

    @Query("""
        SELECT s.id, s.displayTitle, s.seasonNumber, s.coverMediaStoreId, s.lastUpdatedSec,
               s.posterUrl, s.backdropUrl, s.synopsis, s.genres, s.totalSeasonEpisodes, s.isMovie,
               COUNT(e.id) AS episodeCount,
               COALESCE(SUM(CASE WHEN p.isFinished = 1 THEN 1 ELSE 0 END), 0) AS finishedCount
        FROM series s
        JOIN episodes e ON e.seriesId = s.id
        LEFT JOIN playback_state p ON p.fingerprint = e.fingerprint
        GROUP BY s.id
        ORDER BY s.lastUpdatedSec DESC
    """)
    fun observeDashboard(): Flow<List<SeriesSummary>>

    @Query("SELECT * FROM series WHERE seriesKey = :key LIMIT 1")
    suspend fun findByKey(key: String): SeriesEntity?

    @Query("SELECT * FROM series WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): SeriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(series: SeriesEntity): Long

    @Update
    suspend fun update(series: SeriesEntity)

    @Query("""
        UPDATE series 
        SET posterUrl = :poster, 
            backdropUrl = :backdrop, 
            synopsis = :synopsis, 
            genres = :genres, 
            totalSeasonEpisodes = :totalEps 
        WHERE id = :seriesId
    """)
    suspend fun updateMetadata(
        seriesId: Long,
        poster: String?,
        backdrop: String?,
        synopsis: String?,
        genres: String?,
        totalEps: Int?
    )

    @Query("UPDATE series SET lastUpdatedSec = :lastUpdatedSec, coverMediaStoreId = :coverId WHERE id = :seriesId")
    suspend fun updateTimestamps(seriesId: Long, lastUpdatedSec: Long, coverId: Long?)

    @Query("SELECT * FROM series WHERE synopsis IS NULL OR posterUrl IS NULL")
    suspend fun getSeriesNeedingEnrichment(): List<SeriesEntity>

    @Query("SELECT * FROM series")
    suspend fun getAllSeries(): List<SeriesEntity>

    @Query("DELETE FROM series WHERE id NOT IN (SELECT DISTINCT seriesId FROM episodes)")
    suspend fun deleteOrphans()

    @Query("DELETE FROM series WHERE id = :id")
    suspend fun deleteById(id: Long)
}
