package id.smartplayer.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiAiClient(
    private val apiKeyProvider: () -> String
) : AiMetadataService {

    override suspend fun enrich(rawTitle: String, seasonNumber: Int): Result<EnrichedMetadata> =
        withContext(Dispatchers.IO) {
            val key = apiKeyProvider()
            if (key.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Gemini API key is not set."))
            }

            try {
                val model = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = key
                )

                val prompt = """
                    You are a media metadata assistant for SmartPlayer.
                    Given this show or movie title: "$rawTitle" (Season $seasonNumber),
                    return ONLY a valid JSON object (without markdown code blocks, just raw JSON) with these fields:
                    {
                      "canonicalTitle": "Official Clean Title",
                      "synopsis": "Engaging 2-3 sentence synopsis written in Bahasa Indonesia",
                      "genres": ["Genre1", "Genre2"],
                      "posterUrl": "Direct high quality poster image URL if known or null",
                      "backdropUrl": "Direct high quality backdrop image URL if known or null",
                      "totalSeasonEpisodes": 12
                    }
                """.trimIndent()

                val response = model.generateContent(prompt)
                val text = response.text?.trim() ?: throw IllegalStateException("Empty response from Gemini")

                val cleanJson = text
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val json = JSONObject(cleanJson)

                val canonical = json.optString("canonicalTitle", rawTitle)
                val synopsis = json.optString("synopsis", "")
                val poster = json.optString("posterUrl", "").takeIf { it.isNotBlank() && it != "null" }
                val backdrop = json.optString("backdropUrl", "").takeIf { it.isNotBlank() && it != "null" }
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
                Result.failure(t)
            }
        }
}
