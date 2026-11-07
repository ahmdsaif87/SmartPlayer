package id.smartplayer.data.parse

enum class Confidence {
    HIGH,
    MEDIUM,
    LOW
}

data class ParsedMedia(
    val coreTitle: String,
    val seasonNumber: Int = 1,
    val episodeNumber: Int? = null,
    val episodeVersion: Int? = null,
    val releaseGroup: String? = null,
    val resolution: String? = null,
    val year: Int? = null,
    val confidence: Confidence = Confidence.LOW
)
