package id.smartplayer.data.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class GeminiAiClient(
    private val apiKeyProvider: () -> String
) : AiMetadataService {

    override suspend fun enrich(rawTitle: String, seasonNumber: Int): Result<EnrichedMetadata> =
        withContext(Dispatchers.IO) {
            val key = apiKeyProvider().trim()
            val cleanTitle = cleanMediaTitle(rawTitle)

            Log.d("SmartPlayer", "Enriching: raw='$rawTitle' -> clean='$cleanTitle' (Season $seasonNumber)")

            // 1. Try to fetch verified high-quality poster from TVMaze or Jikan
            var (tvMazePoster, tvMazeBackdrop) = fetchTvMaze(cleanTitle)
            var poster = tvMazePoster
            var backdrop = tvMazeBackdrop

            if (poster.isNullOrBlank()) {
                poster = fetchJikanPoster(cleanTitle)
            }

            // If no Gemini API key is configured, return what we have (title & verified poster)
            if (key.isBlank()) {
                Log.i("SmartPlayer", "Gemini API key is blank. Returning TVMaze/Jikan metadata for '$cleanTitle'")
                return@withContext Result.success(
                    EnrichedMetadata(
                        canonicalTitle = cleanTitle,
                        synopsis = "",
                        genres = emptyList(),
                        posterUrl = poster,
                        backdropUrl = backdrop,
                        totalSeasonEpisodes = null
                    )
                )
            }

            try {
                Log.d("SmartPlayer", "Calling Gemini 2.5 for synopsis & canonicalTitle: '$cleanTitle'")
                val model = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = key
                )

                val prompt = """
                    You are a media metadata assistant for SmartPlayer.
                    Given this series or movie title: "$cleanTitle" (Season $seasonNumber).
                    Return ONLY a valid JSON object (without markdown code blocks, just raw JSON) with these fields:
                    {
                      "canonicalTitle": "Official Clean Title",
                      "synopsis": "Engaging 2-3 sentence synopsis written in Bahasa Indonesia",
                      "genres": ["Genre1", "Genre2"],
                      "totalSeasonEpisodes": 12
                    }
                """.trimIndent()

                val response = model.generateContent(prompt)
                val text = response.text?.trim() ?: throw IllegalStateException("Empty response from Gemini")

                val jsonMatch = Regex("""\{[\s\S]*\}""").find(text)
                val jsonStr = jsonMatch?.value ?: text
                val json = JSONObject(jsonStr)

                val canonical = json.optString("canonicalTitle", cleanTitle)
                val synopsis = json.optString("synopsis", "")
                val totalEps = if (json.has("totalSeasonEpisodes") && !json.isNull("totalSeasonEpisodes")) {
                    json.optInt("totalSeasonEpisodes")
                } else null

                val genresList = mutableListOf<String>()
                val genresArray = json.optJSONArray("genres")
                if (genresArray != null) {
                    for (i in 0 until genresArray.length()) {
                        genresList.add(genresArray.getString(i))
                    }
                }

                // If poster wasn't found with the raw/clean title, try again using the Gemini canonical title
                if (poster.isNullOrBlank() && canonical.isNotBlank() && !canonical.equals(cleanTitle, ignoreCase = true)) {
                    val (canonTvMaze, canonBackdrop) = fetchTvMaze(canonical)
                    poster = canonTvMaze ?: fetchJikanPoster(canonical)
                    if (backdrop.isNullOrBlank()) {
                        backdrop = canonBackdrop
                    }
                }

                Log.d("SmartPlayer", "Enrich success for '$rawTitle' -> canonical='$canonical', poster=$poster, synopsisLen=${synopsis.length}")

                Result.success(
                    EnrichedMetadata(
                        canonicalTitle = canonical,
                        synopsis = synopsis,
                        genres = genresList,
                        posterUrl = poster,
                        backdropUrl = backdrop,
                        totalSeasonEpisodes = totalEps
                    )
                )
            } catch (t: Throwable) {
                Log.e("SmartPlayer", "Gemini enrichment error for '$rawTitle'", t)
                // If Gemini fails (e.g. quota or parse error), but we found a poster, return partial success!
                if (!poster.isNullOrBlank()) {
                    Result.success(
                        EnrichedMetadata(
                            canonicalTitle = cleanTitle,
                            synopsis = "",
                            genres = emptyList(),
                            posterUrl = poster,
                            backdropUrl = backdrop,
                            totalSeasonEpisodes = null
                        )
                    )
                } else {
                    Result.failure(t)
                }
            }
        }

    private fun cleanMediaTitle(raw: String): String {
        var s = raw
        // Strip fansub & distributor tags
        s = s.replace(Regex("""(?i)\b(kusonime|samehadaku|oploverz|meownime|nekonime|anoboy|nanime|otakudesu|moenime|riie|shinokun|komikcast|kiryuu)\b"""), " ")
        // Strip bracketed tokens e.g. [Samehadaku], [720p], [Erai-raws], [SubsPlease]
        s = s.replace(Regex("""\[[^\]]*\]"""), " ")
        // Strip parentheses tokens with resolution or batch: (Batch), (END), (720p)
        s = s.replace(Regex("""\((?i)(?:batch|end|tamad|\d{3,4}p)[^)]*\)"""), " ")
        // Strip resolution & encoding artifacts
        s = s.replace(Regex("""(?i)\b(\d{3,4}p|x264|x265|hevc|aac|flac|bd|bdrip|web-dl|bluray|mkv|mp4)\b"""), " ")
        // Strip season indicators for search query
        s = s.replace(Regex("""(?i)\b(?:s\d+|season\s*\d+)\b"""), " ")
        // Clean symbols and extra whitespace
        s = s.replace(Regex("""[_\-]+"""), " ")
        s = s.replace(Regex("""\s+"""), " ").trim()
        return s.ifBlank { raw }
    }

    private fun fetchTvMaze(title: String): Pair<String?, String?> {
        return try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = URL("https://api.tvmaze.com/singlesearch/shows?q=$encoded")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "SmartPlayer/1.0")
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(resp)
                val img = root.optJSONObject("image")
                val original = img?.optString("original")
                val medium = img?.optString("medium")
                val poster = if (!original.isNullOrBlank()) original else medium
                if (!poster.isNullOrBlank()) {
                    Log.d("SmartPlayer", "TVMaze poster found for '$title' -> $poster")
                    return Pair(poster, original)
                }
            }
            Pair(null, null)
        } catch (e: Exception) {
            Log.w("SmartPlayer", "TVMaze lookup skipped for '$title': ${e.message}")
            Pair(null, null)
        }
    }

    private fun fetchJikanPoster(title: String): String? {
        return try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = URL("https://api.jikan.moe/v4/anime?q=$encoded&limit=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "SmartPlayer/1.0")
            connection.requestMethod = "GET"
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(response)
                val data = root.optJSONArray("data")
                if (data != null && data.length() > 0) {
                    val first = data.getJSONObject(0)
                    val images = first.optJSONObject("images")
                    val jpg = images?.optJSONObject("jpg")
                    val large = jpg?.optString("large_image_url")
                    val standard = jpg?.optString("image_url")
                    val found = if (!large.isNullOrBlank()) large else standard
                    if (!found.isNullOrBlank()) {
                        Log.d("SmartPlayer", "Jikan poster found for '$title' -> $found")
                        return found
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.w("SmartPlayer", "Jikan poster fetch skipped for '$title': ${e.message}")
            null
        }
    }
}
