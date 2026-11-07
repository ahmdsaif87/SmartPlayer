package id.smartplayer.data.parse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FilenameParserTest {

    private lateinit var parser: FilenameParser

    @BeforeEach
    fun setUp() {
        parser = DefaultFilenameParser()
    }

    @Test
    fun `test example 1 - Boku no Hero Academia S6`() {
        val result = parser.parse("[Oploverz] Boku no Hero Academia S6 - 05 [720p].mp4")
        assertEquals("Boku no Hero Academia", result.coreTitle)
        assertEquals(6, result.seasonNumber)
        assertEquals(5, result.episodeNumber)
        assertEquals("Oploverz", result.releaseGroup)
        assertEquals("720p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 2 - Kimetsu no Yaiba - Yuukaku-hen`() {
        val result = parser.parse("[Kusonime] Kimetsu no Yaiba - Yuukaku-hen - 07 [BD][1080p][HEVC 10bit].mkv")
        assertEquals("Kimetsu no Yaiba - Yuukaku-hen", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(7, result.episodeNumber)
        assertEquals("Kusonime", result.releaseGroup)
        assertEquals("1080p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 3 - Spy x Family with CRC32`() {
        val result = parser.parse("[SubsPlease] Spy x Family - 12 (1080p) [A1B2C3D4].mkv")
        assertEquals("Spy x Family", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(12, result.episodeNumber)
        assertEquals("SubsPlease", result.releaseGroup)
        assertEquals("1080p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 4 - Underscore delimiters Sousou no Frieren`() {
        val result = parser.parse("Sousou_no_Frieren_-_03_[720p].mkv")
        assertEquals("Sousou no Frieren", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(3, result.episodeNumber)
        assertEquals("720p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 5 - Dot delimited One Piece`() {
        val result = parser.parse("One.Piece.S01E1089.1080p.WEB-DL.x264.mkv")
        assertEquals("One Piece", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(1089, result.episodeNumber)
        assertEquals("1080p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 6 - ReZero with S02E05v2`() {
        val result = parser.parse("[Anime Time] Re:Zero kara Hajimeru Isekai Seikatsu S02E05v2 [BD 1080p][HEVC 10bit x265][Dual Audio].mkv")
        assertEquals("Re:Zero kara Hajimeru Isekai Seikatsu", result.coreTitle)
        assertEquals(2, result.seasonNumber)
        assertEquals(5, result.episodeNumber)
        assertEquals(2, result.episodeVersion)
        assertEquals("Anime Time", result.releaseGroup)
        assertEquals("1080p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 7 - Naruto Shippuden Episode token and watermark suffix`() {
        val result = parser.parse("Naruto Shippuden Episode 123 Sub Indo 480p - Samehadaku.mp4")
        assertEquals("Naruto Shippuden", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(123, result.episodeNumber)
        assertEquals("480p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 8 - Mob Psycho 100 III with roman numeral`() {
        val result = parser.parse("[Kusonime] Mob Psycho 100 III - 04 [720p].mkv")
        assertEquals("Mob Psycho 100 III", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(4, result.episodeNumber)
        assertEquals("Kusonime", result.releaseGroup)
        assertEquals("720p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 9 - Dr Stone dot in title`() {
        val result = parser.parse("[Oploverz] Dr. Stone - New World - 11 (WEBRip 1080p).mp4")
        assertEquals("Dr. Stone - New World", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(11, result.episodeNumber)
        assertEquals("Oploverz", result.releaseGroup)
        assertEquals("1080p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 10 - Tonikaku Kawaii 2nd Season trailing number`() {
        val result = parser.parse("Tonikaku Kawaii 2nd Season 08.mkv")
        assertEquals("Tonikaku Kawaii", result.coreTitle)
        assertEquals(2, result.seasonNumber)
        assertEquals(8, result.episodeNumber)
        assertEquals(Confidence.MEDIUM, result.confidence)
    }

    @Test
    fun `test example 11 - Kaiju No 8 with number in title`() {
        val result = parser.parse("[Erai-raws] Kaiju No. 8 - 03 [1080p][Multiple Subtitle].mkv")
        assertEquals("Kaiju No. 8", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(3, result.episodeNumber)
        assertEquals("Erai-raws", result.releaseGroup)
        assertEquals("1080p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    @Test
    fun `test example 12 - Your Name standalone movie with year`() {
        val result = parser.parse("Your Name (2016) 1080p BluRay.mkv")
        assertEquals("Your Name", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertNull(result.episodeNumber)
        assertEquals(2016, result.year)
        assertEquals("1080p", result.resolution)
        assertEquals(Confidence.LOW, result.confidence)
    }

    @Test
    fun `test example 13 - Folder fallback`() {
        val result = parser.parse("Episode 05.mkv", parentFolderName = "Frieren")
        assertEquals("Frieren", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(5, result.episodeNumber)
        assertEquals(Confidence.MEDIUM, result.confidence)
    }

    @Test
    fun `test example 14 - Meownime domain watermark suffix`() {
        val result = parser.parse("[Meownime] Oshi no Ko - 09 [x265 10bit][480p] - Meownime.com.mkv")
        assertEquals("Oshi no Ko", result.coreTitle)
        assertEquals(1, result.seasonNumber)
        assertEquals(9, result.episodeNumber)
        assertEquals("Meownime", result.releaseGroup)
        assertEquals("480p", result.resolution)
        assertEquals(Confidence.HIGH, result.confidence)
    }

    // --- Section 6.1 Adversarial & Negative Cases ---

    @Test
    fun `test adversarial - Final Fantasy VII Advent Children (Final not stripped)`() {
        val result = parser.parse("Final Fantasy VII Advent Children.mkv")
        assertEquals("Final Fantasy VII Advent Children", result.coreTitle)
        assertNull(result.episodeNumber)
        assertEquals(Confidence.LOW, result.confidence)
    }

    @Test
    fun `test adversarial - 86 Eighty Six number in title`() {
        val result = parser.parse("[Group] 86 - Eighty Six - 11 [1080p].mkv")
        assertEquals("86 - Eighty Six", result.coreTitle)
        assertEquals(11, result.episodeNumber)
        assertEquals("Group", result.releaseGroup)
    }

    @Test
    fun `test adversarial - Mob Psycho 100 number in title`() {
        val result = parser.parse("[Group] Mob Psycho 100 - 12 [720p].mkv")
        assertEquals("Mob Psycho 100", result.coreTitle)
        assertEquals(12, result.episodeNumber)
    }

    @Test
    fun `test adversarial - Anime 2024 (year guard)`() {
        val result = parser.parse("Anime 2024 1080p WEB-DL.mkv")
        assertNull(result.episodeNumber)
        assertEquals(2024, result.year)
    }

    @Test
    fun `test adversarial - 1920x1080 resolution guard for E2`() {
        val result = parser.parse("[Group] Title - 1920x1080 - 05.mkv")
        assertEquals("Title", result.coreTitle)
        assertEquals(5, result.episodeNumber)
        assertEquals("1080p", result.resolution)
    }

    @Test
    fun `test adversarial - No spaces in filename`() {
        val result = parser.parse("[Group]Title-07[480p].mp4")
        assertEquals("Title", result.coreTitle)
        assertEquals(7, result.episodeNumber)
    }

    @Test
    fun `test adversarial - Japanese Unicode title`() {
        val result = parser.parse("日本語タイトル - 05 [1080p].mkv")
        assertEquals("日本語タイトル", result.coreTitle)
        assertEquals(5, result.episodeNumber)
    }

    @Test
    fun `test SeriesKeyFactory invariant`() {
        val key1 = SeriesKeyFactory.key("Sousou no Frieren", 1)
        val key2 = SeriesKeyFactory.key("Sousou_no_Frieren", 1)
        val key3 = SeriesKeyFactory.key("sousou  no   frieren", 1)
        assertEquals(key1, key2)
        assertEquals(key1, key3)
        assertEquals("sousou no frieren|s1", key1)
    }
}
