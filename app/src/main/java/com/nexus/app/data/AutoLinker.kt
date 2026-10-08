package com.nexus.app.data

import android.util.Log
import com.nexus.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sqrt

/**
 * Sistema de vinculación automática de notas basado en comparación semántica
 * utilizando la API de Google Gemini (Gemini Embeddings), con fallback por
 * solapamiento de palabras clave si la API no está configurada o no hay conexión.
 */
object AutoLinker {

    private const val TAG = "AutoLinker"

    // Modelo de embeddings Google AI Studio
    var primaryModel: String = "text-embedding-004"
    var secondaryModel: String = "gemini-embedding-2"

    // Umbral de similitud de coseno (0.0 a 1.0)
    // Valores de 0.60 - 0.70 indican relaciones semánticas sólidas en Gemini Embeddings
    var similarityThreshold: Float = 0.65f

    // Clave de API de Google Gemini
    var apiKey: String = BuildConfig.GEMINI_API_KEY

    // Caché en memoria para almacenar vectores de embeddings por nota (ID + Hash del texto)
    private val embeddingCache = ConcurrentHashMap<String, FloatArray>()

    /**
     * Determina si dos notas están semánticamente relacionadas mediante Gemini Embeddings
     * o mediante coincidencia de palabras clave si Gemini no está disponible.
     *
     * @param noteA Primera nota
     * @param noteB Segunda nota
     * @param threshold Umbral de similitud de coseno (por defecto [similarityThreshold])
     * @return true si la similitud es mayor o igual al umbral
     */
    suspend fun areNotesRelated(
        noteA: Note,
        noteB: Note,
        threshold: Float = similarityThreshold,
    ): Boolean {
        // Una nota no se enlaza consigo misma
        if (noteA.id != 0L && (noteA.id == noteB.id)) return false

        val similarity = calculateSimilarity(noteA, noteB)
        if (similarity > 0f) {
            Log.d(TAG, "Similitud semántica entre Nota ${noteA.id} ('${noteA.title}') y Nota ${noteB.id} ('${noteB.title}'): $similarity (Umbral: $threshold)")
            return similarity >= threshold
        }

        // Fallback por coincidencias de texto si no hay embeddings (sin API Key o sin red)
        val fallbackSim = calculateTextOverlapSimilarity(noteA, noteB)
        Log.d(TAG, "Similitud por solapamiento de texto entre Nota ${noteA.id} y Nota ${noteB.id}: $fallbackSim")
        return fallbackSim >= 0.20f
    }

    /**
     * Calcula la similitud basada en la intersección de palabras clave significativas entre dos notas.
     */
    private fun calculateTextOverlapSimilarity(noteA: Note, noteB: Note): Float {
        val wordsA = extractKeywords("${noteA.title} ${noteA.content}")
        val wordsB = extractKeywords("${noteB.title} ${noteB.content}")

        if (wordsA.isEmpty() || wordsB.isEmpty()) return 0f

        val intersection = wordsA.intersect(wordsB).size.toFloat()
        val union = wordsA.union(wordsB).size.toFloat()

        return if (union > 0f) intersection / union else 0f
    }

