package com.davidbrazuna.droidmentor.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.davidbrazuna.droidmentor.DroidMentorApp
import com.davidbrazuna.droidmentor.data.remote.GeminiError
import com.davidbrazuna.droidmentor.data.remote.GeminiResult
import com.davidbrazuna.droidmentor.data.remote.GeminiService
import com.davidbrazuna.droidmentor.data.remote.toContent
import com.davidbrazuna.droidmentor.domain.Author
import com.davidbrazuna.droidmentor.domain.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the chat screen needs to draw itself. */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
) {
    val canSend: Boolean get() = input.isNotBlank() && !isSending
}

class ChatViewModel(
    private val geminiService: GeminiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(input = text) }
    }

    fun onSend() {
        val current = _uiState.value
        if (!current.canSend) return

        val text = current.input.trim()
        val userMessage = ChatMessage(author = Author.USER, text = text)
        _uiState.update {
            it.copy(messages = it.messages + userMessage, input = "", isSending = true, error = null)
        }

        viewModelScope.launch {
            // The API is stateless: send the whole conversation every time
            val history = _uiState.value.messages.map { it.toContent() }
            when (val result = geminiService.generate(history, SYSTEM_INSTRUCTION)) {
                is GeminiResult.Success -> _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(author = Author.MENTOR, text = result.text),
                        isSending = false,
                    )
                }
                // On failure, remove the unanswered message and give the text back,
                // so the user can retry and the history keeps alternating roles
                is GeminiResult.Failure -> _uiState.update {
                    it.copy(
                        messages = it.messages - userMessage,
                        input = text,
                        isSending = false,
                        error = result.error.toUserMessage(),
                    )
                }
            }
        }
    }

    companion object {
        private const val SYSTEM_INSTRUCTION =
            "You are DroidMentor, a senior Android engineer and mentor. " +
                "Give clear, practical advice on Android development best practices, " +
                "with short Kotlin examples when useful."

        /** Builds the ViewModel with its dependencies from the Application (service locator). */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DroidMentorApp
                ChatViewModel(app.geminiService)
            }
        }
    }
}

/** Maps each technical error to a message the user understands. */
private fun GeminiError.toUserMessage(): String = when (this) {
    GeminiError.NoNetwork -> "You're offline. Check your connection and try again."
    GeminiError.InvalidApiKey -> "The API key is invalid. Check it in Settings."
    GeminiError.RateLimited -> "Too many requests. Wait a moment and try again."
    is GeminiError.ServerError -> "Gemini is having problems (error $code). Try again later."
    GeminiError.Blocked -> "The reply was blocked. Try rephrasing your question."
    is GeminiError.Unknown -> "Something went wrong. Try again."
}
