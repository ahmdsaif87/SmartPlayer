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
            if (key.isBlank()) {
                Log.w("SmartPlayer", "Gemini API key is not set, skipping enrichment")
                return@withContext Result.failure(IllegalStateException("Gemini API key is not set."))
            }

            try {
                Log.d("SmartPlayer", "Enriching via Gemini 2.5: '$rawTitle' (Season $seasonNumber)")
                val model = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = key
                )

                val prompt = """
                    You are a media metadata assistant for SmartPlayer.
                    Given this series or movie title: "$rawTitle" (Season $seasonNumber).
                    Return ONLY a valid JSON object (without markdown code blocks, just raw JSON) with these fields:
                    {
                      "canonicalTitle": "Official Clean Title",
                      "synopsis": "Engaging 2-3 sentence synopsis written in Bahasa Indonesia",
                      "genres": ["Genre1", "Genre2"],
                      "posterUrl": "Direct high quality poster image URL if known or empty string",
                      "backdropUrl": "Direct high quality backdrop image URL if known or empty string",
                      "totalSeasonEpisodes": 12
                    }
                """.trimIndent()

                val response = model.generateContent(prompt)
                val text = response.text?.trim() ?: throw IllegalStateException("Empty response from Gemini")

                val jsonMatch = Regex("""\{[\s\S]*\}""").find(text)
                val jsonStr = jsonMatch?.value ?: text
                val json = JSONObject(jsonStr)

                val canonical = json.optString("canonicalTitle", rawTitle)
                val synopsis = json.optString("synopsis", "")
                var poster = json.optString("posterUrl", "").takeIf {
                    it.isNotBlank() && it != "null" && it.startsWith("http")
                }
                val backdrop = json.optString("backdropUrl", "").takeIf {
                    it.isNotBlank() && it != "null" && it.startsWith("http")
                }
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

                // If Gemini didn't supply a poster URL, fallback to open anime database (Jikan / MAL)
                if (poster.isNullOrBlank()) {
                    val searchTitle = canonical.ifBlank { rawTitle }
                    poster = fetchJikanPoster(searchTitle)
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
                Result.failure(t)
            }
        }

    private fun fetchJikanPoster(title: String): String? {
        return try {
            // Clean title for searching (strip season suffixes like "S2" or "(Season 2)")
            val cleanQuery = title.replace(Regex("""(?i)\b(s\d+|season\s*\d+)\b.*"""), "").trim()
            val queryToUse = cleanQuery.ifBlank { title }
            val encoded = URLEncoder.encode(queryToUse, "UTF-8")
            val url = URL("https://api.jikan.moe/v4/anime?q=$encoded&limit=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
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
            Log.w("SmartPlayer", "Jikan poster fetch skipped: ${e.message}")
            null
        }
    }
}
