package id.smartplayer.data.parse

import java.util.Locale

class NoiseDictionary(
    groupsInput: Collection<String> = DEFAULT_GROUPS,
    qualityTokensInput: Collection<String> = DEFAULT_QUALITY_TOKENS
) {
    val groups: Set<String> = groupsInput.map { it.lowercase(Locale.ROOT).trim() }.toSet()
    val qualityTokens: Set<String> = qualityTokensInput.map { it.lowercase(Locale.ROOT).trim() }.toSet()

    fun isKnownGroup(token: String): Boolean = groups.contains(token.lowercase(Locale.ROOT).trim())

    fun containsQuality(token: String): Boolean {
        val lower = token.lowercase(Locale.ROOT).trim()
        return qualityTokens.any { lower.contains(it) }
    }

    companion object {
        val DEFAULT_GROUPS = setOf(
            "kusonime", "oploverz", "samehadaku", "anoboy", "meownime",
            "otakudesu", "nimegami", "neonime", "awsubs", "kuramanime",
            "animeindo", "nontonanimeid", "animesail", "anichin",
            "erai-raws", "subsplease", "judas", "horriblesubs", "commie", "anime time"
        )

        val DEFAULT_QUALITY_TOKENS = setOf(
            "2160p", "1440p", "1080p", "720p", "576p", "480p", "360p", "4k",
            "hevc", "x264", "x265", "h264", "h265", "10bit", "8bit",
            "bd", "bdrip", "bluray", "web-dl", "webrip", "hdtv", "aac", "flac",
            "dual audio", "multiple subtitle"
        )
    }
}
