package id.smartplayer.data.db.entities

data class SeriesSummary(
    val id: Long,
    val displayTitle: String,
    val seasonNumber: Int,
    val coverMediaStoreId: Long?,
    val lastUpdatedSec: Long,
    val posterUrl: String?,
    val backdropUrl: String?,
    val synopsis: String?,
    val genres: String?,
    val totalSeasonEpisodes: Int?,
    val isMovie: Boolean,
    val episodeCount: Int,
    val finishedCount: Int
) {
    val isFinished: Boolean
        get() = episodeCount > 0 && finishedCount >= episodeCount

    val hasStarted: Boolean
        get() = finishedCount > 0
}

data class EpisodeRow(
    val id: Long,
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
    val episodeTitle: String?,
    val synopsis: String?,
    val resumePositionMs: Long?,
    val isFinished: Boolean
)

data class EpisodeSnapshot(
    val mediaStoreId: Long,
    val dateModifiedSec: Long,
    val sizeBytes: Long
)

data class HistoryItemRow(
    val episodeId: Long,
    val seriesId: Long,
    val seriesTitle: String,
    val mediaStoreId: Long,
    val displayName: String,
    val episodeNumber: Int?,
    val seasonNumber: Int,
    val durationMs: Long,
    val resumePositionMs: Long?,
    val isFinished: Boolean,
    val lastPlayedEpochMs: Long,
    val posterUrl: String?
)