    private fun extractKeywords(text: String): Set<String> {
        val stopWords = setOf(
            "que", "los", "las", "un", "una", "unos", "unas", "de", "del", "a", "ante",
            "con", "en", "para", "por", "y", "o", "no", "si", "su", "sus", "el", "la",
            "es", "son", "al", "lo", "como", "mas", "más", "esta", "este", "se", "todo", "toda"
        )
        return text.lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 2 && it !in stopWords }
            .toSet()
    }

    /**
     * Calcula el índice de similitud semántica (de 0.0 a 1.0) entre dos notas.
     */
    suspend fun calculateSimilarity(noteA: Note, noteB: Note): Float {
        val embeddingA = getEmbeddingForNote(noteA) ?: return 0f
        val embeddingB = getEmbeddingForNote(noteB) ?: return 0f

        return cosineSimilarity(embeddingA, embeddingB)
    }

    /**
     * Obtiene el vector de embedding para una nota utilizando la API de Google Gemini.
     * Reutiliza resultados cacheados para evitar llamadas innecesarias a la API.
     */
    suspend fun getEmbeddingForNote(note: Note): FloatArray? {
        val textToEmbed = "${note.title}\n${note.content}".trim()
        if (textToEmbed.isBlank()) return null

        val cacheKey = "note_${note.id}_${textToEmbed.hashCode()}"
        embeddingCache[cacheKey]?.let { return it }

        if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
            Log.w(
                TAG,
                "Gemini API Key no configurada."
            )
            return null
        }

        // Intenta con el modelo primario y si da 404 intenta con el secundario
        var embedding = fetchEmbeddingFromGeminiApi(textToEmbed, primaryModel)
        if (embedding == null) {
            Log.i(TAG, "Intentando con modelo secundario de embeddings: $secondaryModel")
            embedding = fetchEmbeddingFromGeminiApi(textToEmbed, secondaryModel)
        }

        if (embedding != null && embedding.isNotEmpty()) {
            embeddingCache[cacheKey] = embedding
            Log.d(TAG, "Embedding obtenido y almacenado en caché para nota ${note.id} (${embedding.size} dimensiones)")
        }

        return embedding
    }

    //Realiza la petición HTTP POST a la API REST de Gemini Embeddings.
    private suspend fun fetchEmbeddingFromGeminiApi(text: String, modelName: String): FloatArray? =
        withContext(Dispatchers.IO) {
            try {
                val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:embedContent"
                val urlString = "$apiUrl?key=$apiKey"
                val url = URL(urlString)

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                val requestBody = JSONObject().apply {
                    put("model", "models/$modelName")
                    put("content", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", text))
                        })
                    })
                }.toString()

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(requestBody)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                    parseEmbeddingResponse(responseText)
                } else {
                    val errorText = connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: "Sin detalle"
                    when (responseCode) {
                        400 -> Log.e(TAG, "HTTP 400 Bad Request en Gemini ($modelName): $errorText")
                        401 -> Log.e(TAG, "HTTP 401 Unauthorized - Revisa que tu API Key de Gemini sea válida.")
                        403 -> Log.e(TAG, "HTTP 403 Forbidden - La API Key no tiene permisos o Generative Language API no está habilitada.")
                        404 -> Log.w(TAG, "HTTP 404 Not Found - El modelo '$modelName' no fue encontrado en este endpoint.")
                        else -> Log.e(TAG, "Error HTTP $responseCode en Gemini ($modelName): $errorText")
                    }
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción de red al consultar API de Gemini Embeddings ($modelName): ${e.message}", e)
                null
            }
        }

    //Extrae el array de números flotantes del JSON de respuesta de Gemini.
    private fun parseEmbeddingResponse(jsonText: String): FloatArray? {
        return try {
            val jsonObject = JSONObject(jsonText)
            val embeddingObj = jsonObject.optJSONObject("embedding") ?: return null
            val valuesArray = embeddingObj.optJSONArray("values") ?: return null

            val floats = FloatArray(valuesArray.length())
            for (i in 0 until valuesArray.length()) {
                floats[i] = valuesArray.getDouble(i).toFloat()
            }
            floats
        } catch (e: Exception) {
            Log.e(TAG, "Error al parsear la respuesta JSON de embeddings: ${e.message}", e)
            null
        }
    }

    //Calcula la similitud de coseno entre dos vectores (de 0.0 a 1.0).
    fun cosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        if (vectorA.size != vectorB.size || vectorA.isEmpty()) return 0f

        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f

        for (i in vectorA.indices) {
            val a = vectorA[i]
            val b = vectorB[i]
            dotProduct += a * b
            normA += a * a
            normB += b * b
        }

        if (normA == 0.0f || normB == 0.0f) return 0f

        val similarity = dotProduct / (sqrt(normA) * sqrt(normB))
        return similarity.coerceIn(0f, 1f)
    }

    //Invalida la caché de embeddings para una nota específica.
    fun invalidateCacheForNote(noteId: Long) {
        val keysToRemove = embeddingCache.keys().toList().filter { it.startsWith("note_${noteId}_") }
        for (key in keysToRemove) {
            embeddingCache.remove(key)
        }
    }

    //Limpia completamente la caché de embeddings en memoria.
    fun clearCache() {
        embeddingCache.clear()
    }
}
