package id.smartplayer.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "series",
    indices = [
        Index(value = ["seriesKey"], unique = true),
        Index("lastUpdatedSec")
    ]
)
data class SeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesKey: String,
    val displayTitle: String,
    val seasonNumber: Int,
    val coverMediaStoreId: Long?,
    val lastUpdatedSec: Long,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val synopsis: String? = null,
    val genres: String? = null,
    val totalSeasonEpisodes: Int? = null,
    val isMovie: Boolean = false
)
