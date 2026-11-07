package id.smartplayer.data.ai

data class EnrichedMetadata(
    val canonicalTitle: String,
    val synopsis: String,
    val genres: List<String>,
    val posterUrl: String?,
    val backdropUrl: String?,
    val totalSeasonEpisodes: Int?
)

interface AiMetadataService {
    suspend fun enrich(rawTitle: String, seasonNumber: Int): Result<EnrichedMetadata>
}
