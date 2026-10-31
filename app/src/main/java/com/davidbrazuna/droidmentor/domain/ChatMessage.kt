package com.davidbrazuna.droidmentor.domain

import java.util.UUID

/** Who wrote a message in the conversation. */
enum class Author { USER, MENTOR }

/**
 * A message in a conversation, as the app sees it.
 * This is the domain model: it doesn't depend on the Gemini API format.
 */
data class ChatMessage(
    val author: Author,
    val text: String,
    val id: String = UUID.randomUUID().toString(),
)
