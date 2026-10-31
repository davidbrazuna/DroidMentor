package com.davidbrazuna.droidmentor.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for the Gemini `generateContent` REST endpoint.
 * Only the fields the app uses are modelled; the JSON parser is configured
 * with `ignoreUnknownKeys = true`, so extra fields in the response are skipped.
 */

/** A single piece of a message. For now, only text parts are supported. */
@Serializable
data class Part(
    val text: String,
)

/**
 * A message in the conversation, used both in requests and responses.
 * [role] is "user" or "model". It is null for the system instruction,
 * which only carries parts.
 */
@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part> = emptyList(),
)

/**
 * Request body. The API is stateless, so [contents] carries the whole
 * conversation history, alternating "user" and "model" roles.
 */
@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    @SerialName("system_instruction")
    val systemInstruction: Content? = null,
)

/** Response body. [candidates] may be empty, for example when a reply is blocked. */
@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList(),
)

/**
 * One generated reply. [finishReason] is "STOP" for a complete reply;
 * values like "MAX_TOKENS" or "SAFETY" mean it was cut short or blocked.
 */
@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
)

/** Roles used in [Content.role]. */
object GeminiRole {
    const val USER = "user"
    const val MODEL = "model"
}
