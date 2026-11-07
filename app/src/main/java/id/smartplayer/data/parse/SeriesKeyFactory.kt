package id.smartplayer.data.parse

import java.text.Normalizer
import java.util.Locale

object SeriesKeyFactory {

    private val NON_ALPHANUMERIC_REGEX = Regex("""[^\p{L}\p{N}]+""")
    private val WHITESPACE_REGEX = Regex("""\s+""")

    /**
     * Generates a canonical, deterministic identity key for grouping files into the same series/season.
     * Two files belong to the same series iff their keys are byte-equal.
     *
     * Example: "Re:Zero kara Hajimeru Isekai Seikatsu", season 2 -> "re zero kara hajimeru isekai seikatsu|s2"
     */
    fun key(coreTitle: String, season: Int = 1): String {
        val nfkc = Normalizer.normalize(coreTitle, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
        val sanitized = NON_ALPHANUMERIC_REGEX.replace(nfkc, " ")
        val collapsed = WHITESPACE_REGEX.replace(sanitized, " ").trim()
        return "$collapsed|s$season"
    }
}
