package com.davidbrazuna.droidmentor.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

sealed interface GeminiResult {
    data class Success(val text: String) : GeminiResult
    data class Failure(val error: GeminiError) : GeminiResult
}


class GeminiService(
    private val client: HttpClient,
    private val apiKeyProvider: () -> String,
    private val model: String = DEFAULT_MODEL
) {

    companion object {
        const val DEFAULT_MODEL = "gemini-3.5-flash-lite"
        private const val API_KEY_HEADER = "x-goog-api-key"
    }

    suspend fun generate(history: List<Content>, systemInstruction: String? = null): GeminiResult {
        val request = GenerateContentRequest(
            contents = history,
            systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(it))) },
        )
        return try {
            val response = client.post("models/$model:generateContent") {
                header(API_KEY_HEADER, apiKeyProvider())
                setBody(request)
            }
            if (response.status.isSuccess()) parseSuccess(response.body())
            else GeminiResult.Failure(mapHttpError(response.status.value))
        } catch (e: CancellationException) {
            throw e                                              // never swallow cancellation
        } catch (e: IOException) {
            GeminiResult.Failure(GeminiError.NoNetwork)          // no network, timeout
        } catch (e: Exception) {
            GeminiResult.Failure(GeminiError.Unknown(e))
        }
    }

    private fun parseSuccess(body: GenerateContentResponse): GeminiResult {
        val candidate = body.candidates.firstOrNull()
            ?: return GeminiResult.Failure(GeminiError.Blocked)
        if (candidate.finishReason == "SAFETY") return GeminiResult.Failure(GeminiError.Blocked)
        val text = candidate.content?.parts.orEmpty().joinToString("") { it.text }
        return if (text.isBlank()) GeminiResult.Failure(GeminiError.Blocked)
        else GeminiResult.Success(text)
    }

    private fun mapHttpError(code: Int): GeminiError = when (code) {
        400, 401, 403 -> GeminiError.InvalidApiKey
        429 -> GeminiError.RateLimited
        in 500..599 -> GeminiError.ServerError(code)
        else -> GeminiError.Unknown()
    }
}

sealed interface GeminiError {
    data object NoNetwork : GeminiError                     // no internet or timeout
    data object InvalidApiKey : GeminiError                 // 400 / 401 / 403
    data object RateLimited : GeminiError                   // 429
    data class ServerError(val code: Int) : GeminiError     // 5xx
    data object Blocked : GeminiError                       // no reply or blocked for safety
    data class Unknown(val cause: Throwable? = null) : GeminiError
}

