package com.davidbrazuna.droidmentor.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidbrazuna.droidmentor.domain.Author
import com.davidbrazuna.droidmentor.domain.ChatMessage
import com.davidbrazuna.droidmentor.ui.theme.DroidMentorTheme

/** Stateful entry point: connects the ViewModel to the stateless screen. */
@Composable
fun ChatRoute(viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(
        uiState = uiState,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::onSend,
    )
}

/** Stateless chat screen: draws the given state and reports user events. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Keep the latest message (or the progress indicator) visible
    LaunchedEffect(uiState.messages.size, uiState.isSending) {
        val lastIndex = uiState.messages.size + (if (uiState.isSending) 1 else 0) - 1
        if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("DroidMentor") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
                if (uiState.isSending) {
                    item(key = "progress") {
                        CircularProgressIndicator(Modifier.padding(8.dp).size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            ChatInput(
                input = uiState.input,
                canSend = uiState.canSend,
                enabled = !uiState.isSending,
                onInputChange = onInputChange,
                onSend = onSend,
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.author == Author.USER
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Text(text = message.text, modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
private fun ChatInput(
    input: String,
    canSend: Boolean,
    enabled: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask your Android mentor...") },
            enabled = enabled,
            maxLines = 5,
        )
        Button(onClick = onSend, enabled = canSend) {
            Text("Send")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    DroidMentorTheme {
        ChatScreen(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(Author.USER, "What is state hoisting?"),
                    ChatMessage(Author.MENTOR, "Moving state up to the caller, so the composable becomes stateless."),
                ),
                input = "Give me an example",
            ),
            onInputChange = {},
            onSend = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenSendingWithErrorPreview() {
    DroidMentorTheme {
        ChatScreen(
            uiState = ChatUiState(
                messages = listOf(ChatMessage(Author.USER, "What is a coroutine?")),
                isSending = true,
                error = "Too many requests. Wait a moment and try again.",
            ),
            onInputChange = {},
            onSend = {},
        )
    }
}
