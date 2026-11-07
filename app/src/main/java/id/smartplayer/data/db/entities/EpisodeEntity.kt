package id.smartplayer.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["mediaStoreId"], unique = true),
        Index("absolutePath"),
        Index(value = ["seriesId", "episodeNumber"]),
        Index("fingerprint")
    ]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val mediaStoreId: Long,
    val contentUri: String,
    val absolutePath: String,
    val relativePath: String,
    val displayName: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val dateAddedSec: Long,
    val dateModifiedSec: Long,
    val episodeNumber: Int?,
    val episodeVersion: Int?,
    val seasonNumber: Int,
    val releaseGroup: String?,
    val resolution: String?,
    val parseConfidence: String,
    val fingerprint: String,
    val episodeTitle: String? = null,
    val synopsis: String? = null
)
