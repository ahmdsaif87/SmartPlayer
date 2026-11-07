package id.smartplayer.data.parse

import java.text.Normalizer
import java.util.Locale

interface FilenameParser {
    fun parse(displayName: String, parentFolderName: String? = null): ParsedMedia
}

class DefaultFilenameParser(
    private val dictionary: NoiseDictionary = NoiseDictionary()
) : FilenameParser {

    companion object {
        // Step 0: Extension strip
        private val EXT_REGEX = Regex("""\.(mkv|mp4|webm)$""", RegexOption.IGNORE_CASE)

        // Step 3: Bracket extraction
        private val BRACKET_REGEX = Regex("""[\[\(\{]([^\]\)\}]*)[\]\)\}]""")
        private val CRC32_REGEX = Regex("""^[0-9A-Fa-f]{8}$""")
        private val YEAR_REGEX = Regex("""^(19|20)\d{2}$""")
        private val BRACKET_EPISODE_REGEX = Regex("""^(\d{1,4})(?:v(\d))?$""", RegexOption.IGNORE_CASE)

        // Resolution tokens
        private val RESOLUTION_TOKEN_REGEX = Regex("""\b(?:mkv|mp4|webm)?((?:2160|1440|1080|720|576|480|360)[pi]|4k)\b""", RegexOption.IGNORE_CASE)
        private val WXH_RESOLUTION_REGEX = Regex("""\b\d{3,4}x\d{3,4}\b""", RegexOption.IGNORE_CASE)

        // Step 4: Domain / watermark strip
        private val DOMAIN_REGEX = Regex("""(?i)(?:www\.)?[a-z0-9-]+\.(?:com|net|org|id|co|me|moe|info|my\.id|io|tv|xyz|vip|cc)\b""")

        // Step 6: Episode Extraction Rules
        // E1: S01E05v2 or S1 - E5
        private val E1_REGEX = Regex("""\bS(\d{1,2})\s?[-._ ]?E(\d{1,4})(?:v(\d))?\b""", RegexOption.IGNORE_CASE)
        // E2: 2x05
        private val E2_REGEX = Regex("""\b(\d{1,2})x(\d{2,3})\b""", RegexOption.IGNORE_CASE)
        // E3: Episode 12, Eps 07, Ep.3
        private val E3_REGEX = Regex("""\b(?:episode|eps?\.?|ep\.?)\s*[-._]?\s*(\d{1,4})(?:v(\d))?\b""", RegexOption.IGNORE_CASE)
        // E4: Bare E05
        private val E4_REGEX = Regex("""\bE(\d{2,4})(?:v(\d))?\b""", RegexOption.IGNORE_CASE)
        // E5: fansub canonical " - 05" or "Title-07" (take the last match)
        private val E5_REGEX = Regex("""(?:\s-\s|-(?=\d{1,4}))(\d{1,4})(?:v(\d))?(?=\s|$)""", RegexOption.IGNORE_CASE)

        // Season Extraction Patterns
        private val SEASON_STANDALONE_REGEX = Regex("""\bS(\d{1,2})\b""", RegexOption.IGNORE_CASE)
        private val SEASON_WORD_REGEX = Regex("""\bseason\s*(\d{1,2})\b""", RegexOption.IGNORE_CASE)
        private val SEASON_ORDINAL_REGEX = Regex("""\b(\d{1,2})(?:st|nd|rd|th)\s+season\b""", RegexOption.IGNORE_CASE)

        // Step 8: Unambiguous noise patterns in head
        private val NOISE_VIDEO_CODEC = Regex("""\b(?:x\.?26[45]|h\.?26[45]|hevc|avc|av1|xvid)\b""", RegexOption.IGNORE_CASE)
        private val NOISE_BIT_DEPTH = Regex("""\b(?:10[\s-]?bits?|8[\s-]?bits?|hi10p?)\b""", RegexOption.IGNORE_CASE)
        private val NOISE_SOURCE = Regex("""\b(?:bd(?:rip)?|blu[\s-]?ray|bdmv|web[\s-]?(?:dl|rip)|hdtv|dvd(?:rip)?|tvrip)\b""", RegexOption.IGNORE_CASE)
        private val NOISE_AUDIO = Regex("""\b(?:aac(?:2\.0)?|flac|ac3|eac3|dts|opus|dual[\s-]?audio)\b""", RegexOption.IGNORE_CASE)
        private val NOISE_SUB = Regex("""\b(?:sub[\s-]?indo(?:nesia)?|indo[\s-]?sub|hardsub|softsub|esub|multi(?:ple)?[\s-]?subs?)\b""", RegexOption.IGNORE_CASE)
        private val NOISE_FLAGS = Regex("""\b(?:batch|uncensored|uncut|repack|proper)\b""", RegexOption.IGNORE_CASE)

        private val GENERIC_TITLES = setOf("episode", "video", "vid", "unknown")
        private val SKIP_FOLDERS = setOf("download", "downloads", "movies", "video", "videos", "anime", "dcim")
    }

    override fun parse(displayName: String, parentFolderName: String?): ParsedMedia {
        // Step 0 - Extension strip
        val rawWithoutExt = EXT_REGEX.replace(displayName.trim(), "")

        // Step 1 - Unicode normalization (NFKC)
        var s = Normalizer.normalize(rawWithoutExt, Normalizer.Form.NFKC)
            .replace('\u2013', '-')
            .replace('\u2014', '-')
            .replace('（', '(').replace('）', ')')
            .replace('［', '[').replace('］', ']')

        // Step 2 - Delimiter normalization
        s = s.replace('_', ' ')
        // Scene-style dots: if no spaces and >= 2 dots, replace dots with spaces
        if (!s.contains(' ') && s.count { it == '.' } >= 2) {
            s = s.replace('.', ' ')
        }

        var year: Int? = null
        var resolution: String? = null
        var releaseGroup: String? = null
        var bracketEpisodeCandidate: Pair<Int, Int?>? = null // (episode, version)
        var capturedSeasonFromBracket: Int? = null

        // Step 3 - Bracket extraction
        val originalWithBrackets = s
        val bracketMatches = BRACKET_REGEX.findAll(originalWithBrackets).toList()

        for (match in bracketMatches) {
            val inner = match.groupValues[1].trim()
            if (inner.isEmpty()) continue

            // 1: CRC32
            if (CRC32_REGEX.matches(inner)) {
                // discard
            }
            // 2: Year
            else if (YEAR_REGEX.matches(inner) && year == null) {
                year = inner.toIntOrNull()
            }
            // 3: Bracket episode candidate
            else if (BRACKET_EPISODE_REGEX.matches(inner) && bracketEpisodeCandidate == null) {
                val epMatch = BRACKET_EPISODE_REGEX.matchEntire(inner)!!
                val epNum = epMatch.groupValues[1].toIntOrNull()
                val epVer = epMatch.groupValues[2].takeIf { it.isNotEmpty() }?.toIntOrNull()
                if (epNum != null) {
                    bracketEpisodeCandidate = epNum to epVer
                }
            }
            // 4: Season info
            else if (checkSeasonFromText(inner) != null && capturedSeasonFromBracket == null) {
                capturedSeasonFromBracket = checkSeasonFromText(inner)
            }
            // 5: Quality / tech
            else if (dictionary.containsQuality(inner)) {
                if (resolution == null) {
                    resolution = extractResolutionFromText(inner)
                }
            }
            // 6: Known group / site
            else if (dictionary.isKnownGroup(inner) && releaseGroup == null) {
                releaseGroup = inner
            }
            // 7: Positional group at index 0
            else if (match.range.first == 0 && releaseGroup == null && !inner.all { it.isDigit() }) {
                releaseGroup = inner
            }
        }

        // Remove all bracketed chunks
        s = BRACKET_REGEX.replace(s, " ")

        // Step 4 - Domain / watermark strip
        s = DOMAIN_REGEX.replace(s, " ")
        for (group in dictionary.groups) {
            val trailingSiteRegex = Regex("""\s-\s*""" + Regex.escape(group) + """(?:\.[a-z]{2,4})?\s*$""", RegexOption.IGNORE_CASE)
            s = trailingSiteRegex.replace(s, "")
        }

        // Step 5 - Resolution extraction (unbracketed)
        if (resolution == null) {
            val resMatch = RESOLUTION_TOKEN_REGEX.find(s)
            if (resMatch != null) {
                resolution = (resMatch.groups[1]?.value ?: resMatch.value).lowercase(Locale.ROOT)
            }
        }
        // WxH patterns (e.g. 1920x1080) MUST be removed before episode rule E2 runs
        val wxhMatch = WXH_RESOLUTION_REGEX.find(s)
        if (wxhMatch != null) {
            if (resolution == null && wxhMatch.value.contains("1080")) {
                resolution = "1080p"
            } else if (resolution == null && wxhMatch.value.contains("720")) {
                resolution = "720p"
            }
            s = WXH_RESOLUTION_REGEX.replace(s, " ")
        }

        // Step 6 - Episode extraction (E1 to E7)
        var season: Int = capturedSeasonFromBracket ?: 1
        var episode: Int? = null
        var version: Int? = null
        var confidence = Confidence.LOW
        var tailBoundary = s.length

        // E1: SxxExx
        val e1 = E1_REGEX.find(s)
        if (e1 != null) {
            season = e1.groupValues[1].toInt()
            episode = e1.groupValues[2].toInt()
            version = e1.groupValues[3].takeIf { it.isNotEmpty() }?.toIntOrNull()
            confidence = Confidence.HIGH
            tailBoundary = e1.range.first
        }

        // E2: 2x05
        if (episode == null) {
            val e2 = E2_REGEX.find(s)
            if (e2 != null) {
                season = e2.groupValues[1].toInt()
                episode = e2.groupValues[2].toInt()
                confidence = Confidence.HIGH
                tailBoundary = e2.range.first
            }
        }

        // E3: Episode / Eps / Ep
        if (episode == null) {
            val e3 = E3_REGEX.find(s)
            if (e3 != null) {
                episode = e3.groupValues[1].toInt()
                version = e3.groupValues[2].takeIf { it.isNotEmpty() }?.toIntOrNull()
                confidence = Confidence.HIGH
                tailBoundary = e3.range.first
            }
        }

        // E4: Bare E05
        if (episode == null) {
            val e4 = E4_REGEX.find(s)
            if (e4 != null) {
                episode = e4.groupValues[1].toInt()
                version = e4.groupValues[2].takeIf { it.isNotEmpty() }?.toIntOrNull()
                confidence = Confidence.HIGH
                tailBoundary = e4.range.first
            }
        }

        // E5: Canonical " - 05" (take LAST match)
        if (episode == null) {
            val e5Matches = E5_REGEX.findAll(s).toList()
            if (e5Matches.isNotEmpty()) {
                val lastE5 = e5Matches.last()
                episode = lastE5.groupValues[1].toInt()
                version = lastE5.groupValues[2].takeIf { it.isNotEmpty() }?.toIntOrNull()
                confidence = Confidence.HIGH
                tailBoundary = lastE5.range.first
            }
        }

        // E6: Bracket episode candidate
        if (episode == null && bracketEpisodeCandidate != null) {
            episode = bracketEpisodeCandidate.first
            version = bracketEpisodeCandidate.second
            confidence = Confidence.MEDIUM
        }

        // Step 7 - Tail discard
        var head = if (tailBoundary < s.length) s.substring(0, tailBoundary) else s

        // E7: Trailing number on working string
        if (episode == null) {
            // Check year pattern in unbracketed string first
            val yearInHead = Regex("""\b((?:19|20)\d{2})\b""").find(head)
            if (yearInHead != null && year == null) {
                year = yearInHead.groupValues[1].toInt()
                head = head.removeRange(yearInHead.range)
            }

            val e7 = Regex("""(?<=\s)(\d{1,4})(?:v(\d))?\s*$""").find(head.trimEnd())
            if (e7 != null) {
                val num = e7.groupValues[1].toInt()
                val isYear = num in 1900..2099
                val prefix = head.substring(0, e7.range.first).trimEnd()
                val isRomanOrNo = prefix.endsWith("No.", ignoreCase = true) ||
                        prefix.matches(Regex(""".*\b(?:I|II|III|IV|V|VI|VII|VIII|IX|X)\s*$"""))

                if (!isYear && !isRomanOrNo) {
                    episode = num
                    version = e7.groupValues[2].takeIf { it.isNotEmpty() }?.toIntOrNull()
                    confidence = Confidence.MEDIUM
                    head = head.substring(0, e7.range.first)
                }
            }
        }

        // Step 8 - Noise token removal in head
        head = RESOLUTION_TOKEN_REGEX.replace(head, " ")
        head = NOISE_VIDEO_CODEC.replace(head, " ")
        head = NOISE_BIT_DEPTH.replace(head, " ")
        head = NOISE_SOURCE.replace(head, " ")
        head = NOISE_AUDIO.replace(head, " ")
        head = NOISE_SUB.replace(head, " ")
        head = NOISE_FLAGS.replace(head, " ")

        // Step 9 - Season extraction from head
        if (capturedSeasonFromBracket == null && season == 1) {
            val sOrdinal = SEASON_ORDINAL_REGEX.find(head)
            if (sOrdinal != null) {
                season = sOrdinal.groupValues[1].toInt()
                head = head.removeRange(sOrdinal.range)
            } else {
                val sWord = SEASON_WORD_REGEX.find(head)
                if (sWord != null) {
                    season = sWord.groupValues[1].toInt()
                    head = head.removeRange(sWord.range)
                } else {
                    val sStandalone = SEASON_STANDALONE_REGEX.find(head)
                    if (sStandalone != null) {
                        season = sStandalone.groupValues[1].toInt()
                        head = head.removeRange(sStandalone.range)
                    }
                }
            }
        }

        // Step 10 - Title cleanup
        var title = cleanTitle(head)

        // Step 11 - Folder fallback if title is empty or generic
        if (title.isEmpty() || GENERIC_TITLES.contains(title.lowercase(Locale.ROOT))) {
            if (!parentFolderName.isNullOrBlank()) {
                val folderClean = parentFolderName.trim()
                if (!SKIP_FOLDERS.contains(folderClean.lowercase(Locale.ROOT))) {
                    title = cleanTitle(folderClean)
                    confidence = Confidence.MEDIUM
                }
            }
        }

        if (title.isEmpty()) {
            title = "Unknown"
        }

        return ParsedMedia(
            coreTitle = title,
            seasonNumber = season,
            episodeNumber = episode,
            episodeVersion = version,
            releaseGroup = releaseGroup,
            resolution = resolution,
            year = year,
            confidence = confidence
        )
    }

    private fun checkSeasonFromText(text: String): Int? {
        val s1 = SEASON_ORDINAL_REGEX.find(text)
        if (s1 != null) return s1.groupValues[1].toIntOrNull()
        val s2 = SEASON_WORD_REGEX.find(text)
        if (s2 != null) return s2.groupValues[1].toIntOrNull()
        val s3 = SEASON_STANDALONE_REGEX.find(text)
        if (s3 != null) return s3.groupValues[1].toIntOrNull()
        return null
    }

    private fun extractResolutionFromText(text: String): String? {
        val resMatch = RESOLUTION_TOKEN_REGEX.find(text)
        return (resMatch?.groups?.get(1)?.value ?: resMatch?.value)?.lowercase(Locale.ROOT)
    }

    private fun cleanTitle(raw: String): String {
        var t = raw.replace(Regex("""\s+"""), " ").trim()
        // Trim leading and trailing punctuation like - _ : .
        t = t.trim(' ', '-', '_', ':', '.')
        return t
    }
}
